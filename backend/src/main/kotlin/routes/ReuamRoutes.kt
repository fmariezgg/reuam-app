package ni.uam.edu.routes

import io.ktor.http.HttpStatusCode
import io.ktor.openapi.Response
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import ni.uam.edu.mapping.toResponse
import ni.uam.edu.models.AuthenticatedUser
import ni.uam.edu.models.BackendErrorResponse
import ni.uam.edu.models.badRequest
import ni.uam.edu.models.unauthorized
import ni.uam.edu.plugins.FIREBASE_AUTH_PROVIDER
import ni.uam.edu.services.ReuamServices
import ni.uam.edu.services.toUuid
import ni.uam.edu.services.dto.CategoryResponse
import ni.uam.edu.services.dto.CreateCategoryRequest
import ni.uam.edu.services.dto.CreateExchangeRequestRequest
import ni.uam.edu.services.dto.CreateItemRequest
import ni.uam.edu.services.dto.ExchangeRequestResponse
import ni.uam.edu.services.dto.ItemResponse
import ni.uam.edu.services.dto.UpdateExchangeRequestStatusRequest
import ni.uam.edu.services.dto.UpdateItemRequest
import ni.uam.edu.services.dto.UpdateProfilePhotoRequest
import ni.uam.edu.services.dto.UpsertUserProfileRequest
import ni.uam.edu.services.dto.UserProfileResponse
import ni.uam.edu.services.dto.receiveWithValidation

