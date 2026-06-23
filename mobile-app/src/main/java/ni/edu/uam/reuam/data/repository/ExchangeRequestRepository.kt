package ni.edu.uam.reuam.data.repository

import ni.edu.uam.reuam.data.remote.ReuamApiService
import ni.edu.uam.reuam.data.remote.RetrofitInstance
import ni.edu.uam.reuam.data.remote.dto.CreateExchangeRequestRequest
import ni.edu.uam.reuam.data.remote.dto.ExchangeRequestResponse
import ni.edu.uam.reuam.data.remote.dto.ExchangeRequestStatus
import ni.edu.uam.reuam.data.remote.dto.ItemStatus
import ni.edu.uam.reuam.data.remote.dto.UpdateExchangeRequestStatusRequest
import ni.edu.uam.reuam.data.remote.dto.UpdateItemRequest
import ni.edu.uam.reuam.data.remote.safeApiCall

class ExchangeRequestRepository(
    private val api: ReuamApiService = RetrofitInstance.api,
    private val itemRepository: ItemRepository = ItemRepository(),
) {
    /** POST /exchange-requests — crea una solicitud sobre el artículo de otro estudiante. */
    suspend fun createRequest(itemId: String, message: String?): ExchangeRequestResponse =
        safeApiCall {
            api.createExchangeRequest(
                CreateExchangeRequestRequest(itemId = itemId, message = message?.trim()?.ifBlank { null })
            )
        }

    /** GET /exchange-requests/sent — solicitudes que yo envié sobre artículos de otros. */
    suspend fun getSentRequests(): List<ExchangeRequestResponse> =
        safeApiCall { api.getSentExchangeRequests() }

    /** GET /exchange-requests/received — solicitudes que otros me enviaron sobre mis artículos. */
    suspend fun getReceivedRequests(): List<ExchangeRequestResponse> =
        safeApiCall { api.getReceivedExchangeRequests() }

    /**
     * Cambia el estado de una solicitud recibida (yo soy el dueño del artículo).
     *
     * El backend NO actualiza el item automáticamente al aceptar una solicitud
     * (solo cambia el estado de la fila de ExchangeRequest), así que cuando el
     * nuevo estado es ACCEPTED, esta función hace una segunda llamada para
     * marcar el item como RESERVED — de lo contrario el artículo seguiría
     * apareciendo como disponible para otros estudiantes después de aceptado.
     */
    suspend fun updateRequestStatus(
        requestId: String,
        itemId: String,
        newStatus: ExchangeRequestStatus,
    ): ExchangeRequestResponse {
        val updated = safeApiCall {
            api.updateExchangeRequestStatus(requestId, UpdateExchangeRequestStatusRequest(newStatus))
        }

        if (newStatus == ExchangeRequestStatus.ACCEPTED) {
            itemRepository.updateItem(
                itemId,
                UpdateItemRequest(status = ItemStatus.RESERVED)
            )
        }

        return updated
    }

    /** POST /exchange-requests/{id}/cancel — cancela una solicitud que yo mismo envié. */
    suspend fun cancelRequest(requestId: String): ExchangeRequestResponse =
        safeApiCall { api.cancelExchangeRequest(requestId) }
}
