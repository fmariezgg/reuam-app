package ni.uam.edu.mapping

import ni.uam.edu.models.Category
import ni.uam.edu.models.ExchangeRequest
import ni.uam.edu.models.Item
import ni.uam.edu.models.ItemPhoto
import ni.uam.edu.models.UserProfile
import ni.uam.edu.repositories.CategoriesTable
import ni.uam.edu.repositories.ExchangeRequestsTable
import ni.uam.edu.repositories.ItemPhotosTable
import ni.uam.edu.repositories.ItemsTable
import ni.uam.edu.repositories.UserProfilesTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.util.UUID

fun ResultRow.toUserProfile() = UserProfile(
    id = this[UserProfilesTable.id].toUuid(),
    firebaseUid = this[UserProfilesTable.firebaseUid],
    email = this[UserProfilesTable.email],
    displayName = this[UserProfilesTable.displayName],
    photoUrl = this[UserProfilesTable.photoUrl],
    phoneNumber = this[UserProfilesTable.phoneNumber],
    career = this[UserProfilesTable.career],
    studentCode = this[UserProfilesTable.studentCode],
    bio = this[UserProfilesTable.bio],
    createdAt = this[UserProfilesTable.createdAt],
    updatedAt = this[UserProfilesTable.updatedAt],
)

fun ResultRow.toCategory() = Category(
    id = this[CategoriesTable.id],
    name = this[CategoriesTable.name],
    slug = this[CategoriesTable.slug],
    createdAt = this[CategoriesTable.createdAt],
)

fun ResultRow.toItemPhoto() = ItemPhoto(
    id = this[ItemPhotosTable.id].toUuid(),
    itemId = this[ItemPhotosTable.itemId].toUuid(),
    storagePath = this[ItemPhotosTable.storagePath],
    downloadUrl = this[ItemPhotosTable.downloadUrl],
    sortOrder = this[ItemPhotosTable.sortOrder],
    createdAt = this[ItemPhotosTable.createdAt],
)

fun ResultRow.toItem(photos: List<ItemPhoto> = emptyList()) = Item(
    id = this[ItemsTable.id].toUuid(),
    ownerId = this[ItemsTable.ownerId].toUuid(),
    title = this[ItemsTable.title],
    description = this[ItemsTable.description],
    categoryId = this[ItemsTable.categoryId],
    condition = this[ItemsTable.condition],
    transactionType = this[ItemsTable.transactionType],
    priceCents = this[ItemsTable.priceCents],
    status = this[ItemsTable.status],
    location = this[ItemsTable.location],
    createdAt = this[ItemsTable.createdAt],
    updatedAt = this[ItemsTable.updatedAt],
    photos = photos,
)

fun ResultRow.toExchangeRequest() = ExchangeRequest(
    id = this[ExchangeRequestsTable.id].toUuid(),
    itemId = this[ExchangeRequestsTable.itemId].toUuid(),
    requesterId = this[ExchangeRequestsTable.requesterId].toUuid(),
    ownerId = this[ExchangeRequestsTable.ownerId].toUuid(),
    offerItemId = this[ExchangeRequestsTable.offerItemId]?.toUuid(),
    message = this[ExchangeRequestsTable.message],
    status = this[ExchangeRequestsTable.status],
    createdAt = this[ExchangeRequestsTable.createdAt],
    updatedAt = this[ExchangeRequestsTable.updatedAt],
)

private fun String.toUuid(): UUID = UUID.fromString(this)
