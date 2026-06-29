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
import ni.uam.edu.services.LocalStorageService
import ni.uam.edu.services.ReuamServices
import ni.uam.edu.services.UserProfileService
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import java.net.URI
import java.net.URLDecoder

suspend fun Application.configureDatabase() {
    val connectionConfig = databaseConnectionConfig()
    val database = R2dbcDatabase.connect(
        url = connectionConfig.url,
        user = connectionConfig.user,
        password = connectionConfig.password,
    )

    ReuamSchema(database).create()

    val profileRepository = ExposedUserProfileRepository(database)
    val categoryRepository = ExposedCategoryRepository(database)
    val itemRepository = ExposedItemRepository(database)
    val exchangeRequestRepository = ExposedExchangeRequestRepository(database)
    val localStorage = LocalStorageService.fromApplication(this)

    configureReuamRoutes(
        ReuamServices(
            profiles = UserProfileService(profileRepository, localStorage),
            categories = CategoryService(categoryRepository),
            items = ItemService(itemRepository, categoryRepository, localStorage),
            exchangeRequests = ExchangeRequestService(exchangeRequestRepository, itemRepository),
            localStorage = localStorage,
        )
    )
}

private fun Application.getenvOrConfig(envName: String, configPath: String): String =
    System.getenv(envName)
        ?: environment.config.property(configPath).getString()

private data class DatabaseConnectionConfig(
    val url: String,
    val user: String,
    val password: String,
)

private fun Application.databaseConnectionConfig(): DatabaseConnectionConfig {
    val rawUrl = getenvOrConfig("DATABASE_URL", "database.url")
    val configuredUser = System.getenv("DATABASE_USER")
    val configuredPassword = System.getenv("DATABASE_PASSWORD")

    val railwayPostgres = rawUrl.toRailwayPostgresConfig()
    return DatabaseConnectionConfig(
        url = railwayPostgres?.url ?: rawUrl,
        user = configuredUser
            ?: railwayPostgres?.user?.takeIf { it.isNotBlank() }
            ?: environment.config.property("database.user").getString(),
        password = configuredPassword
            ?: railwayPostgres?.password?.takeIf { it.isNotBlank() }
            ?: environment.config.property("database.password").getString(),
    )
}

private fun String.toRailwayPostgresConfig(): DatabaseConnectionConfig? {
    val normalized = when {
        startsWith("postgresql://") -> this
        startsWith("postgres://") -> replaceFirst("postgres://", "postgresql://")
        startsWith("jdbc:postgresql://") -> removePrefix("jdbc:")
        else -> return null
    }

    val uri = URI.create(normalized)
    val authority = uri.host + if (uri.port > 0) ":${uri.port}" else ""
    val query = uri.rawQuery?.let { "?$it" }.orEmpty()
    val r2dbcUrl = "r2dbc:postgresql://$authority${uri.rawPath.orEmpty()}$query"
    val userInfo = uri.rawUserInfo.orEmpty()
    val user = userInfo.substringBefore(':', missingDelimiterValue = "")
        .takeIf { it.isNotEmpty() }
        ?.decodeUrl()
    val password = userInfo.substringAfter(':', missingDelimiterValue = "")
        .takeIf { it.isNotEmpty() }
        ?.decodeUrl()

    return DatabaseConnectionConfig(
        url = r2dbcUrl,
        user = user.orEmpty(),
        password = password.orEmpty(),
    )
}

private fun String.decodeUrl(): String =
    URLDecoder.decode(this, Charsets.UTF_8)
