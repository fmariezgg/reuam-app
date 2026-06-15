package ni.uam.edu.plugins

import io.ktor.server.application.Application
import ni.uam.edu.repositories.ExposedCategoryRepository
import ni.uam.edu.repositories.ExposedExchangeRequestRepository
import ni.uam.edu.repositories.ExposedItemRepository
import ni.uam.edu.repositories.ExposedUserProfileRepository
import ni.uam.edu.repositories.ReuamSchema
import ni.uam.edu.routes.configureReuamRoutes
import ni.uam.edu.services.CategoryService
import ni.uam.edu.services.ExchangeRequestService
import ni.uam.edu.services.ItemService
import ni.uam.edu.services.ReuamServices
import ni.uam.edu.services.UserProfileService
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

suspend fun Application.configureDatabase() {
    val database = R2dbcDatabase.connect(
        url = getenvOrConfig("DATABASE_URL", "database.url"),
        user = getenvOrConfig("DATABASE_USER", "database.user"),
        password = getenvOrConfig("DATABASE_PASSWORD", "database.password"),
    )

    ReuamSchema(database).create()

    val profileRepository = ExposedUserProfileRepository(database)
    val categoryRepository = ExposedCategoryRepository(database)
    val itemRepository = ExposedItemRepository(database)
    val exchangeRequestRepository = ExposedExchangeRequestRepository(database)

    configureReuamRoutes(
        ReuamServices(
            profiles = UserProfileService(profileRepository),
            categories = CategoryService(categoryRepository),
            items = ItemService(itemRepository, categoryRepository),
            exchangeRequests = ExchangeRequestService(exchangeRequestRepository, itemRepository),
        )
    )
}

private fun Application.getenvOrConfig(envName: String, configPath: String): String =
    System.getenv(envName)
        ?: environment.config.property(configPath).getString()
