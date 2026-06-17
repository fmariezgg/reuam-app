package ni.edu.uam.reuam.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.navigation.Routes
import ni.edu.uam.reuam.presentation.components.ReUAMBottomBar
import ni.edu.uam.reuam.ui.theme.*

// ── Datos de la pantalla ──────────────────────────────────────────────────────

private data class UserStat(
    val icon: ImageVector,
    val label: String,
    val value: String
)

private data class MenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String? = null
)

// ── Pantalla ──────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    currentRoute: String = Routes.Profile.route,
    onNavigate: (String) -> Unit = {},
    onMyPublications: () -> Unit = {},
    onRequests: () -> Unit = {},
    onLogout: () -> Unit = {},
    // Datos del usuario — conectar con AuthViewModel cuando esté listo
    userName: String = "Fatima Zogaib",
    userEmail: String = "fzogaib@uam.edu.ni",
    userRole: String = "Estudiante"
) {
    val stats = listOf(
        UserStat(Icons.Outlined.Inventory2,  "Artículos publicados",  "5"),
        UserStat(Icons.Filled.Recycling,     "Artículos reutilizados", "12"),
        UserStat(Icons.AutoMirrored.Filled.Send, "Solicitudes realizadas", "8")
    )

    val menuItems = listOf(
        MenuItem(Icons.Outlined.Inventory2,   "Mis publicaciones"),
        MenuItem(Icons.Outlined.Description,  "Solicitudes"),
        MenuItem(Icons.Outlined.Settings,     "Configuración")
    )

    Scaffold(
        bottomBar = {
            ReUAMBottomBar(currentRoute = currentRoute, onNavigate = onNavigate)
        },
        containerColor = ReUAMBackground
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            // ── HEADER ────────────────────────────────────────────────────────
            Surface(color = ReUAMSurface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigate(Routes.Home.route) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = ReUAMTextPrimary
                        )
                    }
                    Text(
                        text = "Perfil",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ReUAMTextPrimary
                    )
                }
            }

            // ── TARJETA DE USUARIO ────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .border(1.dp, ReUAMGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = ReUAMSurface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar con gradiente
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ReUAMGreen, ReUAMGreenLight))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ReUAMTextPrimary
                    )

                    Spacer(Modifier.height(6.dp))

                    // Email
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Email, null,
                            Modifier.size(16.dp), ReUAMTextSecondary)
                        Text(userEmail, fontSize = 13.sp, color = ReUAMTextSecondary)
                    }

                    Spacer(Modifier.height(4.dp))

                    // Rol
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.School, null,
                            Modifier.size(16.dp), ReUAMTextSecondary)
                        Text(userRole, fontSize = 13.sp, color = ReUAMTextSecondary)
                    }
                }
            }

            // ── ESTADÍSTICAS ──────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Estadísticas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ReUAMTextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    stats.forEach { stat ->
                        StatCard(stat = stat, modifier = Modifier.weight(1f))
                    }
                }
            }

            // ── MENÚ ──────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Menú",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ReUAMTextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ReUAMSurface,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ReUAMGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        menuItems.forEachIndexed { index, item ->
                            MenuRow(
                                icon = item.icon,
                                label = item.label,
                                showDivider = index != menuItems.lastIndex,
                                onClick = {
                                    when (item.label) {
                                        "Mis publicaciones" -> onMyPublications()
                                        "Solicitudes"       -> onRequests()
                                        else -> { /* Configuración — pendiente */ }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ── CERRAR SESIÓN ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 24.dp)
            ) {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ReUAMError
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ReUAMError)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null,
                            Modifier.size(20.dp), ReUAMError)
                        Text(
                            "Cerrar sesión",
                            style = MaterialTheme.typography.labelLarge,
                            color = ReUAMError
                        )
                    }
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun StatCard(stat: UserStat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .border(1.dp, ReUAMGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = ReUAMSurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(ReUAMGreenSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(stat.icon, null, Modifier.size(22.dp), ReUAMGreen)
            }
            Text(
                text = stat.value,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = ReUAMGreen
            )
            Text(
                text = stat.label,
                fontSize = 11.sp,
                color = ReUAMTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ReUAMGreenSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(20.dp), ReUAMGreen)
            }
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = ReUAMTextPrimary
            )
            Icon(Icons.Filled.ChevronRight, null,
                Modifier.size(20.dp), ReUAMTextSecondary)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = ReUAMGreen.copy(alpha = 0.10f)
            )
        }
    }
}