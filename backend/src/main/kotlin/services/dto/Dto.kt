package ni.uam.edu.services.dto

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import ni.uam.edu.models.BackendException

interface Dto {
    @Throws(BackendException::class)
    fun validate()
}

suspend inline fun <reified T : Dto> ApplicationCall.receiveWithValidation(): T {
    val body = receive<T>()
    body.validate()
    return body
}
