package ni.uam.edu.services

import io.ktor.server.application.Application
import ni.uam.edu.models.badRequest
import java.net.URI
import java.net.URLEncoder
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

private const val DEFAULT_PUBLIC_PATH = "/storage"

class LocalStorageService(
    private val rootDir: Path,
    private val publicBaseUrl: String,
    private val publicPath: String = DEFAULT_PUBLIC_PATH,
) {
    fun resolveProfilePhotoUrl(photoUrl: String): String =
        resolveReference(photoUrl)

    fun resolveItemPhotoDownloadUrl(storagePath: String, downloadUrl: String?): String =
        downloadUrl
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::resolveReference)
            ?: resolveReference(storagePath)

    private fun resolveReference(reference: String): String {
        val trimmed = reference.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        val relativePath = toStorageRelativePath(trimmed)
        return "${publicBaseUrl.trimEnd('/')}$publicPath/${encodePath(relativePath)}"
    }

    private fun toStorageRelativePath(reference: String): String {
        val withoutPublicBase = reference.removePrefix("${publicBaseUrl.trimEnd('/')}$publicPath/")
        val withoutPublicPath = withoutPublicBase
            .removePrefix("$publicPath/")
            .removePrefix(publicPath)
            .trimStart('/', '\\')

        val path = toPathOrNull(withoutPublicPath)
        if (path != null && path.isAbsolute) {
            val absolute = path.normalize()
            if (!absolute.startsWith(rootDir)) {
                throw badRequest("Local image paths must be inside ${rootDir.pathString}")
            }
            return normalizeRelative(rootDir.relativize(absolute).pathString)
        }

        return normalizeRelative(withoutPublicPath)
    }

    private fun normalizeRelative(rawPath: String): String {
        val normalized = rawPath.replace('\\', '/')
        val parts = normalized
            .split('/')
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "." && it != ".." }

        if (parts.isEmpty()) {
            throw badRequest("Image path cannot be empty")
        }

        return parts.joinToString("/")
    }

    private fun encodePath(relativePath: String): String =
        relativePath
            .split("/")
            .joinToString("/") { segment ->
                URLEncoder.encode(segment, Charsets.UTF_8).replace("+", "%20")
            }

    private fun toPathOrNull(reference: String): Path? =
        try {
            if (reference.startsWith("file:", ignoreCase = true)) {
                Paths.get(URI.create(reference))
            } else {
                Paths.get(reference)
            }
        } catch (_: IllegalArgumentException) {
            null
        }

    companion object {
        fun fromApplication(application: Application): LocalStorageService {
            val rootDir = application.getenvOrConfig("LOCAL_STORAGE_DIR", "storage.localDir")
            val publicBaseUrl = application.getenvOrConfig("LOCAL_STORAGE_PUBLIC_BASE_URL", "storage.publicBaseUrl")
            return LocalStorageService(
                rootDir = Paths.get(rootDir).toAbsolutePath().normalize(),
                publicBaseUrl = publicBaseUrl,
            )
        }
    }
}

internal fun Application.getenvOrConfig(envName: String, configPath: String): String =
    System.getenv(envName) ?: environment.config.property(configPath).getString()
