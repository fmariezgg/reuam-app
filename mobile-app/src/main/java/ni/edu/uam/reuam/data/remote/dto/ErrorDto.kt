package ni.edu.uam.reuam.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Espejo exacto del cuerpo de error que devuelve el backend Ktor:
 *
 * {
 *   "code": "validation_error",
 *   "detail": "There was an error validating your request",
 *   "field_errors": {
 *     "title": "Field is required"
 *   }
 * }
 */
data class ErrorDto(
    @SerializedName("code") val code: String,
    @SerializedName("detail") val detail: String,
    @SerializedName("field_errors") val fieldErrors: Map<String, String> = emptyMap(),
)

