package ni.edu.uam.reuam.presentation.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.remote.dto.CategoryResponse
import ni.edu.uam.reuam.data.remote.dto.CreateItemRequest
import ni.edu.uam.reuam.data.remote.dto.ItemCondition
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.remote.dto.ItemTransactionType
import ni.edu.uam.reuam.data.repository.CategoryRepository
import ni.edu.uam.reuam.data.repository.ItemRepository

/** Estado editable del formulario de publicación. */
data class PublishFormState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: CategoryResponse? = null,
    val condition: ItemCondition = ItemCondition.GOOD,
    val transactionType: ItemTransactionType = ItemTransactionType.DONATION,
    val priceText: String = "",
    val location: String = "",
    // field_errors que mandó el backend en el último intento de publicar,
    // por nombre de campo — para subrayar en rojo el TextField que falló.
    val fieldErrors: Map<String, String> = emptyMap(),
)

class PublishArticleViewModel(
    private val categoryRepository: CategoryRepository = CategoryRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<ApiResult<List<CategoryResponse>>>(ApiResult.Loading)
    val categoriesState: StateFlow<ApiResult<List<CategoryResponse>>> = _categoriesState.asStateFlow()

    private val _formState = MutableStateFlow(PublishFormState())
    val formState: StateFlow<PublishFormState> = _formState.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = ApiResult.Loading
            try {
                val categories = categoryRepository.getCategories()
                _categoriesState.value = ApiResult.Success(categories)
                // Preselecciona la primera categoría disponible para que el
                // selector nunca quede vacío si el usuario no lo toca.
                if (_formState.value.selectedCategory == null && categories.isNotEmpty()) {
                    _formState.update { it.copy(selectedCategory = categories.first()) }
                }
            } catch (e: ApiException) {
                _categoriesState.value = ApiResult.Error(e.message ?: "No se pudieron cargar las categorías.")
            }
        }
    }

    fun onTitleChange(value: String) = _formState.update { it.copy(title = value, fieldErrors = it.fieldErrors - "title") }
    fun onDescriptionChange(value: String) = _formState.update { it.copy(description = value, fieldErrors = it.fieldErrors - "description") }
    fun onCategorySelected(category: CategoryResponse) = _formState.update { it.copy(selectedCategory = category) }
    fun onConditionSelected(condition: ItemCondition) = _formState.update { it.copy(condition = condition) }
    fun onTransactionTypeSelected(type: ItemTransactionType) = _formState.update {
        // Si deja de ser venta simbólica, limpia el precio para no enviar
        // un priceCents residual que ya no aplica.
        val newPrice = if (type == ItemTransactionType.SYMBOLIC_SALE) it.priceText else ""
        it.copy(transactionType = type, priceText = newPrice, fieldErrors = it.fieldErrors - "priceCents")
    }
    fun onPriceChange(value: String) {
        // Solo permite dígitos — el precio se maneja en centavos como enteros.
        if (value.all { it.isDigit() }) {
            _formState.update { it.copy(priceText = value, fieldErrors = it.fieldErrors - "priceCents") }
        }
    }
    fun onLocationChange(value: String) = _formState.update { it.copy(location = value) }

    /**
     * Valida localmente lo mínimo antes de llamar al backend (campos vacíos
     * obvios), pero la validación final y autoritativa siempre es la del
     * backend — sus field_errors se vuelcan en el formulario si falla.
     */
    private fun validateLocally(form: PublishFormState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (form.title.isBlank()) errors["title"] = "El título es obligatorio"
        if (form.description.isBlank()) errors["description"] = "La descripción es obligatoria"
        if (form.transactionType == ItemTransactionType.SYMBOLIC_SALE && form.priceText.isBlank()) {
            errors["priceCents"] = "Indica un precio simbólico"
        }
        return errors
    }

    fun publish(onSuccess: (ItemResponse) -> Unit, onError: (String) -> Unit) {
        val form = _formState.value
        val localErrors = validateLocally(form)
        if (localErrors.isNotEmpty()) {
            _formState.update { it.copy(fieldErrors = localErrors) }
            return
        }

        viewModelScope.launch {
            _isPublishing.value = true
            try {
                val request = CreateItemRequest(
                    title = form.title.trim(),
                    description = form.description.trim(),
                    categoryId = form.selectedCategory?.id,
                    condition = form.condition,
                    transactionType = form.transactionType,
                    priceCents = form.priceText.toLongOrNull(),
                    location = form.location.trim().ifBlank { null },
                )
                val created = itemRepository.createItem(request)
                onSuccess(created)
            } catch (e: ApiException) {
                if (e.fieldErrors.isNotEmpty()) {
                    _formState.update { it.copy(fieldErrors = e.fieldErrors) }
                }
                onError(e.message ?: "No se pudo publicar el artículo.")
            } finally {
                _isPublishing.value = false
            }
        }
    }
}
