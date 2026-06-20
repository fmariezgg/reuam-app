package ni.edu.uam.reuam.data.repository

import ni.edu.uam.reuam.data.remote.ReuamApiService
import ni.edu.uam.reuam.data.remote.RetrofitInstance
import ni.edu.uam.reuam.data.remote.dto.CategoryResponse
import ni.edu.uam.reuam.data.remote.safeApiCall

/**
 * Repositorio de categorías — el más simple de todos porque el endpoint
 * es público (GET /categories) y no recibe parámetros. Sirve como caso
 * de prueba de la Fase 1: si esta llamada funciona, toda la capa de red
 * (BASE_URL, OkHttp, Retrofit, Gson, manejo de errores) está correcta.
 */
class CategoryRepository(
    private val api: ReuamApiService = RetrofitInstance.api,
) {
    suspend fun getCategories(): List<CategoryResponse> = safeApiCall { api.getCategories() }
}
