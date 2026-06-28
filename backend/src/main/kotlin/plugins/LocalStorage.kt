package ni.uam.edu.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.routing
import ni.uam.edu.services.getenvOrConfig
import java.nio.file.Files
import java.nio.file.Paths

fun Application.configureLocalStorage() {
    val rootDir = Paths.get(getenvOrConfig("LOCAL_STORAGE_DIR", "storage.localDir"))
        .toAbsolutePath()
        .normalize()

    Files.createDirectories(rootDir)

    routing {
        staticFiles("/storage", rootDir.toFile())
    }
}
