package ni.edu.uam.reuam.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.presentation.components.ArticleStatus
import ni.edu.uam.reuam.presentation.components.StatusBadge
import ni.edu.uam.reuam.ui.theme.*

// ── Modelos de datos ──────────────────────────────────────────────────────────

private data class AdminStat(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val color: Color
)

private data class RecentPublication(
    val id: Int,
    val title: String,
    val user: String,
    val category: String,
    val status: ArticleStatus,
    val date: String
)

private data class Report(
    val id: Int,
    val publication: String,
    val reporter: String,
    val reason: String,
    val date: String
)

// ── Pantalla ──────────────────────────────────────────────────────────────────

@Composable
fun AdminPanelScreen(
    onBack: () -> Unit = {}
) {
    val stats = listOf(
        AdminStat(Icons.Outlined.Inventory2,   "Publicaciones activas",  "48",  ReUAMGreen),
        AdminStat(Icons.Outlined.Warning,       "Reportes pendientes",    "3",   Color(0xFFF9A825)),
        AdminStat(Icons.Outlined.CheckCircle,   "Artículos entregados",   "127", ReUAMSecondary),
        AdminStat(Icons.Outlined.Group,         "Usuarios registrados",   "342", ReUAMTextSecondary),
    )

    val recentPublications = listOf(
        RecentPublication(1, "Cálculo I - 9na edición",
            "Juan Pérez", "Libros", ArticleStatus.DISPONIBLE, "Hace 2 horas"),
        RecentPublication(2, "Calculadora científica",
            "María González", "Tecnología", ArticleStatus.DISPONIBLE, "Hace 5 horas"),
        RecentPublication(3, "Apuntes de Física II",
            "Carlos Ramírez", "Apuntes", ArticleStatus.RESERVADO, "Hace 1 día"),
    )

    val reports = listOf(
        Report(1, "Libro de programación C++",
            "Ana Martínez", "Contenido inapropiado", "Hace 3 horas"),
        Report(2, "Calculadora gráfica",
            "Roberto López", "Artículo no disponible", "Hace 1 día"),
    )

    Scaffold(
        containerColor = ReUAMBackground
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás",
                            tint = ReUAMTextPrimary)
                    }
                    Text(
                        "Panel administrativo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ReUAMTextPrimary
                    )
                }
            }

            // ── CONTENIDO ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // ── Grid de estadísticas 2x2 ──────────────────────────────────
                AdminSection(title = null) {
                    // Fila 1
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminStatCard(stat = stats[0], modifier = Modifier.weight(1f))
                        AdminStatCard(stat = stats[1], modifier = Modifier.weight(1f))
                    }
                    // Fila 2
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminStatCard(stat = stats[2], modifier = Modifier.weight(1f))
                        AdminStatCard(stat = stats[3], modifier = Modifier.weight(1f))
                    }
                }

                // ── Acciones rápidas ──────────────────────────────────────────
                AdminSection(title = "Acciones rápidas") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ReUAMGreen)
                        ) {
                            Text("Revisar publicaciones",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White)
                        }
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ReUAMGreen
                            )
                        ) {
                            Text("Gestionar categorías",
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // ── Publicaciones recientes ───────────────────────────────────
                AdminSection(
                    title = "Publicaciones recientes",
                    actionLabel = "Ver todas",
                    onAction = {}
                ) {
                    AdminCard {
                        recentPublications.forEachIndexed { index, pub ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            pub.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ReUAMTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Por ${pub.user} · ${pub.category}",
                                            fontSize = 12.sp,
                                            color = ReUAMTextSecondary
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            StatusBadge(status = pub.status, small = true)
                                            Text(pub.date, fontSize = 11.sp,
                                                color = ReUAMTextSecondary)
                                        }
                                    }
                                    IconButton(
                                        onClick = {},
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.MoreVert, null,
                                            Modifier.size(20.dp), ReUAMTextSecondary)
                                    }
                                }
                                if (index != recentPublications.lastIndex) {
                                    HorizontalDivider(
                                        color = ReUAMGreen.copy(alpha = 0.10f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Reportes pendientes ───────────────────────────────────────
                AdminSection(
                    title = "Reportes pendientes",
                    actionLabel = "Ver todos",
                    onAction = {}
                ) {
                    AdminCard {
                        reports.forEachIndexed { index, report ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Ícono de alerta
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                Color(0xFFF9A825).copy(alpha = 0.12f),
                                                RoundedCornerShape(50)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Warning, null,
                                            Modifier.size(20.dp), Color(0xFFF9A825))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            report.publication,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ReUAMTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Reportado por ${report.reporter}",
                                            fontSize = 12.sp,
                                            color = ReUAMTextSecondary
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                report.reason,
                                                modifier = Modifier
                                                    .background(
                                                        Color(0xFFF9A825).copy(alpha = 0.12f),
                                                        RoundedCornerShape(50)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                                fontSize = 11.sp,
                                                color = Color(0xFFF9A825),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(report.date, fontSize = 11.sp,
                                                color = ReUAMTextSecondary)
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {},
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ReUAMGreen
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Text("Revisar",
                                                    fontSize = 13.sp, color = Color.White)
                                            }
                                            OutlinedButton(
                                                onClick = {},
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = ReUAMGreen
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Text("Descartar", fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                                if (index != reports.lastIndex) {
                                    HorizontalDivider(
                                        color = ReUAMGreen.copy(alpha = 0.10f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-composables reutilizables ─────────────────────────────────────────────

@Composable
private fun AdminStatCard(stat: AdminStat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .border(1.dp, ReUAMGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = ReUAMSurface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(stat.color, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(stat.icon, null, Modifier.size(22.dp), Color.White)
            }
            Spacer(Modifier.height(12.dp))
            Text(stat.value, fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold, color = ReUAMTextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(stat.label, fontSize = 12.sp,
                color = ReUAMTextSecondary, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun AdminSection(
    title: String?,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (title != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ReUAMTextPrimary)
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                        Text(actionLabel, color = ReUAMGreen,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        content()
    }
}

@Composable
private fun AdminCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ReUAMGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = ReUAMSurface,
        shadowElevation = 2.dp
    ) {
        Column(content = content)
    }
}