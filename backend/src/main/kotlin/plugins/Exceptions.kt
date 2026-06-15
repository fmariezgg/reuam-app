package ni.uam.edu.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import ni.uam.edu.models.BackendErrorResponse
import ni.uam.edu.models.BackendException

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureExceptions() {
    install(StatusPages) {
        exception<BackendException> { call, cause ->
            call.respond(cause.status, cause.toResponse())
        }

        exception<BadRequestException> { call, cause ->
            val missingFieldException = cause.cause?.cause as? MissingFieldException

            if (missingFieldException != null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BackendErrorResponse(
                        code = "validation_error",
                        detail = "There was an error validating your request",
                        fieldErrors = missingFieldException.missingFields.associateWith { "Field is required" },
                    )
                )
                return@exception
            }

            call.respond(
                HttpStatusCode.BadRequest,
                BackendErrorResponse(
                    code = "bad_request",
                    detail = cause.message ?: "The request body is invalid",
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                BackendErrorResponse(
                    code = "internal_server_error",
                    detail = "Unexpected server error",
                )
            )
        }
    }
}
