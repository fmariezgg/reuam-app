package ni.edu.uam.reuam.presentation.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.navigation.Routes
import ni.edu.uam.reuam.presentation.components.*
import ni.edu.uam.reuam.ui.theme.*

// ── Datos de muestra ──────────────────────────────────────────────────────────

private data class RequestData(
    val id: Int,
    val articleTitle: String,
    val userName: String,
    val message: String,
    val status: RequestStatus
)

private val mockReceived = listOf(
    RequestData(1,
        "Cálculo I - 9na edición", "María González",
        "Hola, estoy interesada en este libro para mi curso de matemáticas. ¿Está disponible?",
        RequestStatus.PENDING),
    RequestData(2,
        "Cálculo I - 9na edición", "Carlos Ramírez",
        "Me gustaría obtener este libro. ¿Podemos coordinar en biblioteca?",
        RequestStatus.PENDING),
    RequestData(3,
        "Calculadora científica Casio", "Ana Martínez",
        "¿Podría prestarme la calculadora por una semana? Tengo examen el próximo lunes.",
        RequestStatus.PENDING),
)

private val mockSent = listOf(
    RequestData(4,
        "Física II - Serway", "Roberto López",
        "Hola, necesito este libro para mi curso. ¿Está en buen estado?",
        RequestStatus.ACCEPTED),
    RequestData(5,
        "Apuntes de Química Orgánica", "Laura Pérez",
        "Me interesan los apuntes para estudiar. Gracias.",
        RequestStatus.PENDING),
)

// ── Pantalla ──────────────────────────────────────────────────────────────────

@Composable
fun RequestsScreen(
    currentRoute: String = Routes.Requests.route,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(RequestType.RECEIVED) }

    // Estado mutable para aceptar/rechazar localmente
    var receivedRequests by remember { mutableStateOf(mockReceived) }

    val displayList = when (activeTab) {
        RequestType.RECEIVED -> receivedRequests
        RequestType.SENT     -> mockSent
    }

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
        ) {

            // ── HEADER ────────────────────────────────────────────────────────
            Surface(color = ReUAMSurface, shadowElevation = 2.dp) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás",
                                tint = ReUAMTextPrimary)
                        }
                        Text(
                            "Solicitudes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ReUAMTextPrimary
                        )
                    }

                    // ── Tabs Recibidas / Enviadas ─────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            RequestType.RECEIVED to "Recibidas",
                            RequestType.SENT     to "Enviadas"
                        ).forEach { (type, label) ->
                            val isActive = activeTab == type
                            Surface(
                                onClick = { activeTab = type },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = CircleShape,
                                color = if (isActive) ReUAMGreen else ReUAMGreenSoft
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        label,
                                        color = if (isActive) Color.White else ReUAMGreen,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── LISTA ─────────────────────────────────────────────────────────
            if (displayList.isEmpty()) {
                EmptyRequestsPlaceholder(
                    message = if (activeTab == RequestType.RECEIVED)
                        "No tienes solicitudes recibidas en este momento."
                    else
                        "No tienes solicitudes enviadas en este momento."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayList, key = { it.id }) { req ->
                        RequestCard(
                            articleTitle = req.articleTitle,
                            userName     = req.userName,
                            message      = req.message,
                            status       = req.status,
                            type         = activeTab,
                            onAccept = {
                                receivedRequests = receivedRequests.map {
                                    if (it.id == req.id) it.copy(status = RequestStatus.ACCEPTED) else it
                                }
                            },
                            onReject = {
                                receivedRequests = receivedRequests.map {
                                    if (it.id == req.id) it.copy(status = RequestStatus.REJECTED) else it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRequestsPlaceholder(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(ReUAMGreenSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Inbox, null,
                Modifier.size(48.dp), ReUAMGreenLight)
        }
        Spacer(Modifier.height(16.dp))
        Text("No hay solicitudes",
            fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ReUAMTextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(message,
            style = MaterialTheme.typography.bodyMedium,
            color = ReUAMTextSecondary,
            textAlign = TextAlign.Center)
    }
}
