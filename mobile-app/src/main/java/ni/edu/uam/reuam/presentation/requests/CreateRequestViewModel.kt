package ni.edu.uam.reuam.presentation.requests

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.repository.ExchangeRequestRepository
import ni.edu.uam.reuam.data.repository.ItemRepository

class CreateRequestViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ExchangeRequestRepository = ExchangeRequestRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"]) {
        "CreateRequestViewModel requiere el argumento de ruta 'itemId'"
    }

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _itemState = MutableStateFlow<ApiResult<ItemResponse>>(ApiResult.Loading)
    val itemState: StateFlow<ApiResult<ItemResponse>> = _itemState.asStateFlow()

    init {
        loadItemSummary()
    }

    fun loadItemSummary() {
        viewModelScope.launch {
            _itemState.value = ApiResult.Loading
            try {
                _itemState.value = ApiResult.Success(itemRepository.getItemById(itemId))
            } catch (e: ApiException) {
                _itemState.value = ApiResult.Error(e.message ?: "No se pudo cargar el artículo.")
            }
        }
    }

    fun onMessageChange(value: String) {
        _message.value = value.take(300)
    }

    fun sendRequest(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                repository.createRequest(itemId = itemId, message = _message.value)
                onSuccess()
            } catch (e: ApiException) {
                onError(e.message ?: "No se pudo enviar la solicitud.")
            } finally {
                _isSending.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                CreateRequestViewModel(savedStateHandle = savedStateHandle)
            }
        }
    }
}
