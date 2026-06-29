package ni.uam.edu.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.openapi.registerBearerAuthSecurityScheme
import com.kborowy.authprovider.firebase.firebase
import ni.uam.edu.models.AuthenticatedUser
import java.io.File
import java.nio.file.Files

const val FIREBASE_AUTH_PROVIDER = "firebase"
private const val FIREBASE_ADMIN_JSON_ENV = "FIREBASE_ADMIN_JSON"

fun Application.configureSecurity() {
    val adminFile = resolveFirebaseAdminFile()

    registerBearerAuthSecurityScheme(
        name = FIREBASE_AUTH_PROVIDER,
        description = "Firebase Authentication ID token sent as Authorization: Bearer <token>",
        bearerFormat = "Firebase ID token",
    )

    install(Authentication) {
        if (adminFile?.exists() == true) {
            firebase(FIREBASE_AUTH_PROVIDER) {
                setup {
                    this.adminFile = adminFile
                }
                realm = "ReUAM API"
                validate { token ->
                    AuthenticatedUser(
                        firebaseUid = token.uid,
                        email = token.email,
                        name = token.name,
                        picture = token.picture,
                    )
                }
            }
        } else {
            bearer(FIREBASE_AUTH_PROVIDER) {
                realm = "ReUAM API"
                authenticate {
                    null
                }
            }
        }
    }
}

private fun resolveFirebaseAdminFile(): File? {
    val jsonFromEnv = System.getenv(FIREBASE_ADMIN_JSON_ENV)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    if (jsonFromEnv != null) {
        val tempFile = Files.createTempFile("firebase-adminsdk", ".json").toFile()
        tempFile.writeText(jsonFromEnv)
        tempFile.deleteOnExit()
        return tempFile
    }

    return File("firebase-adminsdk.json").takeIf { it.exists() }
}
