package ni.edu.uam.reuam.data.repository

import ni.edu.uam.reuam.data.remote.ReuamApiService
import ni.edu.uam.reuam.data.remote.RetrofitInstance
import ni.edu.uam.reuam.data.remote.dto.CreateItemRequest
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.remote.dto.UpdateItemRequest
import ni.edu.uam.reuam.data.remote.safeApiCall

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

    /** PUT /items/{id} — protegido, usado para editar un artículo propio. */
    suspend fun updateItem(id: String, request: UpdateItemRequest): ItemResponse =
        safeApiCall { api.updateItem(id, request) }

    /** DELETE /items/{id} — protegido, usado para borrar un artículo propio. */
    suspend fun deleteItem(id: String) {
        safeApiCall { api.deleteItem(id) }
    }
}