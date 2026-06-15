package ni.uam.edu.models

import java.util.UUID

enum class ItemTransactionType {
    DONATION,
    EXCHANGE,
    LOAN,
    SYMBOLIC_SALE,
}

enum class ItemCondition {
    NEW,
    LIKE_NEW,
    GOOD,
    FAIR,
    NEEDS_REPAIR,
}

enum class ItemStatus {
    AVAILABLE,
    RESERVED,
    COMPLETED,
    INACTIVE,
}

enum class ExchangeRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    COMPLETED,
}

data class UserProfile(
    val id: UUID,
    val firebaseUid: String,
    val email: String?,
    val displayName: String,
    val photoUrl: String?,
    val phoneNumber: String?,
    val career: String?,
    val studentCode: String?,
    val bio: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class Category(
    val id: Int,
    val name: String,
    val slug: String,
    val createdAt: Long,
)

data class ItemPhoto(
    val id: UUID,
    val itemId: UUID,
    val storagePath: String,
    val downloadUrl: String?,
    val sortOrder: Int,
    val createdAt: Long,
)

data class Item(
    val id: UUID,
    val ownerId: UUID,
    val title: String,
    val description: String,
    val categoryId: Int?,
    val condition: ItemCondition,
    val transactionType: ItemTransactionType,
    val priceCents: Long?,
    val status: ItemStatus,
    val location: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val photos: List<ItemPhoto>,
)

data class ExchangeRequest(
    val id: UUID,
    val itemId: UUID,
    val requesterId: UUID,
    val ownerId: UUID,
    val offerItemId: UUID?,
    val message: String?,
    val status: ExchangeRequestStatus,
    val createdAt: Long,
    val updatedAt: Long,
)
