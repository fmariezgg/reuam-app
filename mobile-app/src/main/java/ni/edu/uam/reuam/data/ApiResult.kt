package ni.edu.uam.reuam.data

/**
 * Envoltorio sellado para representar el resultado de cualquier llamada
 * a la API en los ViewModels, de forma consistente en toda la app.
 *
 * Se usa así desde un ViewModel:
 *
 *     var uiState by mutableStateOf<ApiResult<List<ItemResponse>>>(ApiResult.Loading)
 *     uiState = ApiResult.Loading
 *     uiState = try {
 *         ApiResult.Success(repository.getItems())
 *     } catch (e: ApiException) {
 *         ApiResult.Error(e.toUserMessage())
 *     }
 *
 * Y en Compose, un "when" exhaustivo decide qué componente mostrar:
 * LoadingContent, ErrorContent, EmptyContent o el contenido real.
 */
sealed class ApiResult<out T> {
    data object Loading : ApiResult<Nothing>()
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val fieldErrors: Map<String, String> = emptyMap()) : ApiResult<Nothing>()
}