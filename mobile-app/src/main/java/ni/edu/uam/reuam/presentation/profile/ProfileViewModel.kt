package ni.edu.uam.reuam.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.ApiException
import ni.edu.uam.reuam.data.remote.dto.UpsertUserProfileRequest
import ni.edu.uam.reuam.data.remote.dto.UserProfileResponse
import ni.edu.uam.reuam.data.repository.ProfileRepository

/**
 * Estado editable del formulario de perfil. Vive separado de ApiResult
 * porque mientras se está editando, la pantalla ya tiene datos cargados
 * (ApiResult.Success) pero el usuario está modificando campos localmente
 * antes de guardar.
 */
data class ProfileFormState(
    val displayName: String = "",
    val phoneNumber: String = "",
    val career: String = "",
    val studentCode: String = "",
    val bio: String = "",
) {
    companion object {
        fun fromProfile(profile: UserProfileResponse) = ProfileFormState(
            displayName = profile.displayName,
            phoneNumber = profile.phoneNumber.orEmpty(),
            career = profile.career.orEmpty(),
            studentCode = profile.studentCode.orEmpty(),
            bio = profile.bio.orEmpty(),
        )
    }
}

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository(),
) : ViewModel() {

    private val _profileState = MutableStateFlow<ApiResult<UserProfileResponse>>(ApiResult.Loading)
    val profileState: StateFlow<ApiResult<UserProfileResponse>> = _profileState.asStateFlow()

    private val _formState = MutableStateFlow(ProfileFormState())
    val formState: StateFlow<ProfileFormState> = _formState.asStateFlow()

    /** true mientras se está guardando una edición (distinto de la carga inicial). */
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ApiResult.Loading
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                val profile = repository.getOrCreateMyProfile(firebaseUser)
                _profileState.value = ApiResult.Success(profile)
                _formState.value = ProfileFormState.fromProfile(profile)
            } catch (e: ApiException) {
                _profileState.value = ApiResult.Error(e.message ?: "No se pudo cargar tu perfil.")
            }
        }
    }

    fun onDisplayNameChange(value: String) = _formState.update { it.copy(displayName = value) }
    fun onPhoneNumberChange(value: String) = _formState.update { it.copy(phoneNumber = value) }
    fun onCareerChange(value: String) = _formState.update { it.copy(career = value) }
    fun onStudentCodeChange(value: String) = _formState.update { it.copy(studentCode = value) }
    fun onBioChange(value: String) = _formState.update { it.copy(bio = value) }

    /**
     * Guarda los cambios del formulario en el backend.
     * [onSuccess] se llama solo si el guardado fue exitoso, útil para que
     * la pantalla muestre un Snackbar de confirmación.
     * [onError] recibe el mensaje listo para mostrar al usuario.
     */
    fun saveProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val form = _formState.value
                val updated = repository.updateMyProfile(
                    UpsertUserProfileRequest(
                        displayName = form.displayName.ifBlank { null },
                        phoneNumber = form.phoneNumber.ifBlank { null },
                        career = form.career.ifBlank { null },
                        studentCode = form.studentCode.ifBlank { null },
                        bio = form.bio.ifBlank { null },
                    )
                )
                _profileState.value = ApiResult.Success(updated)
                onSuccess()
            } catch (e: ApiException) {
                onError(e.message ?: "No se pudo guardar tu perfil.")
            } finally {
                _isSaving.value = false
            }
        }
    }
}
