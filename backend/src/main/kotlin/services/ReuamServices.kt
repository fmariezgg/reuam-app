package ni.uam.edu.services

import ni.uam.edu.models.AuthenticatedUser
import ni.uam.edu.models.Category
import ni.uam.edu.models.ExchangeRequest
import ni.uam.edu.models.ExchangeRequestStatus
import ni.uam.edu.models.Item
import ni.uam.edu.models.ItemPhoto
import ni.uam.edu.models.ItemStatus
import ni.uam.edu.models.ItemTransactionType
import ni.uam.edu.models.UserProfile
import ni.uam.edu.models.badRequest
import ni.uam.edu.models.buildBackendError
import ni.uam.edu.models.conflict
import ni.uam.edu.models.forbidden
import ni.uam.edu.models.notFound
import ni.uam.edu.repositories.CategoryRepository
import ni.uam.edu.repositories.ExchangeRequestRepository
import ni.uam.edu.repositories.ItemRepository
import ni.uam.edu.repositories.UserProfileRepository
import ni.uam.edu.services.dto.CreateCategoryRequest
import ni.uam.edu.services.dto.CreateExchangeRequestRequest
import ni.uam.edu.services.dto.CreateItemRequest
import ni.uam.edu.services.dto.UpdateExchangeRequestStatusRequest
import ni.uam.edu.services.dto.UpdateItemRequest
import ni.uam.edu.services.dto.UpdateProfilePhotoRequest
import ni.uam.edu.services.dto.UpsertUserProfileRequest
import java.time.Instant
import java.util.UUID

class ReuamServices(
    val profiles: UserProfileService,
    val categories: CategoryService,
    val items: ItemService,
    val exchangeRequests: ExchangeRequestService,
)

class UserProfileService(private val repository: UserProfileRepository) {
    suspend fun getByFirebaseUid(firebaseUid: String): UserProfile =
        repository.findByFirebaseUid(firebaseUid) ?: throw notFound("Profile not found")

    suspend fun getCurrent(authUser: AuthenticatedUser): UserProfile =
        repository.findByFirebaseUid(authUser.firebaseUid)
            ?: throw notFound("Profile not found. Create it with PUT /api/v1/profiles/me first")

    suspend fun upsert(authUser: AuthenticatedUser, request: UpsertUserProfileRequest): UserProfile {
        val now = nowMillis()
        val existing = repository.findByFirebaseUid(authUser.firebaseUid)
        val displayName = request.displayName
            ?: existing?.displayName
            ?: authUser.name
            ?: request.email
            ?: authUser.email
            ?: "Estudiante UAM"

        return repository.upsert(
            UserProfile(
                id = existing?.id ?: UUID.randomUUID(),
                firebaseUid = authUser.firebaseUid,
                email = request.email ?: existing?.email ?: authUser.email,
                displayName = displayName.trimRequired("displayName"),
                photoUrl = request.photoUrl ?: existing?.photoUrl ?: authUser.picture,
                phoneNumber = request.phoneNumber ?: existing?.phoneNumber,
                career = request.career ?: existing?.career,
                studentCode = request.studentCode ?: existing?.studentCode,
                bio = request.bio ?: existing?.bio,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
        )
    }

    suspend fun updatePhoto(firebaseUid: String, request: UpdateProfilePhotoRequest): UserProfile {
        val existing = getByFirebaseUid(firebaseUid)
        return repository.upsert(
            existing.copy(
                photoUrl = request.photoUrl.trimRequired("photoUrl"),
                updatedAt = nowMillis(),
            )
        )
    }
}

class CategoryService(private val repository: CategoryRepository) {
    suspend fun create(request: CreateCategoryRequest): Category {
        val name = request.name.trimRequired("name")
        return repository.create(
            Category(
                id = 0,
                name = name,
                slug = name.slugify(),
                createdAt = nowMillis(),
            )
        )
    }

