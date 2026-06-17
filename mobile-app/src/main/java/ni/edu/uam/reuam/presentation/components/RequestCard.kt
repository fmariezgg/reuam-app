package ni.edu.uam.reuam.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.ui.theme.*

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class RequestStatus(val label: String) {
    PENDING("Pendiente"),
    ACCEPTED("Aceptada"),
    REJECTED("Rechazada"),
    CANCELLED("Cancelada")
}

enum class RequestType { RECEIVED, SENT }

// ── Colores y textos por estado ───────────────────────────────────────────────

private data class StatusStyle(
    val background: Color,
    val border: Color,
    val text: Color
)

private fun requestStatusStyle(status: RequestStatus): StatusStyle = when (status) {
    RequestStatus.PENDING   -> StatusStyle(ReUAMGreenSoft, ReUAMGreenLight, ReUAMGreen)
    RequestStatus.ACCEPTED  -> StatusStyle(Color(0xFFFFF8E1), Color(0xFFFFD54F), Color(0xFFF9A825))
    RequestStatus.REJECTED,
    RequestStatus.CANCELLED -> StatusStyle(Color(0xFFF5F5F5), Color(0xFFE0E0E0), Color(0xFF757575))
}

// ── Componente ────────────────────────────────────────────────────────────────

@Composable
fun RequestCard(
    articleTitle: String,
    userName: String,
    message: String,
    status: RequestStatus,
    type: RequestType,
    modifier: Modifier = Modifier,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    val style = requestStatusStyle(status)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ReUAMGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = ReUAMSurface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Título + badge de estado ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = articleTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ReUAMTextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = ReUAMTextSecondary
                        )
                        Text(
                            text = userName,
                            fontSize = 13.sp,
                            color = ReUAMTextSecondary
                        )
                    }
                }

                // Badge estado
                Text(
                    text = status.label,
                    modifier = Modifier
                        .background(style.background, CircleShape)
                        .border(1.dp, style.border, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    color = style.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // ── Mensaje ───────────────────────────────────────────────────────
            if (message.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ReUAMGreenSoft.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Message,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp),
                        tint = ReUAMGreen
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = ReUAMTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

            // ── Botones Aceptar / Rechazar (solo recibidas + pendientes) ──────
            if (type == RequestType.RECEIVED && status == RequestStatus.PENDING) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Aceptar
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ReUAMGreen,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Aceptar", style = MaterialTheme.typography.labelMedium)
                    }
                    // Rechazar
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ReUAMGreen
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Rechazar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}