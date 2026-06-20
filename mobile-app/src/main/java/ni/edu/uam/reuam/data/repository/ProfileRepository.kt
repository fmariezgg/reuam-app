package ni.edu.uam.reuam.data.repository

import com.google.firebase.auth.FirebaseUser
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.remote.ReuamApiService
import ni.edu.uam.reuam.data.remote.RetrofitInstance
import ni.edu.uam.reuam.data.remote.dto.UpdateProfilePhotoRequest
import ni.edu.uam.reuam.data.remote.dto.UpsertUserProfileRequest
import ni.edu.uam.reuam.data.remote.dto.UserProfileResponse
import ni.edu.uam.reuam.data.remote.safeApiCall

class ProfileRepository(
    private val api: ReuamApiService = RetrofitInstance.api,
) {

    /**
     * Obtiene el perfil del usuario autenticado actual.
     *
     * Si el backend responde 404 (el usuario hizo login por primera vez y
     * todavía no tiene fila en la tabla de perfiles), lo crea automáticamente
     * con los datos básicos que ya trae Firebase (nombre, email, foto) usando
     * PUT /profiles/me, que el backend trata como upsert.
     *
     * [firebaseUser] se usa únicamente para ese caso de auto-creación;
     * si el perfil ya existe, no se usa para nada (el backend es la fuente
     * de verdad de los datos del perfil, no Firebase).
     */
    suspend fun getOrCreateMyProfile(firebaseUser: FirebaseUser?): UserProfileResponse {
        return try {
            safeApiCall { api.getMyProfile() }
        } catch (e: ApiException) {
            if (e.httpCode == 404 && firebaseUser != null) {
                createInitialProfile(firebaseUser)
            } else {
                throw e
            }
        }
    }

    private suspend fun createInitialProfile(firebaseUser: FirebaseUser): UserProfileResponse {
        val request = UpsertUserProfileRequest(
            email = firebaseUser.email,
            displayName = firebaseUser.displayName ?: "Estudiante UAM",
            photoUrl = firebaseUser.photoUrl?.toString(),
        )
        return safeApiCall { api.upsertMyProfile(request) }
    }

    suspend fun updateMyProfile(request: UpsertUserProfileRequest): UserProfileResponse =
        safeApiCall { api.upsertMyProfile(request) }

    suspend fun updateMyProfilePhoto(photoUrl: String): UserProfileResponse =
        safeApiCall { api.updateMyProfilePhoto(UpdateProfilePhotoRequest(photoUrl)) }
}