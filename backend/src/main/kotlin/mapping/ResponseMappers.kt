package ni.uam.edu.mapping

import ni.uam.edu.models.Category
import ni.uam.edu.models.ExchangeRequest
import ni.uam.edu.models.Item
import ni.uam.edu.models.ItemPhoto
import ni.uam.edu.models.UserProfile
import ni.uam.edu.services.dto.CategoryResponse
import ni.uam.edu.services.dto.ExchangeRequestResponse
import ni.uam.edu.services.dto.ItemPhotoResponse
import ni.uam.edu.services.dto.ItemResponse
import ni.uam.edu.services.dto.UserProfileResponse

fun UserProfile.toResponse() = UserProfileResponse(
    id = id.toString(),
    firebaseUid = firebaseUid,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    phoneNumber = phoneNumber,
    career = career,
    studentCode = studentCode,
    bio = bio,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Category.toResponse() = CategoryResponse(
    id = id,
    name = name,
    slug = slug,
    createdAt = createdAt,
)

fun ItemPhoto.toResponse() = ItemPhotoResponse(
    id = id.toString(),
    itemId = itemId.toString(),
    storagePath = storagePath,
    downloadUrl = downloadUrl,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

fun Item.toResponse() = ItemResponse(
    id = id.toString(),
    ownerId = ownerId.toString(),
    title = title,
    description = description,
    categoryId = categoryId,
    condition = condition,
    transactionType = transactionType,
    priceCents = priceCents,
    status = status,
    location = location,
    createdAt = createdAt,
    updatedAt = updatedAt,
    photos = photos.sortedBy { it.sortOrder }.map { it.toResponse() },
)

fun ExchangeRequest.toResponse() = ExchangeRequestResponse(
    id = id.toString(),
    itemId = itemId.toString(),
    requesterId = requesterId.toString(),
    ownerId = ownerId.toString(),
    offerItemId = offerItemId?.toString(),
    message = message,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
