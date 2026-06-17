package ni.edu.uam.reuam.presentation.publications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ni.edu.uam.reuam.navigation.Routes
import ni.edu.uam.reuam.presentation.components.ArticleStatus
import ni.edu.uam.reuam.presentation.components.ReUAMBottomBar
import ni.edu.uam.reuam.presentation.components.StatusBadge
import ni.edu.uam.reuam.ui.theme.*

// ── Modelos de datos ──────────────────────────────────────────────────────────

private enum class PublicationTab(val label: String) {
    ACTIVE("Activas"),
    RESERVED("Reservadas"),
    DELIVERED("Entregadas"),
    CLOSED("Cerradas")
}

private data class Publication(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val status: ArticleStatus,
    val requestCount: Int = 0
)

private val mockPublications = mapOf(
    PublicationTab.ACTIVE to listOf(
        Publication(1, "Cálculo I - 9na edición",
            "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400",
            ArticleStatus.DISPONIBLE, requestCount = 3),
        Publication(2, "Calculadora científica Casio",
            "https://images.unsplash.com/photo-1611532736597-de2d4265fba3?w=400",
            ArticleStatus.DISPONIBLE, requestCount = 1),
    ),
    PublicationTab.RESERVED to listOf(
        Publication(3, "Apuntes de Física II",
            "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400",
            ArticleStatus.RESERVADO, requestCount = 0),
    ),
    PublicationTab.DELIVERED to emptyList(),
    PublicationTab.CLOSED    to emptyList()
)

// ── Pantalla ──────────────────────────────────────────────────────────────────

@Composable
fun MyPublicationsScreen(
    currentRoute: String = Routes.MyPublications.route,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    onPublishNew: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(PublicationTab.ACTIVE) }
    val publications = mockPublications[activeTab] ?: emptyList()

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
                            "Mis publicaciones",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ReUAMTextPrimary
                        )
                    }

                    // ── Tabs ──────────────────────────────────────────────────
                    LazyRow(
                        modifier = Modifier.padding(bottom = 12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PublicationTab.entries) { tab ->
                            val isActive = activeTab == tab
                            Surface(
                                onClick = { activeTab = tab },
                                shape = CircleShape,
                                color = if (isActive) ReUAMGreen else ReUAMGreenSoft
                            ) {
                                Text(
                                    text = tab.label,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (isActive) Color.White else ReUAMGreen,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            // ── LISTA / VACÍO ─────────────────────────────────────────────────
            if (publications.isEmpty()) {
                EmptyPublicationsPlaceholder(
                    tab = activeTab,
                    onPublishNew = if (activeTab == PublicationTab.ACTIVE) onPublishNew else null
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(publications, key = { it.id }) { pub ->
                        PublicationCard(publication = pub)
                    }
                }
            }
        }
    }
}

// ── Tarjeta de publicación ────────────────────────────────────────────────────

@Composable
private fun PublicationCard(publication: Publication) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ReUAMGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = ReUAMSurface,
        shadowElevation = 2.dp
    ) {
        Column {
            // Fila imagen + contenido
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imagen
                AsyncImage(
                    model = publication.imageUrl,
                    contentDescription = publication.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ReUAMBackground)
                )

                // Contenido
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = publication.title,
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ReUAMTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.MoreVert, null,
                                Modifier.size(20.dp), ReUAMTextSecondary)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    StatusBadge(status = publication.status, small = true)

                    // Badge de solicitudes
                    if (publication.requestCount > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .background(ReUAMGreenSoft, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Notifications, null,
                                Modifier.size(14.dp), ReUAMGreen)
                            Text(
                                "${publication.requestCount} solicitud${if (publication.requestCount != 1) "es" else ""}",
                                fontSize = 12.sp,
                                color = ReUAMGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Divider + botones de acción
            HorizontalDivider(
                color = ReUAMGreen.copy(alpha = 0.10f),
                thickness = 1.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Editar
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ReUAMGreenSoft,
                        contentColor = ReUAMGreen
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Filled.Edit, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar", style = MaterialTheme.typography.labelMedium)
                }
                // Cambiar estado
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ReUAMGreen),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Cambiar estado", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ── Vacío ─────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyPublicationsPlaceholder(
    tab: PublicationTab,
    onPublishNew: (() -> Unit)?
) {
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
            Icon(Icons.Outlined.Inventory2, null,
                Modifier.size(48.dp), ReUAMGreenLight)
        }
        Spacer(Modifier.height(16.dp))
        Text("No hay publicaciones",
            fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ReUAMTextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "No tienes publicaciones ${tab.label.lowercase()} en este momento.",
            style = MaterialTheme.typography.bodyMedium,
            color = ReUAMTextSecondary,
            textAlign = TextAlign.Center
        )
        if (onPublishNew != null) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onPublishNew,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReUAMGreen)
            ) {
                Text("Publicar artículo", color = Color.White)
            }
        }
    }
}