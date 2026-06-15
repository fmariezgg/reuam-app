package ni.uam.edu.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.openapi.registerBearerAuthSecurityScheme
import java.io.File
import com.kborowy.authprovider.firebase.firebase
import ni.uam.edu.models.AuthenticatedUser

const val FIREBASE_AUTH_PROVIDER = "firebase"

fun Application.configureSecurity() {
    val myAdminFile = File("firebase-adminsdk.json")

    registerBearerAuthSecurityScheme(
        name = FIREBASE_AUTH_PROVIDER,
        description = "Firebase Authentication ID token sent as Authorization: Bearer <token>",
        bearerFormat = "Firebase ID token",
    )

    install(Authentication) {
        if (myAdminFile.exists()) {
            firebase(FIREBASE_AUTH_PROVIDER) {
                setup {
                    adminFile = myAdminFile
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
