package ni.uam.edu.repositories

import ni.uam.edu.models.ExchangeRequestStatus
import ni.uam.edu.models.ItemCondition
import ni.uam.edu.models.ItemStatus
import ni.uam.edu.models.ItemTransactionType
import org.jetbrains.exposed.v1.core.Table

object UserProfilesTable : Table("user_profiles") {
    val id = varchar("id", 36)
    val firebaseUid = varchar("firebase_uid", 128).uniqueIndex()
    val email = varchar("email", 255).nullable()
    val displayName = varchar("display_name", 120)
    val photoUrl = text("photo_url").nullable()
    val phoneNumber = varchar("phone_number", 32).nullable()
    val career = varchar("career", 120).nullable()
    val studentCode = varchar("student_code", 32).nullable()
    val bio = text("bio").nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object CategoriesTable : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 80).uniqueIndex()
    val slug = varchar("slug", 100).uniqueIndex()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object ItemsTable : Table("items") {
    val id = varchar("id", 36)
    val ownerId = varchar("owner_id", 36)
    val title = varchar("title", 120)
    val description = text("description")
    val categoryId = integer("category_id").nullable()
    val condition = enumerationByName<ItemCondition>("condition", 32)
    val transactionType = enumerationByName<ItemTransactionType>("transaction_type", 32)
    val priceCents = long("price_cents").nullable()
    val status = enumerationByName<ItemStatus>("status", 32)
    val location = varchar("location", 160).nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object ItemPhotosTable : Table("item_photos") {
    val id = varchar("id", 36)
    val itemId = varchar("item_id", 36)
    val storagePath = text("storage_path")
    val downloadUrl = text("download_url").nullable()
    val sortOrder = integer("sort_order")
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object ExchangeRequestsTable : Table("exchange_requests") {
    val id = varchar("id", 36)
    val itemId = varchar("item_id", 36)
    val requesterId = varchar("requester_id", 36)
    val ownerId = varchar("owner_id", 36)
    val offerItemId = varchar("offer_item_id", 36).nullable()
    val message = text("message").nullable()
    val status = enumerationByName<ExchangeRequestStatus>("status", 32)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}