@OptIn(ExperimentalKtorApi::class)
fun Application.configureReuamRoutes(services: ReuamServices) {
    routing {
        get("/") {
            call.respond(mapOf("name" to "ReUAM API", "status" to "ok"))
        }.describe {
            tag("System")
            operationId = "root"
            summary = "API root"
            security { optional() }
            responses {
                HttpStatusCode.OK { description = "API root" }
            }
        }

        route("/api/v1") {
            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }.describe {
                tag("System")
                operationId = "health"
                summary = "API health check"
                security { optional() }
                responses {
                    HttpStatusCode.OK { description = "API is available" }
                }
            }

            get("/categories") {
                call.respond(services.categories.list().map { it.toResponse() })
            }.describe {
                tag("Categories")
                operationId = "listCategories"
                summary = "List categories"
                security { optional() }
                responses {
                    HttpStatusCode.OK {
                        description = "Available categories"
                        schema = jsonSchema<List<CategoryResponse>>()
                    }
                }
            }

            get("/items") {
                call.respond(services.items.listAvailable().map { it.toResponse() })
            }.describe {
                tag("Items")
                operationId = "listAvailableItems"
                summary = "List available items"
                security { optional() }
                responses {
                    HttpStatusCode.OK {
                        description = "Available items"
                        schema = jsonSchema<List<ItemResponse>>()
                    }
                }
            }

            get("/items/{id}") {
                val id = call.pathParameter("id").toUuid("id")
                call.respond(services.items.get(id).toResponse())
            }.describe {
                tag("Items")
                operationId = "getItem"
                summary = "Get item detail"
                security { optional() }
                uuidPathParameter()
                responses {
                    HttpStatusCode.OK {
                        description = "Item detail"
                        schema = jsonSchema<ItemResponse>()
                    }
                    defaultError()
                }
            }

            authenticate(FIREBASE_AUTH_PROVIDER) {
                get("/profiles/me") {
                    call.respond(call.currentProfile(services).toResponse())
                }.describe {
                    tag("Profiles")
                    operationId = "getCurrentProfile"
                    summary = "Get current profile"
                    firebaseSecurity()
                    responses {
                        HttpStatusCode.OK {
                            description = "Current profile"
                            schema = jsonSchema<UserProfileResponse>()
                        }
                        defaultError()
                    }
                }

                put("/profiles/me") {
                    val response = services.profiles.upsert(
                        authUser = call.authenticatedUser(),
                        request = call.receiveWithValidation<UpsertUserProfileRequest>(),
                    )
                    call.respond(response.toResponse())
                }.describe {
                    tag("Profiles")
                    operationId = "upsertCurrentProfile"
                    summary = "Create or update current profile"
                    firebaseSecurity()
                    jsonRequest<UpsertUserProfileRequest>()
                    okResponse<UserProfileResponse>("Profile created or updated")
                }

                patch("/profiles/me/photo") {
                    val response = services.profiles.updatePhoto(
                        firebaseUid = call.authenticatedUser().firebaseUid,
                        request = call.receiveWithValidation<UpdateProfilePhotoRequest>(),
                    )
                    call.respond(response.toResponse())
                }.describe {
                    tag("Profiles")
                    operationId = "updateCurrentProfilePhoto"
                    summary = "Update current profile photo"
                    firebaseSecurity()
                    jsonRequest<UpdateProfilePhotoRequest>()
                    okResponse<UserProfileResponse>("Profile photo updated")
                }

                post("/categories") {
                    val response = services.categories.create(call.receiveWithValidation<CreateCategoryRequest>())
                    call.respond(HttpStatusCode.Created, response.toResponse())
                }.describe {
                    tag("Categories")
                    operationId = "createCategory"
                    summary = "Create category"
                    firebaseSecurity()
                    jsonRequest<CreateCategoryRequest>()
                    createdResponse<CategoryResponse>("Category created")
                }

                get("/users/me/items") {
                    val profile = call.currentProfile(services)
                    call.respond(services.items.listMine(profile.id).map { it.toResponse() })
                }.describe {
                    tag("Items")
                    operationId = "listCurrentUserItems"
                    summary = "List current user's items"
                    firebaseSecurity()
                    responses {
                        HttpStatusCode.OK {
                            description = "Current user's items"
                            schema = jsonSchema<List<ItemResponse>>()
                        }
                        defaultError()
                    }
                }

                post("/items") {
                    val profile = call.currentProfile(services)
                    val response = services.items.create(
                        ownerId = profile.id,
                        request = call.receiveWithValidation<CreateItemRequest>(),
                    )
                    call.respond(HttpStatusCode.Created, response.toResponse())
                }.describe {
                    tag("Items")
                    operationId = "createItem"
                    summary = "Create item"
                    firebaseSecurity()
                    jsonRequest<CreateItemRequest>()
                    createdResponse<ItemResponse>("Item created")
                }

                put("/items/{id}") {
                    val profile = call.currentProfile(services)
                    val response = services.items.update(
                        ownerId = profile.id,
                        id = call.pathParameter("id").toUuid("id"),
                        request = call.receiveWithValidation<UpdateItemRequest>(),
                    )
                    call.respond(response.toResponse())
                }.describe {
                    tag("Items")
                    operationId = "updateItem"
                    summary = "Update item"
                    firebaseSecurity()
                    uuidPathParameter()
                    jsonRequest<UpdateItemRequest>()
                    okResponse<ItemResponse>("Item updated")
                }

                delete("/items/{id}") {
                    val profile = call.currentProfile(services)
                    services.items.delete(
                        ownerId = profile.id,
                        id = call.pathParameter("id").toUuid("id"),
                    )
                    call.respond(HttpStatusCode.NoContent)
                }.describe {
                    tag("Items")
                    operationId = "deleteItem"
                    summary = "Delete item"
                    firebaseSecurity()
                    uuidPathParameter()
                    responses {
                        HttpStatusCode.NoContent { description = "Item deleted" }
                        defaultError()
                    }
                }

                post("/exchange-requests") {
                    val profile = call.currentProfile(services)
                    val response = services.exchangeRequests.create(
                        requesterId = profile.id,
                        request = call.receiveWithValidation<CreateExchangeRequestRequest>(),
                    )
                    call.respond(HttpStatusCode.Created, response.toResponse())
                }.describe {
                    tag("ExchangeRequests")
                    operationId = "createExchangeRequest"
                    summary = "Create exchange request"
                    firebaseSecurity()
                    jsonRequest<CreateExchangeRequestRequest>()
                    createdResponse<ExchangeRequestResponse>("Exchange request created")
                }

                get("/exchange-requests/sent") {
                    val profile = call.currentProfile(services)
                    call.respond(services.exchangeRequests.listSent(profile.id).map { it.toResponse() })
                }.describe {
                    tag("ExchangeRequests")
                    operationId = "listSentExchangeRequests"
                    summary = "List sent exchange requests"
                    firebaseSecurity()
                    responses {
                        HttpStatusCode.OK {
                            description = "Sent exchange requests"
                            schema = jsonSchema<List<ExchangeRequestResponse>>()
                        }
                        defaultError()
                    }
                }

                get("/exchange-requests/received") {
                    val profile = call.currentProfile(services)
                    call.respond(services.exchangeRequests.listReceived(profile.id).map { it.toResponse() })
                }.describe {
                    tag("ExchangeRequests")
                    operationId = "listReceivedExchangeRequests"
                    summary = "List received exchange requests"
                    firebaseSecurity()
                    responses {
                        HttpStatusCode.OK {
                            description = "Received exchange requests"
                            schema = jsonSchema<List<ExchangeRequestResponse>>()
                        }
                        defaultError()
                    }
                }

                patch("/exchange-requests/{id}/status") {
                    val profile = call.currentProfile(services)
                    val response = services.exchangeRequests.updateStatus(
                        ownerId = profile.id,
                        id = call.pathParameter("id").toUuid("id"),
                        request = call.receiveWithValidation<UpdateExchangeRequestStatusRequest>(),
                    )
                    call.respond(response.toResponse())
                }.describe {
                    tag("ExchangeRequests")
                    operationId = "updateExchangeRequestStatus"
                    summary = "Update exchange request status"
                    firebaseSecurity()
                    uuidPathParameter()
                    jsonRequest<UpdateExchangeRequestStatusRequest>()
                    okResponse<ExchangeRequestResponse>("Exchange request status updated")
                }

                post("/exchange-requests/{id}/cancel") {
                    val profile = call.currentProfile(services)
                    val response = services.exchangeRequests.cancel(
                        requesterId = profile.id,
                        id = call.pathParameter("id").toUuid("id"),
                    )
                    call.respond(response.toResponse())
                }.describe {
                    tag("ExchangeRequests")
                    operationId = "cancelExchangeRequest"
                    summary = "Cancel exchange request"
                    firebaseSecurity()
                    uuidPathParameter()
                    okResponse<ExchangeRequestResponse>("Exchange request cancelled")
                }
            }
        }
    }
}

