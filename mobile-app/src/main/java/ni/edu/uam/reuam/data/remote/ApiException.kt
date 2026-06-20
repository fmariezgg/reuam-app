package ni.edu.uam.reuam.data.remote

import com.google.gson.Gson
import ni.edu.uam.reuam.data.remote.dto.ErrorDto
import retrofit2.Response
import java.io.IOException

/**
 * Excepción interna usada por los repositorios para representar cualquier
 * fallo de red o de API de forma uniforme, ya con un mensaje listo para
 * mostrar al usuario y (si aplica) los errores de campo específicos que
 * mandó el backend en "field_errors".
 */
class ApiException(
    message: String,
    val code: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val httpCode: Int? = null,
) : Exception(message)

private val gson = Gson()

/**
 * Convierte una Response<T> de Retrofit que NO fue exitosa (isSuccessful
 * == false) en una ApiException con el mensaje real que mandó el backend,
 * parseando su body de error en el formato:
 *
 *     { "code": "...", "detail": "...", "field_errors": { ... } }
 *
 * Si el body no se puede parsear (p. ej. el servidor está caído y un
 * proxy intermedio devuelve HTML), cae a un mensaje genérico según el
 * código HTTP.
 */
fun <T> Response<T>.toApiException(): ApiException {
    val rawError = errorBody()?.string()
    val parsed = rawError?.let {
        try {
            gson.fromJson(it, ErrorDto::class.java)
        } catch (_: Exception) {
            null
        }
    }

    return if (parsed != null) {
        ApiException(
            message = parsed.detail,
            code = parsed.code,
            fieldErrors = parsed.fieldErrors,
            httpCode = code(),
        )
    } else {
        ApiException(message = genericMessageFor(code()), httpCode = code())
    }
}

/**
 * Punto único para envolver cualquier llamada suspend de Retrofit y
 * convertir tanto errores HTTP como errores de red/IO en ApiException,
 * para que los repositorios y ViewModels solo necesiten manejar un tipo
 * de excepción.
 *
 * Uso típico dentro de un repositorio:
 *
 *     suspend fun getItems(): List<ItemResponse> = safeApiCall {
 *         api.getItems()
 *     }
 */
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): T {
    val response = try {
        call()
    } catch (_: IOException) {
        throw ApiException("No se pudo conectar con el servidor. Verifica tu conexión e inténtalo de nuevo.")
    }

    if (!response.isSuccessful) {
        throw response.toApiException()
    }

    return response.body() ?: throw ApiException("El servidor respondió sin contenido.")
}

private fun genericMessageFor(httpCode: Int): String = when (httpCode) {
    400 -> "La solicitud no es válida."
    401 -> "Tu sesión expiró o no has iniciado sesión. Inicia sesión de nuevo."
    403 -> "No tienes permiso para realizar esta acción."
    404 -> "No se encontró el recurso solicitado."
    409 -> "Ya existe un conflicto con este recurso."
    in 500..599 -> "Ocurrió un error en el servidor. Inténtalo más tarde."
    else -> "Ocurrió un error inesperado ($httpCode)."
}