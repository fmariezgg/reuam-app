package ni.edu.uam.reuam.data.remote.dto

import com.google.gson.annotations.SerializedName

// ───────────────────────────── Categorías ─────────────────────────────

data class CategoryResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("createdAt") val createdAt: Long,
)

// ───────────────────────────── Enums de Item ───────────────────────────
// Deben coincidir EXACTAMENTE (mismos nombres) con los enums de Kotlin
// del backend (ItemTransactionType, ItemCondition, ItemStatus), porque
// Gson serializa/deserializa enums por su nombre tal cual.

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

// ───────────────────────────── Fotos de Item ───────────────────────────

data class ItemPhotoInput(
    @SerializedName("storagePath") val storagePath: String,
    @SerializedName("downloadUrl") val downloadUrl: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int = 0,
)

data class ItemPhotoResponse(
    @SerializedName("id") val id: String,
    @SerializedName("itemId") val itemId: String,
    @SerializedName("storagePath") val storagePath: String,
    @SerializedName("downloadUrl") val downloadUrl: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("createdAt") val createdAt: Long,
)

// ───────────────────────────── Item ─────────────────────────────────────

data class CreateItemRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("condition") val condition: ItemCondition,
    @SerializedName("transactionType") val transactionType: ItemTransactionType,
    @SerializedName("priceCents") val priceCents: Long? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("photos") val photos: List<ItemPhotoInput> = emptyList(),
)

data class UpdateItemRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("condition") val condition: ItemCondition? = null,
    @SerializedName("transactionType") val transactionType: ItemTransactionType? = null,
    @SerializedName("priceCents") val priceCents: Long? = null,
    @SerializedName("status") val status: ItemStatus? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("photos") val photos: List<ItemPhotoInput>? = null,
)

data class ItemResponse(
    @SerializedName("id") val id: String,
    @SerializedName("ownerId") val ownerId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("condition") val condition: ItemCondition,
    @SerializedName("transactionType") val transactionType: ItemTransactionType,
    @SerializedName("priceCents") val priceCents: Long? = null,
    @SerializedName("status") val status: ItemStatus,
    @SerializedName("location") val location: String? = null,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("updatedAt") val updatedAt: Long,
    @SerializedName("photos") val photos: List<ItemPhotoResponse> = emptyList(),
)
