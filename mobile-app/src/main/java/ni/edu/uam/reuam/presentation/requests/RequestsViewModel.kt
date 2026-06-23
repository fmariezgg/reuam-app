package ni.edu.uam.reuam.presentation.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.remote.dto.ExchangeRequestResponse
import ni.edu.uam.reuam.data.remote.dto.ExchangeRequestStatus
import ni.edu.uam.reuam.data.repository.ExchangeRequestRepository
import ni.edu.uam.reuam.data.repository.ItemRepository

enum class RequestsTab { SENT, RECEIVED }

/**
 * ExchangeRequestResponse solo trae itemId (no el título del artículo), así
 * que esta proyección le agrega el título resuelto con una llamada extra a
 * GET /items/{id} por cada artículo distinto involucrado. Es una solución
 * N+1 razonable para esta fase porque el número de solicitudes por usuario
 * es pequeño; si la lista crece mucho, lo correcto sería que el backend
 * exponga el título directamente en ExchangeRequestResponse.
 */
data class RequestUi(
    val request: ExchangeRequestResponse,
    val itemTitle: String,
)

class RequestsViewModel(
    private val repository: ExchangeRequestRepository = ExchangeRequestRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(RequestsTab.RECEIVED)
    val selectedTab: StateFlow<RequestsTab> = _selectedTab.asStateFlow()

    private val _sentState = MutableStateFlow<ApiResult<List<RequestUi>>>(ApiResult.Loading)
    val sentState: StateFlow<ApiResult<List<RequestUi>>> = _sentState.asStateFlow()

    private val _receivedState = MutableStateFlow<ApiResult<List<RequestUi>>>(ApiResult.Loading)
    val receivedState: StateFlow<ApiResult<List<RequestUi>>> = _receivedState.asStateFlow()

    /** ids de solicitudes que están procesando una acción (aceptar/rechazar/cancelar). */
    private val _processingIds = MutableStateFlow<Set<String>>(emptySet())
    val processingIds: StateFlow<Set<String>> = _processingIds.asStateFlow()

    init {
        loadReceived()
        loadSent()
    }

    fun onTabSelected(tab: RequestsTab) {
        _selectedTab.value = tab
    }

    fun loadSent() {
        viewModelScope.launch {
            _sentState.value = ApiResult.Loading
            try {
                val requests = repository.getSentRequests()
                _sentState.value = ApiResult.Success(enrichWithItemTitles(requests))
            } catch (e: ApiException) {
                _sentState.value = ApiResult.Error(e.message ?: "No se pudieron cargar tus solicitudes enviadas.")
            }
        }
    }

    fun loadReceived() {
        viewModelScope.launch {
            _receivedState.value = ApiResult.Loading
            try {
                val requests = repository.getReceivedRequests()
                _receivedState.value = ApiResult.Success(enrichWithItemTitles(requests))
            } catch (e: ApiException) {
                _receivedState.value = ApiResult.Error(e.message ?: "No se pudieron cargar las solicitudes recibidas.")
            }
        }
    }

    private suspend fun enrichWithItemTitles(requests: List<ExchangeRequestResponse>): List<RequestUi> {
        val titleByItemId = mutableMapOf<String, String>()
        return requests.map { request ->
            val title = titleByItemId.getOrPut(request.itemId) {
                try {
                    itemRepository.getItemById(request.itemId).title
                } catch (_: ApiException) {
                    "Artículo no disponible"
                }
            }
            RequestUi(request = request, itemTitle = title)
        }
    }

    /** Acepta o rechaza una solicitud recibida (yo soy el dueño del artículo). */
    fun respondToRequest(
        requestUi: RequestUi,
        newStatus: ExchangeRequestStatus,
        onError: (String) -> Unit,
    ) {
        val request = requestUi.request
        viewModelScope.launch {
            _processingIds.update { it + request.id }
            try {
                repository.updateRequestStatus(
                    requestId = request.id,
                    itemId = request.itemId,
                    newStatus = newStatus,
                )
                loadReceived()
            } catch (e: ApiException) {
                onError(e.message ?: "No se pudo actualizar la solicitud.")
            } finally {
                _processingIds.update { it - request.id }
            }
        }
    }

    /** Cancela una solicitud que yo mismo envié. */
    fun cancelMyRequest(requestUi: RequestUi, onError: (String) -> Unit) {
        val request = requestUi.request
        viewModelScope.launch {
            _processingIds.update { it + request.id }
            try {
                repository.cancelRequest(request.id)
                loadSent()
            } catch (e: ApiException) {
                onError(e.message ?: "No se pudo cancelar la solicitud.")
            } finally {
                _processingIds.update { it - request.id }
            }
        }
    }
}
