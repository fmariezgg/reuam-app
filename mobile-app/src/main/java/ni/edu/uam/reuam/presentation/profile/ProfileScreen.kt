package ni.edu.uam.reuam.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.presentation.components.ReUAMTextField
import ni.edu.uam.reuam.ui.theme.ReUAMBackground
import ni.edu.uam.reuam.ui.theme.ReUAMError
import ni.edu.uam.reuam.ui.theme.ReUAMGreen
import ni.edu.uam.reuam.ui.theme.ReUAMGreenLight
import ni.edu.uam.reuam.ui.theme.ReUAMGreenSoft
import ni.edu.uam.reuam.ui.theme.ReUAMSurface
import ni.edu.uam.reuam.ui.theme.ReUAMTextPrimary
import ni.edu.uam.reuam.ui.theme.ReUAMTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMyPublicationsClick: () -> Unit = {},
    onRequestsClick: () -> Unit = {},
    onAdminPanelClick: () -> Unit = {},
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
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ReUAMBackground,
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

                is ApiResult.Success -> ProfileContent(
                    photoUrl = state.data.photoUrl,
                    email = state.data.email,
                    role = state.data.role,
                    formState = formState,
                    isSaving = isSaving,
                    onDisplayNameChange = viewModel::onDisplayNameChange,
                    onPhoneNumberChange = viewModel::onPhoneNumberChange,
                    onCareerChange = viewModel::onCareerChange,
                    onStudentCodeChange = viewModel::onStudentCodeChange,
                    onBioChange = viewModel::onBioChange,
                    onMyPublicationsClick = onMyPublicationsClick,
                    onRequestsClick = onRequestsClick,
                    onAdminPanelClick = onAdminPanelClick,
                    onLogoutClick = onLogoutClick,
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
private fun ProfileContent(
    photoUrl: String?,
    email: String?,
    role: String,
    formState: ProfileFormState,
    isSaving: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onCareerChange: (String) -> Unit,
    onStudentCodeChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onMyPublicationsClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ProfileHeaderCard(
            photoUrl = photoUrl,
            displayName = formState.displayName,
            email = email,
            career = formState.career,
        )

        SectionTitle("Estadísticas")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProfileStatCard(
                title = "Publicados",
                value = "--",
                icon = { Icon(Icons.Filled.Article, contentDescription = null) },
                modifier = Modifier.weight(1f),
            )
            ProfileStatCard(
                title = "Solicitudes",
                value = "--",
                icon = { Icon(Icons.Filled.Send, contentDescription = null) },
                modifier = Modifier.weight(1f),
            )
            ProfileStatCard(
                title = "Reutilizados",
                value = "--",
                icon = { Icon(Icons.Filled.School, contentDescription = null) },
                modifier = Modifier.weight(1f),
            )
        }

        SectionTitle("Menú")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            ProfileMenuItem(
                icon = { Icon(Icons.Filled.Article, contentDescription = null) },
                title = "Mis publicaciones",
                subtitle = "Gestiona los artículos que compartiste",
                onClick = onMyPublicationsClick,
            )
            HorizontalDivider(color = ReUAMGreenSoft)
            ProfileMenuItem(
                icon = { Icon(Icons.Filled.Send, contentDescription = null) },
                title = "Solicitudes",
                subtitle = "Revisa solicitudes enviadas y recibidas",
                onClick = onRequestsClick,
            )
            if (role.equals("ADMIN", ignoreCase = true)) {
                HorizontalDivider(color = ReUAMGreenSoft)
                ProfileMenuItem(
                    icon = { Icon(Icons.Filled.Security, contentDescription = null) },
                    title = "Panel administrador",
                    subtitle = "Gestiona reportes, categorías y actividad",
                    onClick = onAdminPanelClick,
                )
            }
        }

        SectionTitle("Información personal")
        ProfileEditCard(
            formState = formState,
            isSaving = isSaving,
            onDisplayNameChange = onDisplayNameChange,
            onPhoneNumberChange = onPhoneNumberChange,
            onCareerChange = onCareerChange,
            onStudentCodeChange = onStudentCodeChange,
            onBioChange = onBioChange,
            onSaveClick = onSaveClick,
        )

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = ReUAMError,
            )
            Text(
                text = "Cerrar sesión",
                modifier = Modifier.padding(start = 8.dp),
                color = ReUAMError,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileHeaderCard(
    photoUrl: String?,
    displayName: String,
    email: String?,
    career: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatar(photoUrl = photoUrl)

            Text(
                text = displayName.ifBlank { "Estudiante UAM" },
                style = MaterialTheme.typography.titleLarge,
                color = ReUAMTextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )

            if (!email.isNullOrBlank()) {
                ProfileInfoLine(
                    icon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    text = email,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            ProfileInfoLine(
                icon = { Icon(Icons.Filled.School, contentDescription = null) },
                text = career.ifBlank { "Estudiante" },
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ProfileInfoLine(
    icon: @Composable () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides ReUAMTextSecondary,
                content = icon,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = ReUAMTextSecondary,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = ReUAMTextPrimary,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ReUAMGreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides ReUAMGreen,
                    content = icon,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = ReUAMGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ReUAMTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ReUAMGreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides ReUAMGreen,
                    content = icon,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ReUAMTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ReUAMTextSecondary,
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = ReUAMTextSecondary,
            )
        }
    }
}

@Composable
private fun ProfileEditCard(
    formState: ProfileFormState,
    isSaving: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onCareerChange: (String) -> Unit,
    onStudentCodeChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReUAMTextField(
                value = formState.displayName,
                onValueChange = onDisplayNameChange,
                label = "Nombre visible",
                placeholder = "Tu nombre completo",
            )

            ReUAMTextField(
                value = formState.career,
                onValueChange = onCareerChange,
                label = "Carrera",
                placeholder = "Ej. Ingeniería en Sistemas",
            )

            ReUAMTextField(
                value = formState.studentCode,
                onValueChange = onStudentCodeChange,
                label = "Carnet / código",
                placeholder = "Ej. 2021-0042",
            )

            ReUAMTextField(
                value = formState.phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = "Teléfono",
                placeholder = "+505 8888-0000",
            )

            ReUAMTextField(
                value = formState.bio,
                onValueChange = onBioChange,
                label = "Sobre mí",
                placeholder = "Cuéntale a otros estudiantes algo sobre ti",
                singleLine = false,
            )

            if (isSaving) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            } else {
                ReUAMButton(
                    text = "Guardar cambios",
                    onClick = onSaveClick,
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatar(photoUrl: String?) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(ReUAMGreenLight),
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
                tint = ReUAMGreen,
            )
        }
    }
}
