package ni.uam.edu.services.dto

import kotlinx.serialization.Serializable
import ni.uam.edu.models.ExchangeRequestStatus
import ni.uam.edu.models.ItemCondition
import ni.uam.edu.models.ItemStatus
import ni.uam.edu.models.ItemTransactionType
import ni.uam.edu.models.validation.buildValidationService
import ni.uam.edu.models.validation.email
import ni.uam.edu.models.validation.max
import ni.uam.edu.models.validation.maxItems
import ni.uam.edu.models.validation.min
import ni.uam.edu.models.validation.minValue
import ni.uam.edu.models.validation.required
import ni.uam.edu.models.validation.uuid

@Serializable
data class UserProfileResponse(
    val id: String,
    val firebaseUid: String,
    val email: String? = null,
    val displayName: String,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val career: String? = null,
    val studentCode: String? = null,
    val bio: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class UpsertUserProfileRequest(
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val career: String? = null,
    val studentCode: String? = null,
    val bio: String? = null,
) : Dto {
    override fun validate() {
        buildValidationService {
            field("email", email) {
                email()
                max(255)
            }
            field("displayName", displayName) {
                min(2)
                max(120)
            }
            field("photoUrl", photoUrl) { max(1000) }
            field("phoneNumber", phoneNumber) { max(32) }
            field("career", career) { max(120) }
            field("studentCode", studentCode) { max(32) }
            field("bio", bio) { max(500) }
        }.validate()
    }
}

@Serializable
data class UpdateProfilePhotoRequest(
    val photoUrl: String,
) : Dto {
    override fun validate() {
        buildValidationService {
            field("photoUrl", photoUrl) {
                required()
                max(1000)
            }
        }.validate()
    }
}

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val slug: String,
    val createdAt: Long,
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
) : Dto {
    override fun validate() {
        buildValidationService {
            field("name", name) {
                required()
                min(2)
                max(80)
            }
        }.validate()
    }
}

@Serializable
data class ItemPhotoInput(
    val storagePath: String,
    val downloadUrl: String? = null,
    val sortOrder: Int = 0,
)

@Serializable
data class ItemPhotoResponse(
    val id: String,
    val itemId: String,
    val storagePath: String,
    val downloadUrl: String? = null,
    val sortOrder: Int,
    val createdAt: Long,
)

@Serializable
data class CreateItemRequest(
    val title: String,
    val description: String,
    val categoryId: Int? = null,
    val condition: ItemCondition,
    val transactionType: ItemTransactionType,
    val priceCents: Long? = null,
    val location: String? = null,
    val photos: List<ItemPhotoInput> = emptyList(),
) : Dto {
    override fun validate() {
        validateItemPhotoInputs(photos)
        buildValidationService {
            field("title", title) {
                required()
                min(3)
                max(120)
            }
            field("description", description) {
                required()
                min(5)
                max(2000)
            }
            field("categoryId", categoryId) { minValue(1) }
            field("priceCents", priceCents) { minValue(0) }
            field("location", location) { max(160) }
            field("photos", photos) { maxItems(8) }
            refine {
                if (transactionType == ItemTransactionType.SYMBOLIC_SALE && priceCents == null)
                    "priceCents" to "Field is required for symbolic sales"
                else null
            }
        }.validate()
    }
}

@Serializable
data class UpdateItemRequest(
    val title: String? = null,
    val description: String? = null,
    val categoryId: Int? = null,
    val condition: ItemCondition? = null,
    val transactionType: ItemTransactionType? = null,
    val priceCents: Long? = null,
    val status: ItemStatus? = null,
    val location: String? = null,
    val photos: List<ItemPhotoInput>? = null,
) : Dto {
    override fun validate() {
        photos?.let(::validateItemPhotoInputs)
        buildValidationService {
            field("title", title) {
                min(3)
                max(120)
            }
            field("description", description) {
                min(5)
                max(2000)
            }
            field("categoryId", categoryId) { minValue(1) }
            field("priceCents", priceCents) { minValue(0) }
            field("location", location) { max(160) }
            field("photos", photos) { maxItems(8) }
            refine {
                if (transactionType == ItemTransactionType.SYMBOLIC_SALE && priceCents == null)
                    "priceCents" to "Field is required for symbolic sales"
                else null
            }
        }.validate()
    }
}

@Serializable
data class ItemResponse(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val categoryId: Int? = null,
    val condition: ItemCondition,
    val transactionType: ItemTransactionType,
    val priceCents: Long? = null,
    val status: ItemStatus,
    val location: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val photos: List<ItemPhotoResponse>,
)

@Serializable
data class CreateExchangeRequestRequest(
    val itemId: String,
    val offerItemId: String? = null,
    val message: String? = null,
) : Dto {
    override fun validate() {
        buildValidationService {
            field("itemId", itemId) {
                required()
                uuid()
            }
            field("offerItemId", offerItemId) { uuid() }
            field("message", message) { max(500) }
        }.validate()
    }
}

@Serializable
data class UpdateExchangeRequestStatusRequest(
    val status: ExchangeRequestStatus,
) : Dto {
    override fun validate() = Unit
}

@Serializable
data class ExchangeRequestResponse(
    val id: String,
    val itemId: String,
    val requesterId: String,
    val ownerId: String,
    val offerItemId: String? = null,
    val message: String? = null,
    val status: ExchangeRequestStatus,
    val createdAt: Long,
    val updatedAt: Long,
)

private fun validateItemPhotoInputs(photos: List<ItemPhotoInput>) {
    buildValidationService {
        photos.forEachIndexed { index, photo ->
            field("photos[$index].storagePath", photo.storagePath) {
                required()
                max(500)
            }
            field("photos[$index].downloadUrl", photo.downloadUrl) { max(1000) }
            field("photos[$index].sortOrder", photo.sortOrder) { minValue(0) }
        }
    }.validate()
}
