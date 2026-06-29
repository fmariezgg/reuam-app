package ni.uam.edu.repositories

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import ni.uam.edu.mapping.toCategory
import ni.uam.edu.mapping.toExchangeRequest
import ni.uam.edu.mapping.toItem
import ni.uam.edu.mapping.toItemPhoto
import ni.uam.edu.mapping.toUserProfile
import ni.uam.edu.models.Category
import ni.uam.edu.models.ExchangeRequest
import ni.uam.edu.models.ExchangeRequestStatus
import ni.uam.edu.models.Item
import ni.uam.edu.models.ItemPhoto
import ni.uam.edu.models.ItemStatus
import ni.uam.edu.models.UserProfile
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.UUID

interface UserProfileRepository {
    suspend fun findById(id: UUID): UserProfile?
    suspend fun findByFirebaseUid(firebaseUid: String): UserProfile?
    suspend fun upsert(profile: UserProfile): UserProfile
}

interface CategoryRepository {
    suspend fun create(category: Category): Category
    suspend fun list(): List<Category>
    suspend fun findById(id: Int): Category?
}

interface ItemRepository {
    suspend fun create(item: Item): Item
    suspend fun findById(id: UUID): Item?
    suspend fun list(ownerId: UUID? = null, status: ItemStatus? = ItemStatus.AVAILABLE): List<Item>
    suspend fun update(item: Item): Item
    suspend fun delete(id: UUID)
}

interface ExchangeRequestRepository {
    suspend fun create(request: ExchangeRequest): ExchangeRequest
    suspend fun findById(id: UUID): ExchangeRequest?
    suspend fun listSent(requesterId: UUID): List<ExchangeRequest>
    suspend fun listReceived(ownerId: UUID): List<ExchangeRequest>
    suspend fun updateStatus(id: UUID, status: ExchangeRequestStatus, updatedAt: Long): ExchangeRequest?
}

class ReuamSchema(private val database: R2dbcDatabase) {
    @Suppress("DEPRECATION")
    suspend fun create() {
        suspendTransaction(database) {
            // Useful for the local H2 database while the schema is still changing.
            SchemaUtils.createMissingTablesAndColumns(
                UserProfilesTable,
                CategoriesTable,
                ItemsTable,
                ItemPhotosTable,
                ExchangeRequestsTable,
            )
        }
    }
}

class ExposedUserProfileRepository(private val database: R2dbcDatabase) : UserProfileRepository {
    override suspend fun findById(id: UUID): UserProfile? = suspendTransaction(database) {
        UserProfilesTable.selectAll()
            .where { UserProfilesTable.id eq id.toString() }
            .map { it.toUserProfile() }
            .toList()
            .singleOrNull()
    }

    override suspend fun findByFirebaseUid(firebaseUid: String): UserProfile? = suspendTransaction(database) {
        UserProfilesTable.selectAll()
            .where { UserProfilesTable.firebaseUid eq firebaseUid }
            .map { it.toUserProfile() }
            .toList()
            .singleOrNull()
    }

    override suspend fun upsert(profile: UserProfile): UserProfile = suspendTransaction(database) {
        val exists = UserProfilesTable.selectAll()
            .where { UserProfilesTable.id eq profile.id.toString() }
            .map { it[UserProfilesTable.id] }
            .toList()
            .isNotEmpty()

        if (exists) {
            UserProfilesTable.update({ UserProfilesTable.id eq profile.id.toString() }) {
                it[firebaseUid] = profile.firebaseUid
                it[email] = profile.email
                it[displayName] = profile.displayName
                it[photoUrl] = profile.photoUrl
                it[phoneNumber] = profile.phoneNumber
                it[career] = profile.career
                it[studentCode] = profile.studentCode
                it[bio] = profile.bio
                it[updatedAt] = profile.updatedAt
            }
        } else {
            UserProfilesTable.insert {
                it[id] = profile.id.toString()
                it[firebaseUid] = profile.firebaseUid
                it[email] = profile.email
                it[displayName] = profile.displayName
                it[photoUrl] = profile.photoUrl
                it[phoneNumber] = profile.phoneNumber
                it[career] = profile.career
                it[studentCode] = profile.studentCode
                it[bio] = profile.bio
                it[createdAt] = profile.createdAt
                it[updatedAt] = profile.updatedAt
            }
        }

        profile
    }
}

class ExposedCategoryRepository(private val database: R2dbcDatabase) : CategoryRepository {
    override suspend fun create(category: Category): Category = suspendTransaction(database) {
        val result = CategoriesTable.insert {
            it[name] = category.name
            it[slug] = category.slug
            it[createdAt] = category.createdAt
        }
        category.copy(id = result[CategoriesTable.id])
    }

    override suspend fun list(): List<Category> = suspendTransaction(database) {
        CategoriesTable.selectAll()
            .map { it.toCategory() }
            .toList()
            .sortedBy { it.name }
    }

    override suspend fun findById(id: Int): Category? = suspendTransaction(database) {
        CategoriesTable.selectAll()
            .where { CategoriesTable.id eq id }
            .map { it.toCategory() }
            .toList()
            .singleOrNull()
    }
}

