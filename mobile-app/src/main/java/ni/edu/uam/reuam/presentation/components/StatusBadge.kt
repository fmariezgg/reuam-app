package ni.edu.uam.reuam.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.ui.theme.*

enum class ArticleStatus(val label: String) {
    DISPONIBLE("Disponible"),
    RESERVADO("Reservado"),
    ENTREGADO("Entregado")
}

enum class ArticleType(val label: String) {
    DONACION("Donación"),
    INTERCAMBIO("Intercambio"),
    PRESTAMO("Préstamo"),
    VENTA_SIMBOLICA("Venta simbólica")
}

@Composable
fun StatusBadge(
    status: ArticleStatus,
    modifier: Modifier = Modifier,
    small: Boolean = false
) {
    val (bg, border, text) = when (status) {
        ArticleStatus.DISPONIBLE    -> Triple(ReUAMGreenSoft,            ReUAMGreenLight,              ReUAMGreen)
        ArticleStatus.RESERVADO     -> Triple(Color(0xFFFFF8E1),         Color(0xFFFFD54F),            Color(0xFFF9A825))
        ArticleStatus.ENTREGADO     -> Triple(Color(0xFFF5F5F5),         Color(0xFFE0E0E0),            Color(0xFF757575))
    }

    val hPad = if (small) 8.dp else 12.dp
    val vPad = if (small) 4.dp else 6.dp
    val fontSize = if (small) 11.sp else 13.sp

    Text(
        text = status.label,
        modifier = modifier
            .background(bg, CircleShape)
            .border(1.dp, border, CircleShape)
            .padding(horizontal = hPad, vertical = vPad),
        color = text,
        fontSize = fontSize,
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
    )
}