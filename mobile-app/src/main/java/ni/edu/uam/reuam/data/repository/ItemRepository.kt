package ni.edu.uam.reuam.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.remote.ReuamApiService
import ni.edu.uam.reuam.data.remote.RetrofitInstance
import ni.edu.uam.reuam.data.remote.dto.CreateItemRequest
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.remote.dto.UpdateItemRequest
import ni.edu.uam.reuam.data.remote.dto.UploadedFileResponse
import ni.edu.uam.reuam.data.remote.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ItemRepository(
    private val api: ReuamApiService = RetrofitInstance.api,
) {
    /** GET /items — público, usado en Home y en el listado general. */
    suspend fun getItems(): List<ItemResponse> = safeApiCall { api.getItems() }

    /** GET /items/{id} — público, usado en el detalle de artículo. */
    suspend fun getItemById(id: String): ItemResponse = safeApiCall { api.getItemById(id) }

    /** GET /users/me/items — protegido, usado en "Mis publicaciones". */
    suspend fun getMyItems(): List<ItemResponse> = safeApiCall { api.getMyItems() }

    /** POST /items — protegido, usado en "Publicar artículo". */
    suspend fun createItem(request: CreateItemRequest): ItemResponse =
        safeApiCall { api.createItem(request) }

    suspend fun uploadItemPhoto(context: Context, uri: Uri): UploadedFileResponse {
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw ApiException("No se pudo leer la imagen seleccionada.")
        val contentType = resolver.getType(uri) ?: "image/*"
        val fileName = resolver.displayName(uri) ?: fallbackImageName(contentType)
        val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

        return safeApiCall { api.uploadItemPhoto(part) }
    }

    /** PUT /items/{id} — protegido, usado para editar un artículo propio. */
    suspend fun updateItem(id: String, request: UpdateItemRequest): ItemResponse =
        safeApiCall { api.updateItem(id, request) }

    /** DELETE /items/{id} — protegido, usado para borrar un artículo propio. */
    suspend fun deleteItem(id: String) {
        safeApiCall { api.deleteItem(id) }
    }
}

private fun android.content.ContentResolver.displayName(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
    } ?: uri.lastPathSegment

private fun fallbackImageName(contentType: String): String {
    val extension = when (contentType.substringBefore(';').lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
    }
    return "foto-reuam.$extension"
}
