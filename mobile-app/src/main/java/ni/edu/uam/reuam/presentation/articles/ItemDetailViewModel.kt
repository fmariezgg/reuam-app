package ni.edu.uam.reuam.presentation.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.repository.ItemRepository
import ni.edu.uam.reuam.data.repository.ProfileRepository

data class ItemDetailUiData(
    val item: ItemResponse,
    /** true si el perfil del usuario actual es el dueño de este artículo. */
    val isOwner: Boolean,
)

class ItemDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository = ItemRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
) : ViewModel() {

    // Navigation Compose inyecta el argumento de ruta "itemId" automáticamente
    // en el SavedStateHandle del ViewModel — no hace falta pasarlo a mano.
    private val itemId: String = checkNotNull(savedStateHandle["itemId"]) {
        "ItemDetailViewModel requiere el argumento de ruta 'itemId'"
    }

    private val _uiState = MutableStateFlow<ApiResult<ItemDetailUiData>>(ApiResult.Loading)
    val uiState: StateFlow<ApiResult<ItemDetailUiData>> = _uiState.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    init {
        loadItem()
    }

    fun loadItem() {
        viewModelScope.launch {
            _uiState.value = ApiResult.Loading
            try {
                val item = itemRepository.getItemById(itemId)

                // Determinar si el usuario actual es el dueño requiere su
                // perfil (el ownerId del item es un id de perfil, no el
                // firebaseUid). Si el usuario no tiene sesión o falla la
                // carga del perfil, simplemente se asume que no es el dueño
                // — es la opción segura: nunca mostrar acciones de edición
                // de más.
                val isOwner = try {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        false
                    } else {
                        val myProfile = profileRepository.getOrCreateMyProfile(
                            FirebaseAuth.getInstance().currentUser
                        )
                        myProfile.id == item.ownerId
                    }
                } catch (_: ApiException) {
                    false
                }

                _uiState.value = ApiResult.Success(ItemDetailUiData(item = item, isOwner = isOwner))
            } catch (e: ApiException) {
                _uiState.value = ApiResult.Error(e.message ?: "No se pudo cargar el artículo.")
            }
        }
    }

    fun deleteItem(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                itemRepository.deleteItem(itemId)
                onSuccess()
            } catch (e: ApiException) {
                onError(e.message ?: "No se pudo eliminar el artículo.")
            } finally {
                _isDeleting.value = false
            }
        }
    }
}
