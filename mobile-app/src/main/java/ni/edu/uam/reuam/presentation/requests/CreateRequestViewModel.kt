package ni.edu.uam.reuam.presentation.requests

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.repository.ExchangeRequestRepository

class CreateRequestViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ExchangeRequestRepository = ExchangeRequestRepository(),
) : ViewModel() {

    // Navigation Compose inyecta el argumento de ruta "itemId" automáticamente.
    private val itemId: String = checkNotNull(savedStateHandle["itemId"]) {
        "CreateRequestViewModel requiere el argumento de ruta 'itemId'"
    }

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun onMessageChange(value: String) {
        _message.value = value
    }

    fun sendRequest(onSuccess: () -> Unit, onError: (String) -> Unit) {
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
}
