package ni.uam.edu.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*

fun Application.configureHttp() {
    routing {
        openAPI(path = "openapi") {
            // Route metadata is supplied with describe { } blocks in routes/ReuamRoutes.kt.
        }
    }
}
