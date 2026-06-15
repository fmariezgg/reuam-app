package ni.uam.edu.models

data class AuthenticatedUser(
    val firebaseUid: String,
    val email: String? = null,
    val name: String? = null,
    val picture: String? = null,
)
