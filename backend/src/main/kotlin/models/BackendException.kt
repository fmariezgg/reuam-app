package ni.uam.edu.models

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackendErrorResponse(
    val code: String,
    val detail: String,
    @SerialName("field_errors")
    val fieldErrors: Map<String, String> = emptyMap(),
)

class BackendException(
    val status: HttpStatusCode = HttpStatusCode.BadRequest,
    val code: String,
    val detail: String,
    val fieldErrors: Map<String, String> = emptyMap(),
) : RuntimeException(detail) {
    fun toResponse() = BackendErrorResponse(code, detail, fieldErrors)
}

class BackendErrorBuilder internal constructor(
    private val fieldErrors: MutableMap<String, String>,
) {
    fun error(fieldName: String, message: String) {
        fieldErrors[fieldName] = message
    }
}

fun buildBackendError(
    code: String,
    detail: String,
    status: HttpStatusCode = HttpStatusCode.BadRequest,
    action: BackendErrorBuilder.() -> Unit = {},
): BackendException {
    val fieldErrors = mutableMapOf<String, String>()
    BackendErrorBuilder(fieldErrors).action()
    return BackendException(status, code, detail, fieldErrors)
}

fun notFound(detail: String) = BackendException(
    status = HttpStatusCode.NotFound,
    code = "not_found",
    detail = detail,
)

fun badRequest(detail: String) = BackendException(
    status = HttpStatusCode.BadRequest,
    code = "bad_request",
    detail = detail,
)

fun unauthorized(detail: String) = BackendException(
    status = HttpStatusCode.Unauthorized,
    code = "unauthorized",
    detail = detail,
)

fun forbidden(detail: String) = BackendException(
    status = HttpStatusCode.Forbidden,
    code = "forbidden",
    detail = detail,
)

fun conflict(detail: String) = BackendException(
    status = HttpStatusCode.Conflict,
    code = "conflict",
    detail = detail,
)