class ExposedItemRepository(private val database: R2dbcDatabase) : ItemRepository {
    override suspend fun create(item: Item): Item = suspendTransaction(database) {
        ItemsTable.insert {
            it[id] = item.id.toString()
            it[ownerId] = item.ownerId.toString()
            it[title] = item.title
            it[description] = item.description
            it[categoryId] = item.categoryId
            it[condition] = item.condition
            it[transactionType] = item.transactionType
            it[priceCents] = item.priceCents
            it[status] = item.status
            it[location] = item.location
            it[createdAt] = item.createdAt
            it[updatedAt] = item.updatedAt
        }
        replacePhotos(item.id, item.photos)
        item
    }

    override suspend fun findById(id: UUID): Item? = suspendTransaction(database) {
        val photos = findPhotosByItemId(id)
        ItemsTable.selectAll()
            .where { ItemsTable.id eq id.toString() }
            .map { it.toItem(photos) }
            .toList()
            .singleOrNull()
    }

    override suspend fun list(ownerId: UUID?, status: ItemStatus?): List<Item> = suspendTransaction(database) {
        val rows = when {
            ownerId != null && status != null -> ItemsTable.selectAll()
                .where { (ItemsTable.ownerId eq ownerId.toString()) and (ItemsTable.status eq status) }
            ownerId != null -> ItemsTable.selectAll()
                .where { ItemsTable.ownerId eq ownerId.toString() }
            status != null -> ItemsTable.selectAll()
                .where { ItemsTable.status eq status }
            else -> ItemsTable.selectAll()
        }.map { row ->
            val itemId = UUID.fromString(row[ItemsTable.id])
            row.toItem(findPhotosByItemId(itemId))
        }.toList()

        rows.sortedByDescending { it.createdAt }
    }

    override suspend fun update(item: Item): Item = suspendTransaction(database) {
        ItemsTable.update({ ItemsTable.id eq item.id.toString() }) {
            it[title] = item.title
            it[description] = item.description
            it[categoryId] = item.categoryId
            it[condition] = item.condition
            it[transactionType] = item.transactionType
            it[priceCents] = item.priceCents
            it[status] = item.status
            it[location] = item.location
            it[updatedAt] = item.updatedAt
        }
        replacePhotos(item.id, item.photos)
        item
    }

    override suspend fun delete(id: UUID) {
        suspendTransaction(database) {
            ItemPhotosTable.deleteWhere { itemId eq id.toString() }
            ItemsTable.deleteWhere { ItemsTable.id eq id.toString() }
        }
    }

    private suspend fun findPhotosByItemId(itemId: UUID): List<ItemPhoto> =
        ItemPhotosTable.selectAll()
            .where { ItemPhotosTable.itemId eq itemId.toString() }
            .map { it.toItemPhoto() }
            .toList()
            .sortedBy { it.sortOrder }

    private suspend fun replacePhotos(itemId: UUID, photos: List<ItemPhoto>) {
        ItemPhotosTable.deleteWhere { ItemPhotosTable.itemId eq itemId.toString() }
        photos.forEach { photo ->
            ItemPhotosTable.insert {
                it[id] = photo.id.toString()
                it[ItemPhotosTable.itemId] = itemId.toString()
                it[storagePath] = photo.storagePath
                it[downloadUrl] = photo.downloadUrl
                it[sortOrder] = photo.sortOrder
                it[createdAt] = photo.createdAt
            }
        }
    }
}

class ExposedExchangeRequestRepository(private val database: R2dbcDatabase) : ExchangeRequestRepository {
    override suspend fun create(request: ExchangeRequest): ExchangeRequest = suspendTransaction(database) {
        ExchangeRequestsTable.insert {
            it[id] = request.id.toString()
            it[itemId] = request.itemId.toString()
            it[requesterId] = request.requesterId.toString()
            it[ownerId] = request.ownerId.toString()
            it[offerItemId] = request.offerItemId?.toString()
            it[message] = request.message
            it[status] = request.status
            it[createdAt] = request.createdAt
            it[updatedAt] = request.updatedAt
        }
        request
    }

    override suspend fun findById(id: UUID): ExchangeRequest? = suspendTransaction(database) {
        ExchangeRequestsTable.selectAll()
            .where { ExchangeRequestsTable.id eq id.toString() }
            .map { it.toExchangeRequest() }
            .toList()
            .singleOrNull()
    }

    override suspend fun listSent(requesterId: UUID): List<ExchangeRequest> = suspendTransaction(database) {
        ExchangeRequestsTable.selectAll()
            .where { ExchangeRequestsTable.requesterId eq requesterId.toString() }
            .map { it.toExchangeRequest() }
            .toList()
            .sortedByDescending { it.createdAt }
    }

    override suspend fun listReceived(ownerId: UUID): List<ExchangeRequest> = suspendTransaction(database) {
        ExchangeRequestsTable.selectAll()
            .where { ExchangeRequestsTable.ownerId eq ownerId.toString() }
            .map { it.toExchangeRequest() }
            .toList()
            .sortedByDescending { it.createdAt }
    }

    override suspend fun updateStatus(
        id: UUID,
        status: ExchangeRequestStatus,
        updatedAt: Long,
    ): ExchangeRequest? = suspendTransaction(database) {
        ExchangeRequestsTable.update({ ExchangeRequestsTable.id eq id.toString() }) {
            it[ExchangeRequestsTable.status] = status
            it[ExchangeRequestsTable.updatedAt] = updatedAt
        }
        ExchangeRequestsTable.selectAll()
            .where { ExchangeRequestsTable.id eq id.toString() }
            .map { it.toExchangeRequest() }
            .toList()
            .singleOrNull()
    }
}
