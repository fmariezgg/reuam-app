package ni.edu.uam.reuam.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.reuam.ui.theme.*

// Firebase Console → Project Settings → General → Your apps → Web API Key
// O bien Firebase Console → Authentication → Sign-in method → Google → Web client ID
private const val WEB_CLIENT_ID =
    "980022219203-t97g3dsp43kl5m691ctv9d7rs2vvj7ip.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    // Cuando el login es exitoso, navegar
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            authViewModel.resetState()
            onLoginSuccess()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "login_anim")
    val logoWobble by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f, label = "wobble",
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ReUAMGreenSoft, ReUAMBackground))),
        contentAlignment = Alignment.Center
    ) {
        // Decorativos de fondo
        Icon(
            Icons.Filled.Eco, null,
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = 40.dp)
                .rotate(45f)
                .alpha(0.08f),
            tint = ReUAMGreen
        )
        Icon(
            Icons.Filled.Recycling, null,
            modifier = Modifier
                .size(128.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = (-80).dp)
                .rotate(-12f)
                .alpha(0.08f),
            tint = ReUAMGreen
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo animado
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .rotate(logoWobble)
                    .background(ReUAMGreen, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Recycling, null, Modifier.size(56.dp), Color.White)
                Icon(
                    Icons.Filled.Eco, null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp),
                    tint = ReUAMGreenLight
                )
            }

            // Tarjeta de login
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ReUAMSurface,
                shadowElevation = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ReUAMGreenSoft, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Título
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Bienvenido a ReUAM",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ReUAMTextPrimary
                        )
                        Text(
                            "Ingresa con tu cuenta institucional para acceder a la comunidad.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReUAMTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Botón Google — muestra spinner mientras carga
                    OutlinedButton(
                        onClick = {
                            authViewModel.signInWithGoogle(context, WEB_CLIENT_ID)
                        },
                        enabled = uiState !is AuthUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ReUAMSurface,
                            contentColor = ReUAMTextPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ReUAMGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "G", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4285F4)
                                )
                                Text(
                                    "Continuar con Google",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ReUAMTextPrimary
                                )
                            }
                        }
                    }

                    // Mensaje de error (si hay)
                    if (uiState is AuthUiState.Error) {
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Aviso de seguridad
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ReUAMGreenSoft, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.Shield, null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp),
                            tint = ReUAMGreen
                        )
                        Text(
                            text = "Solo disponible para cuentas institucionales UAM. " +
                                    "Tu información está protegida y solo se usa para verificar tu identidad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReUAMTextSecondary
                        )
                    }
                }
            }

            // Footer legal
            Text(
                "Al continuar, aceptas nuestros términos de servicio y política de privacidad",
                style = MaterialTheme.typography.bodySmall,
                color = ReUAMTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.7f)
            )
        }
    }
}