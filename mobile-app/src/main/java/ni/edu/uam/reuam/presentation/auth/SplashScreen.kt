package ni.edu.uam.reuam.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.ui.theme.*

@Composable
fun SplashScreen(onFinish: () -> Unit) {

    // Animación principal — escala + alpha
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    // Rotación del logo
    val rotation = remember { Animatable(-180f) }

    // Puntos de carga
    val dot1Alpha = remember { Animatable(0.5f) }
    val dot2Alpha = remember { Animatable(0.5f) }
    val dot3Alpha = remember { Animatable(0.5f) }

    // Texto alpha
    val textAlpha = remember { Animatable(0f) }
    val footerAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo aparece
        launch {
            scale.animateTo(1f, animationSpec = tween(600, easing = EaseOut))
        }
        launch {
            alpha.animateTo(1f, animationSpec = tween(600))
        }
        // Rotación del logo
        launch {
            rotation.animateTo(0f, animationSpec = tween(800, easing = EaseOut, delayMillis = 200))
        }
        // Texto
        launch {
            delay(500)
            textAlpha.animateTo(1f, animationSpec = tween(600))
        }
        // Footer
        launch {
            delay(1500)
            footerAlpha.animateTo(1f, animationSpec = tween(600))
        }
        // Dots pulsantes
        launch {
            delay(1000)
            while (true) {
                dot1Alpha.animateTo(1f, tween(300)); dot1Alpha.animateTo(0.5f, tween(300))
                dot2Alpha.animateTo(1f, tween(300)); dot2Alpha.animateTo(0.5f, tween(300))
                dot3Alpha.animateTo(1f, tween(300)); dot3Alpha.animateTo(0.5f, tween(300))
            }
        }
        // Navegar
        delay(2500)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ReUAMGreenSoft, ReUAMBackground, ReUAMGreenLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(ReUAMGreen, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Recycling,
                    contentDescription = "ReUAM Logo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(rotation.value)
                )
            }

            // Nombre y slogan
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = "ReUAM",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReUAMGreen
                )
                Text(
                    text = "Reutiliza, comparte y transforma tu campus",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ReUAMTextSecondary
                )
            }

            // Puntos de carga
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { dotAlpha ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(dotAlpha.value)
                            .background(ReUAMGreen, RoundedCornerShape(50))
                    )
                }
            }
        }

        // Footer
        Text(
            text = "Universidad Americana UAM Nicaragua",
            style = MaterialTheme.typography.bodySmall,
            color = ReUAMTextSecondary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(footerAlpha.value)
        )
    }
}
