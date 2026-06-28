package ni.uam.edu

import ni.uam.edu.services.LocalStorageService
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalStorageServiceTest {
    @Test
    fun `relative storage paths become public local urls`() {
        val rootDir = Files.createTempDirectory("reuam-storage-test")
        val storage = LocalStorageService(rootDir, "http://10.0.2.2:8080")

        val url = storage.resolveItemPhotoDownloadUrl(
            storagePath = "items/user-1/item-1/foto 1.jpg",
            downloadUrl = null,
        )

        assertEquals("http://10.0.2.2:8080/storage/items/user-1/item-1/foto%201.jpg", url)
    }

    @Test
    fun `external urls are preserved`() {
        val rootDir = Files.createTempDirectory("reuam-storage-test")
        val storage = LocalStorageService(rootDir, "http://10.0.2.2:8080")

        val url = storage.resolveProfilePhotoUrl("https://example.com/avatar.jpg")

        assertEquals("https://example.com/avatar.jpg", url)
    }

    @Test
    fun `absolute paths inside storage root become public local urls`() {
        val rootDir = Files.createTempDirectory("reuam-storage-test")
        val avatarPath = rootDir.resolve("profiles/user-1/avatar.jpg")
        val storage = LocalStorageService(rootDir, "http://10.0.2.2:8080")

        val url = storage.resolveProfilePhotoUrl(avatarPath.toString())

        assertEquals("http://10.0.2.2:8080/storage/profiles/user-1/avatar.jpg", url)
    }
}
