package ni.edu.uam.reuam.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.remote.dto.CategoryResponse
import ni.edu.uam.reuam.data.remote.dto.ItemCondition
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.remote.dto.ItemStatus
import ni.edu.uam.reuam.data.remote.dto.ItemTransactionType
import ni.edu.uam.reuam.data.repository.CategoryRepository
import ni.edu.uam.reuam.data.repository.ItemRepository
import ni.edu.uam.reuam.presentation.components.ArticleStatus
import ni.edu.uam.reuam.presentation.components.ArticleType

/**
 * Artículo ya adaptado al formato que entiende ArticleCard (presentation/components),
 * con los datos crudos del backend convertidos a los enums de UI.
 *
 * No reemplaza a ItemResponse: es una proyección hecha solo para pintar la
 * tarjeta en Home/listados. El detalle de artículo (Fase 4) usa ItemResponse
 * directamente porque necesita todos los campos.
 */
data class HomeArticleUi(
    val id: String,
    val imageUrl: String,
    val title: String,
    val categoryLabel: String,
    val type: ArticleType,
    val status: ArticleStatus,
    val ownerId: String,
)

data class HomeUiData(
    val categories: List<CategoryResponse>,
    val articles: List<HomeArticleUi>,
)

class HomeViewModel(
    private val categoryRepository: CategoryRepository = CategoryRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<ApiResult<HomeUiData>>(ApiResult.Loading)
    val uiState: StateFlow<ApiResult<HomeUiData>> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = ApiResult.Loading
            try {
                // Categorías e items son independientes entre sí, pero para
                // mantener esta fase simple (y porque ambas llamadas son
                // rápidas sobre H2 local) se piden de forma secuencial.
                // Si en el futuro esto se siente lento, se puede paralelizar
                // con async/awaitAll desde kotlinx.coroutines.
                val categories = categoryRepository.getCategories()
                val items = itemRepository.getItems()

                val categoryNamesById = categories.associateBy({ it.id }, { it.name })
                val articles = items.map { it.toHomeArticleUi(categoryNamesById) }

                _uiState.value = ApiResult.Success(
                    HomeUiData(categories = categories, articles = articles)
                )
            } catch (e: ApiException) {
                _uiState.value = ApiResult.Error(e.message ?: "No se pudieron cargar los artículos.")
            }
        }
    }
}

/** Convierte un ItemResponse del backend a la proyección que pinta ArticleCard. */
private fun ItemResponse.toHomeArticleUi(categoryNamesById: Map<Int, String>): HomeArticleUi {
    val categoryLabel = categoryId?.let { categoryNamesById[it] } ?: "Sin categoría"
    val imageUrl = photos.firstOrNull()?.downloadUrl
        ?: "https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=400"

    return HomeArticleUi(
        id = id,
        imageUrl = imageUrl,
        title = title,
        categoryLabel = categoryLabel,
        type = transactionType.toArticleType(),
        status = status.toArticleStatus(),
        ownerId = ownerId,
    )
}

/** Pública porque se reutiliza en Home y en Mis publicaciones. */
fun ItemTransactionType.toArticleType(): ArticleType = when (this) {
    ItemTransactionType.DONATION -> ArticleType.DONACION
    ItemTransactionType.EXCHANGE -> ArticleType.INTERCAMBIO
    ItemTransactionType.LOAN -> ArticleType.PRESTAMO
    ItemTransactionType.SYMBOLIC_SALE -> ArticleType.VENTA_SIMBOLICA
}

/**
 * El backend distingue 4 estados (AVAILABLE, RESERVED, COMPLETED, INACTIVE)
 * pero la UI actual de ArticleCard solo tiene 3 (DISPONIBLE, RESERVADO,
 * ENTREGADO). INACTIVE se mapea a ENTREGADO como la aproximación visual más
 * cercana ("ya no está disponible"); si más adelante se necesita distinguir
 * un artículo inactivo de uno completado, hay que agregar un cuarto valor
 * a ArticleStatus en StatusBadge.kt.
 *
 * Pública porque se reutiliza tanto en Home como en el Detalle de artículo.
 */
fun ItemStatus.toArticleStatus(): ArticleStatus = when (this) {
    ItemStatus.AVAILABLE -> ArticleStatus.DISPONIBLE
    ItemStatus.RESERVED -> ArticleStatus.RESERVADO
    ItemStatus.COMPLETED -> ArticleStatus.ENTREGADO
    ItemStatus.INACTIVE -> ArticleStatus.ENTREGADO
}

/** Útil para futuras pantallas que necesiten mostrar la condición en texto. */
fun ItemCondition.toDisplayLabel(): String = when (this) {
    ItemCondition.NEW -> "Nuevo"
    ItemCondition.LIKE_NEW -> "Como nuevo"
    ItemCondition.GOOD -> "Buen estado"
    ItemCondition.FAIR -> "Estado regular"
    ItemCondition.NEEDS_REPAIR -> "Necesita reparación"
}

/** Usado en Publicar artículo y Detalle de artículo para mostrar el tipo en español. */
fun ItemTransactionType.toDisplayLabel(): String = when (this) {
    ItemTransactionType.DONATION -> "Donación"
    ItemTransactionType.EXCHANGE -> "Intercambio"
    ItemTransactionType.LOAN -> "Préstamo"
    ItemTransactionType.SYMBOLIC_SALE -> "Venta simbólica"
}
