package ni.edu.uam.reuam.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de GET /profiles/me y PUT /profiles/me.
 *
 * NOTA: "role" no existe todavía en el backend actual.
 * Se agregara despues junto con el panel de administrador.
 * Mientras no exista, Gson simplemente deja este campo en "STUDENT"
 * (su valor por defecto) sin que la app se rompa.
 */
data class UserProfileResponse(
    @SerializedName("id") val id: String,
    @SerializedName("firebaseUid") val firebaseUid: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("photoUrl") val photoUrl: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("career") val career: String? = null,
    @SerializedName("studentCode") val studentCode: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("role") val role: String = "STUDENT",
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("updatedAt") val updatedAt: Long,
)

/**
 * Cuerpo de PUT /profiles/me. Todos los campos son opcionales porque
 * el backend hace upsert: solo actualiza lo que mandes y conserva el resto.
 */
data class UpsertUserProfileRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("photoUrl") val photoUrl: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("career") val career: String? = null,
    @SerializedName("studentCode") val studentCode: String? = null,
    @SerializedName("bio") val bio: String? = null,
)

/** Cuerpo de PATCH /profiles/me/photo. */
data class UpdateProfilePhotoRequest(
    @SerializedName("photoUrl") val photoUrl: String,
)
