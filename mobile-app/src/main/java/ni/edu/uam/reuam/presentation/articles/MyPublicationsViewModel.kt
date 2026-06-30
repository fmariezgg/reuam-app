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
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.repository.ItemRepository

class MyPublicationsViewModel(
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<ApiResult<List<ItemResponse>>>(ApiResult.Loading)
    val uiState: StateFlow<ApiResult<List<ItemResponse>>> = _uiState.asStateFlow()

    private val _deletingIds = MutableStateFlow<Set<String>>(emptySet())
    val deletingIds: StateFlow<Set<String>> = _deletingIds.asStateFlow()

    init {
        loadMyItems()
    }

    fun loadMyItems() {
        viewModelScope.launch {
            _uiState.value = ApiResult.Loading
            try {
                val items = itemRepository.getMyItems()
                _uiState.value = ApiResult.Success(items)
            } catch (e: ApiException) {
                _uiState.value = ApiResult.Error(e.message ?: "No se pudieron cargar tus publicaciones.")
            }
        }
    }

    fun deleteItem(
        itemId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _deletingIds.update { it + itemId }
            try {
                itemRepository.deleteItem(itemId)
                val current = (_uiState.value as? ApiResult.Success)?.data.orEmpty()
                _uiState.value = ApiResult.Success(current.filterNot { it.id == itemId })
                onSuccess()
            } catch (e: ApiException) {
                onError(e.message ?: "No se pudo eliminar la publicación.")
            } finally {
                _deletingIds.update { it - itemId }
            }
        }
    }
}
