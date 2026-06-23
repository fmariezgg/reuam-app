package ni.edu.uam.reuam.presentation.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
}
