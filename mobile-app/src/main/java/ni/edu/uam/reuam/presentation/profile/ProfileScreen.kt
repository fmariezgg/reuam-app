package ni.edu.uam.reuam.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.presentation.components.ReUAMTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val profileState by viewModel.profileState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = profileState) {
                is ApiResult.Loading -> LoadingContent(message = "Cargando tu perfil...")

                is ApiResult.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadProfile() }
                )

                is ApiResult.Success -> ProfileForm(
                    photoUrl = state.data.photoUrl,
                    email = state.data.email,
                    formState = formState,
                    isSaving = isSaving,
                    onDisplayNameChange = viewModel::onDisplayNameChange,
                    onPhoneNumberChange = viewModel::onPhoneNumberChange,
                    onCareerChange = viewModel::onCareerChange,
                    onStudentCodeChange = viewModel::onStudentCodeChange,
                    onBioChange = viewModel::onBioChange,
                    onSaveClick = {
                        viewModel.saveProfile(
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                                }
                            },
                            onError = { message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileForm(
    photoUrl: String?,
    email: String?,
    formState: ProfileFormState,
    isSaving: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onCareerChange: (String) -> Unit,
    onStudentCodeChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatar(photoUrl = photoUrl)

        if (!email.isNullOrBlank()) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
        }

        ReUAMTextField(
            value = formState.displayName,
            onValueChange = onDisplayNameChange,
            label = "Nombre completo",
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ReUAMTextField(
            value = formState.career,
            onValueChange = onCareerChange,
            label = "Carrera",
            placeholder = "Ej. Ingeniería en Sistemas",
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ReUAMTextField(
            value = formState.studentCode,
            onValueChange = onStudentCodeChange,
            label = "Carnet / código de estudiante",
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ReUAMTextField(
            value = formState.phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = "Teléfono",
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ReUAMTextField(
            value = formState.bio,
            onValueChange = onBioChange,
            label = "Sobre mí",
            placeholder = "Cuéntale a otros estudiantes algo sobre ti",
            singleLine = false,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        } else {
            ReUAMButton(
                text = "Guardar cambios",
                onClick = onSaveClick,
            )
        }
    }
}

@Composable
private fun ProfileAvatar(photoUrl: String?) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