private suspend fun ApplicationCall.currentProfile(services: ReuamServices) =
    services.profiles.getCurrent(authenticatedUser())

private fun ApplicationCall.authenticatedUser(): AuthenticatedUser =
    principal() ?: throw unauthorized("Authenticated user is missing")

private fun ApplicationCall.pathParameter(name: String): String =
    parameters[name] ?: throw badRequest("$name path parameter is required")

@OptIn(ExperimentalKtorApi::class)
private fun io.ktor.openapi.Operation.Builder.firebaseSecurity() {
    security { requirement(FIREBASE_AUTH_PROVIDER) }
}

@OptIn(ExperimentalKtorApi::class)
private fun io.ktor.openapi.Operation.Builder.uuidPathParameter() {
    parameters {
        path("id") {
            description = "Resource UUID"
            schema = jsonSchema<String>()
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
private inline fun <reified T : Any> io.ktor.openapi.Operation.Builder.jsonRequest() {
    requestBody {
        required = true
        schema = jsonSchema<T>()
    }
}

@OptIn(ExperimentalKtorApi::class)
private inline fun <reified T : Any> io.ktor.openapi.Operation.Builder.okResponse(description: String) {
    responses {
        HttpStatusCode.OK {
            this.description = description
            schema = jsonSchema<T>()
        }
        defaultError()
    }
}

@OptIn(ExperimentalKtorApi::class)
private inline fun <reified T : Any> io.ktor.openapi.Operation.Builder.createdResponse(description: String) {
    responses {
        HttpStatusCode.Created {
            this.description = description
            schema = jsonSchema<T>()
        }
        defaultError()
    }
}

private fun io.ktor.openapi.Responses.Builder.defaultError() {
    default {
        description = "API error"
        schema = jsonSchema<BackendErrorResponse>()
    }
}
