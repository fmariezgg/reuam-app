package ni.edu.uam.reuam.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ni.edu.uam.reuam.ui.theme.*

@Composable
fun ArticleCard(
    imageUrl: String,
    title: String,
    category: String,
    type: ArticleType,
    status: ArticleStatus,
    owner: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val typeColor = when (type) {
        ArticleType.DONACION        -> ReUAMGreen
        ArticleType.INTERCAMBIO     -> Color(0xFFF9A825)
        ArticleType.PRESTAMO        -> Color(0xFF1976D2)
        ArticleType.VENTA_SIMBOLICA -> Color(0xFF7B1FA2)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = ReUAMSurface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.border(
                1.dp,
                ReUAMGreenSoft,
                RoundedCornerShape(16.dp)
            )
        ) {
            Column {
                // Imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(ReUAMBackground)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Badge de estado encima de la imagen
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        StatusBadge(status = status, small = true)
                    }
                }

                // Contenido
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Título
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = ReUAMTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Chip de categoría
                    Text(
                        text = category,
                        fontSize = 11.sp,
                        color = ReUAMGreen,
                        modifier = Modifier
                            .background(ReUAMGreenSoft, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )

                    // Divider
                    HorizontalDivider(color = ReUAMGreenSoft, thickness = 1.dp)

                    // Tipo y owner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type.label,
                            fontSize = 13.sp,
                            color = typeColor,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = owner,
                            fontSize = 11.sp,
                            color = ReUAMTextSecondary
                        )
                    }
                }
            }
        }
    }
}