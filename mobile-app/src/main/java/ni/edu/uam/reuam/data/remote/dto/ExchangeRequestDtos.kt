package ni.edu.uam.reuam.data.remote.dto

import com.google.gson.annotations.SerializedName

enum class ExchangeRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    COMPLETED,
}

data class CreateExchangeRequestRequest(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("offerItemId") val offerItemId: String? = null,
    @SerializedName("message") val message: String? = null,
)

data class UpdateExchangeRequestStatusRequest(
    @SerializedName("status") val status: ExchangeRequestStatus,
)

data class ExchangeRequestResponse(
    @SerializedName("id") val id: String,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("requesterId") val requesterId: String,
    @SerializedName("ownerId") val ownerId: String,
    @SerializedName("offerItemId") val offerItemId: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: ExchangeRequestStatus,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("updatedAt") val updatedAt: Long,
)
