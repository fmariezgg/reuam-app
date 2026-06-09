package ni.edu.uam.reuam.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.ui.theme.*

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_anim")

    val leftOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f, label = "left",
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse)
    )
    val rightOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 10f, label = "right",
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 200), RepeatMode.Reverse)
    )
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f, label = "heart",
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
    )
    val recycleRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, label = "recycle",
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart)
    )
    val bubble1Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f, label = "b1",
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse)
    )
    val bubble2Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 10f, label = "b2",
        animationSpec = infiniteRepeatable(tween(3000, delayMillis = 500), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ReUAMBackground, ReUAMGreenSoft))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── ILUSTRACIÓN ──────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.widthIn(max = 360.dp)) {

                    // Burbujas decorativas
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.TopEnd)
                            .offset { IntOffset(x = 16.dp.roundToPx(), y = (-16).dp.roundToPx()) }
                            .offset(y = bubble1Y.dp)
                            .background(ReUAMAccent.copy(alpha = 0.2f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.BottomStart)
                            .offset { IntOffset(x = (-16).dp.roundToPx(), y = 16.dp.roundToPx()) }
                            .offset(y = bubble2Y.dp)
                            .background(ReUAMGreen.copy(alpha = 0.15f), CircleShape)
                    )

                    // Tarjeta principal
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = ReUAMSurface,
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, ReUAMGreen, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Fila de iconos animados
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Icono izquierdo
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .offset(y = leftOffset.dp)
                                        .background(
                                            Brush.linearGradient(listOf(ReUAMGreen, ReUAMGreenLight)),
                                            RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.People, null,
                                        Modifier.size(40.dp), Color.White
                                    )
                                }
                                // Corazón
                                Icon(
                                    Icons.Filled.Favorite, null,
                                    Modifier.size(32.dp * heartScale), ReUAMAccent
                                )
                                // Icono derecho
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .offset(y = rightOffset.dp)
                                        .background(
                                            Brush.linearGradient(listOf(ReUAMGreenLight, ReUAMGreen)),
                                            RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.MenuBook, null,
                                        Modifier.size(40.dp), Color.White
                                    )
                                }
                            }

                            // Ícono reciclaje rotante
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        Brush.linearGradient(listOf(ReUAMAccent, Color(0xFFFFA000))),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Recycling, null,
                                    Modifier.size(32.dp).rotate(recycleRotation), Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── TEXTO Y BOTONES ───────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dale una segunda vida a lo que ya no usas",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ReUAMTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Encuentra, dona, intercambia o presta artículos dentro de la comunidad UAM.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ReUAMTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                ReUAMButton(
                    text = "Comenzar",
                    onClick = onStartClick,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Iniciar sesión",
                        color = ReUAMGreen,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}