    suspend fun list(): List<Category> = repository.list()
}

class ItemService(
    private val repository: ItemRepository,
    private val categories: CategoryRepository,
) {
    suspend fun create(ownerId: UUID, request: CreateItemRequest): Item {
        validateCategory(request.categoryId)
        validatePrice(request.transactionType, request.priceCents)
        val now = nowMillis()
        val itemId = UUID.randomUUID()

        return repository.create(
            Item(
                id = itemId,
                ownerId = ownerId,
                title = request.title.trimRequired("title"),
                description = request.description.trimRequired("description"),
                categoryId = request.categoryId,
                condition = request.condition,
                transactionType = request.transactionType,
                priceCents = request.priceCents,
                status = ItemStatus.AVAILABLE,
                location = request.location?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = now,
                updatedAt = now,
                photos = request.photos.mapIndexed { index, photo ->
                    ItemPhoto(
                        id = UUID.randomUUID(),
                        itemId = itemId,
                        storagePath = photo.storagePath.trimRequired("photos[$index].storagePath"),
                        downloadUrl = photo.downloadUrl?.trim()?.takeIf { it.isNotEmpty() },
                        sortOrder = photo.sortOrder,
                        createdAt = now,
                    )
                },
            )
        )
    }

    suspend fun get(id: UUID): Item = repository.findById(id) ?: throw notFound("Item not found")

    suspend fun listAvailable(): List<Item> = repository.list(status = ItemStatus.AVAILABLE)

    suspend fun listMine(ownerId: UUID): List<Item> = repository.list(ownerId = ownerId, status = null)

    suspend fun update(ownerId: UUID, id: UUID, request: UpdateItemRequest): Item {
        val existing = get(id)
        if (existing.ownerId != ownerId) throw forbidden("Only the owner can update this item")

        val transactionType = request.transactionType ?: existing.transactionType
        val priceCents = request.priceCents ?: existing.priceCents
        validateCategory(request.categoryId ?: existing.categoryId)
        validatePrice(transactionType, priceCents)

        val now = nowMillis()
        val photos = request.photos?.mapIndexed { index, photo ->
            ItemPhoto(
                id = UUID.randomUUID(),
                itemId = id,
                storagePath = photo.storagePath.trimRequired("photos[$index].storagePath"),
                downloadUrl = photo.downloadUrl?.trim()?.takeIf { it.isNotEmpty() },
                sortOrder = photo.sortOrder,
                createdAt = now,
            )
        } ?: existing.photos

        return repository.update(
            existing.copy(
                title = request.title?.trimRequired("title") ?: existing.title,
                description = request.description?.trimRequired("description") ?: existing.description,
                categoryId = request.categoryId ?: existing.categoryId,
                condition = request.condition ?: existing.condition,
                transactionType = transactionType,
                priceCents = priceCents,
                status = request.status ?: existing.status,
                location = request.location?.trim()?.takeIf { it.isNotEmpty() } ?: existing.location,
                updatedAt = now,
                photos = photos,
            )
        )
    }

    suspend fun delete(ownerId: UUID, id: UUID) {
        val existing = get(id)
        if (existing.ownerId != ownerId) throw forbidden("Only the owner can delete this item")
        repository.delete(id)
    }

    private suspend fun validateCategory(categoryId: Int?) {
        if (categoryId != null && categories.findById(categoryId) == null) {
            throw badRequest("Category does not exist")
        }
    }

    private fun validatePrice(type: ItemTransactionType, priceCents: Long?) {
        if (priceCents != null && priceCents < 0) {
            throw badRequest("priceCents cannot be negative")
        }
        if (type == ItemTransactionType.SYMBOLIC_SALE && priceCents == null) {
            throw badRequest("priceCents is required for symbolic sales")
        }
    }
}

class ExchangeRequestService(
    private val repository: ExchangeRequestRepository,
    private val items: ItemRepository,
) {
    suspend fun create(requesterId: UUID, request: CreateExchangeRequestRequest): ExchangeRequest {
        val itemId = request.itemId.toUuid("itemId")
        val item = items.findById(itemId) ?: throw notFound("Item not found")
        if (item.ownerId == requesterId) {
            throw conflict("You cannot request your own item")
        }
        if (item.status != ItemStatus.AVAILABLE) {
            throw conflict("Item is not available")
        }

        val offerItemId = request.offerItemId?.toUuid("offerItemId")
        if (offerItemId != null) {
            val offerItem = items.findById(offerItemId) ?: throw badRequest("Offer item does not exist")
            if (offerItem.ownerId != requesterId) {
                throw forbidden("Offer item must belong to the requester")
            }
        }

        val now = nowMillis()
        return repository.create(
            ExchangeRequest(
                id = UUID.randomUUID(),
                itemId = itemId,
                requesterId = requesterId,
                ownerId = item.ownerId,
                offerItemId = offerItemId,
                message = request.message?.trim()?.takeIf { it.isNotEmpty() },
                status = ExchangeRequestStatus.PENDING,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    suspend fun listSent(requesterId: UUID): List<ExchangeRequest> = repository.listSent(requesterId)

    suspend fun listReceived(ownerId: UUID): List<ExchangeRequest> = repository.listReceived(ownerId)

    suspend fun updateStatus(ownerId: UUID, id: UUID, request: UpdateExchangeRequestStatusRequest): ExchangeRequest {
        val existing = repository.findById(id) ?: throw notFound("Exchange request not found")
        if (existing.ownerId != ownerId) {
            throw forbidden("Only the item owner can update this request")
        }
        if (request.status == ExchangeRequestStatus.CANCELLED) {
            throw badRequest("Use the cancel endpoint to cancel your own request")
        }
        return repository.updateStatus(id, request.status, nowMillis())
            ?: throw notFound("Exchange request not found")
    }

    suspend fun cancel(requesterId: UUID, id: UUID): ExchangeRequest {
        val existing = repository.findById(id) ?: throw notFound("Exchange request not found")
        if (existing.requesterId != requesterId) {
            throw forbidden("Only the requester can cancel this request")
        }
        return repository.updateStatus(id, ExchangeRequestStatus.CANCELLED, nowMillis())
            ?: throw notFound("Exchange request not found")
    }
}

fun String.toUuid(field: String): UUID =
    try {
        UUID.fromString(this)
    } catch (cause: IllegalArgumentException) {
        throw buildBackendError(code = "validation_error", detail = "There was an error validating your request") {
            error(field, "Must be a valid UUID")
        }
    }

private fun String.trimRequired(field: String): String =
    trim().takeIf { it.isNotEmpty() }
        ?: throw buildBackendError(code = "validation_error", detail = "There was an error validating your request") {
            error(field, "Field is required")
        }

private fun String.slugify(): String =
    lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifEmpty { UUID.randomUUID().toString() }

private fun nowMillis(): Long = Instant.now().toEpochMilli()
