package ni.edu.uam.reuam.data.remote

import ni.edu.uam.reuam.data.remote.dto.CategoryResponse
import ni.edu.uam.reuam.data.remote.dto.CreateExchangeRequestRequest
import ni.edu.uam.reuam.data.remote.dto.CreateItemRequest
import ni.edu.uam.reuam.data.remote.dto.ExchangeRequestResponse
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.remote.dto.UpdateExchangeRequestStatusRequest
import ni.edu.uam.reuam.data.remote.dto.UpdateItemRequest
import ni.edu.uam.reuam.data.remote.dto.UpdateProfilePhotoRequest
import ni.edu.uam.reuam.data.remote.dto.UploadedFileResponse
import ni.edu.uam.reuam.data.remote.dto.UpsertUserProfileRequest
import ni.edu.uam.reuam.data.remote.dto.UserProfileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Definición de todos los endpoints REST de ReUAM.
 *
 * El Authorization header con el Firebase ID token NO se declara aquí:
 * lo agrega automáticamente AuthInterceptor (ver AuthInterceptor.kt) para
 * cada request que lo necesite, así esta interfaz se queda limpia.
 *
 * Todas las funciones son "suspend" para poder llamarlas directamente
 * desde coroutines de un ViewModel sin bloquear el hilo principal.
 */
interface ReuamApiService {

    // ── Endpoints públicos ──────────────────────────────────────────

    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @GET("items")
    suspend fun getItems(): Response<List<ItemResponse>>

    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<ItemResponse>

    // ── Perfil (protegido) ──────────────────────────────────────────

    @GET("profiles/me")
    suspend fun getMyProfile(): Response<UserProfileResponse>

    @PUT("profiles/me")
    suspend fun upsertMyProfile(@Body request: UpsertUserProfileRequest): Response<UserProfileResponse>

    @PATCH("profiles/me/photo")
    suspend fun updateMyProfilePhoto(@Body request: UpdateProfilePhotoRequest): Response<UserProfileResponse>

    // ── Mis artículos (protegido) ───────────────────────────────────

    @GET("users/me/items")
    suspend fun getMyItems(): Response<List<ItemResponse>>

    @POST("items")
    suspend fun createItem(@Body request: CreateItemRequest): Response<ItemResponse>

    @Multipart
    @POST("uploads/item-photo")
    suspend fun uploadItemPhoto(@Part file: MultipartBody.Part): Response<UploadedFileResponse>

    @PUT("items/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Body request: UpdateItemRequest,
    ): Response<ItemResponse>

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Response<Unit>

    // ── Solicitudes de intercambio (protegido) ──────────────────────

    @POST("exchange-requests")
    suspend fun createExchangeRequest(
        @Body request: CreateExchangeRequestRequest,
    ): Response<ExchangeRequestResponse>

    @GET("exchange-requests/sent")
    suspend fun getSentExchangeRequests(): Response<List<ExchangeRequestResponse>>

    @GET("exchange-requests/received")
    suspend fun getReceivedExchangeRequests(): Response<List<ExchangeRequestResponse>>

    @PATCH("exchange-requests/{id}/status")
    suspend fun updateExchangeRequestStatus(
        @Path("id") id: String,
        @Body request: UpdateExchangeRequestStatusRequest,
    ): Response<ExchangeRequestResponse>

    @POST("exchange-requests/{id}/cancel")
    suspend fun cancelExchangeRequest(@Path("id") id: String): Response<ExchangeRequestResponse>
}
