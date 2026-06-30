package ni.edu.uam.reuam.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ni.edu.uam.reuam.ui.theme.ReUAMAccent
import ni.edu.uam.reuam.ui.theme.ReUAMBackground
import ni.edu.uam.reuam.ui.theme.ReUAMError
import ni.edu.uam.reuam.ui.theme.ReUAMGreen
import ni.edu.uam.reuam.ui.theme.ReUAMGreenSoft
import ni.edu.uam.reuam.ui.theme.ReUAMSurface
import ni.edu.uam.reuam.ui.theme.ReUAMTextPrimary
import ni.edu.uam.reuam.ui.theme.ReUAMTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBackClick: () -> Unit,
    isAdmin: Boolean = false,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel administrador")
                        Text(
                            text = "ReUAM · Gestión académica",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReUAMTextSecondary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        containerColor = ReUAMBackground,
    ) { paddingValues ->
        if (!isAdmin) {
            RestrictedAdminContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onBackClick = onBackClick,
            )
        } else {
            AdminContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun RestrictedAdminContent(
    modifier: Modifier,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(ReUAMGreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.size(44.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Acceso restringido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ReUAMTextPrimary)
        Text(
            text = "Este panel está reservado para administradores de ReUAM. Si necesitás acceso, solicitá la asignación de rol al equipo encargado.",
            style = MaterialTheme.typography.bodyMedium,
            color = ReUAMTextSecondary,
            modifier = Modifier.padding(top = 10.dp),
        )
        Button(onClick = onBackClick, modifier = Modifier.padding(top = 22.dp)) {
            Text("Volver")
        }
    }
}

@Composable
private fun AdminContent(modifier: Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AdminHeaderCard()
        }
        item {
            Text("Resumen general", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ReUAMTextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard("Usuarios", "342", "Registrados", ReUAMGreen, { Icon(Icons.Filled.People, contentDescription = null) }, Modifier.weight(1f))
                AdminStatCard("Publicaciones", "66", "Activas", ReUAMAccent, { Icon(Icons.Filled.Article, contentDescription = null) }, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard("Categorías", "6", "Disponibles", Color(0xFF1565C0), { Icon(Icons.Filled.Category, contentDescription = null) }, Modifier.weight(1f))
                AdminStatCard("Reportes", "2", "Pendientes", ReUAMError, { Icon(Icons.Filled.Flag, contentDescription = null) }, Modifier.weight(1f))
            }
        }
        item {
            AdminSectionTitle("Gestión")
            AdminActionCard()
        }
        item {
            AdminSectionTitle("Publicaciones recientes")
            RecentPublication("Libro de programación C++", "Ana Martínez · Libros", "Disponible")
            RecentPublication("Calculadora científica", "Carlos López · Tecnología", "Reservado")
            RecentPublication("Apuntes de Cálculo II", "María Gómez · Apuntes", "Disponible")
        }
        item {
            AdminSectionTitle("Reportes pendientes")
            ReportCard("Libro de programación C++", "Contenido inapropiado", "Reportado hace 3 h")
            ReportCard("Calculadora científica", "Artículo no disponible", "Reportado ayer")
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AdminHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMGreen),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Administración ReUAM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Revisión de actividad, reportes y categorías", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.86f))
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides color,
                    content = icon,
                )
            }
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ReUAMTextPrimary)
            Text(title, style = MaterialTheme.typography.bodySmall, color = ReUAMTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ReUAMTextSecondary)
        }
    }
}

@Composable
private fun AdminSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = ReUAMTextPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun AdminActionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
    ) {
        AdminActionRow(Icons.Filled.Article, "Gestionar publicaciones", "Revisar artículos activos y reportados")
        AdminActionRow(Icons.Filled.People, "Gestionar usuarios", "Consultar perfiles y actividad")
        AdminActionRow(Icons.Filled.Category, "Gestionar categorías", "Mantener categorías de artículos")
    }
}

@Composable
private fun AdminActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ReUAMGreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = ReUAMGreen)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ReUAMTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ReUAMTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ReUAMTextSecondary)
    }
}

@Composable
private fun RecentPublication(title: String, meta: String, status: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Article, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ReUAMTextPrimary)
                Text(meta, style = MaterialTheme.typography.bodySmall, color = ReUAMTextSecondary)
            }
            Text(status, style = MaterialTheme.typography.labelSmall, color = ReUAMGreen)
        }
    }
}

@Composable
private fun ReportCard(title: String, reason: String, date: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = ReUAMAccent)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ReUAMTextPrimary)
                    Text(date, style = MaterialTheme.typography.bodySmall, color = ReUAMTextSecondary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reason,
                    style = MaterialTheme.typography.labelMedium,
                    color = ReUAMAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(ReUAMAccent.copy(alpha = 0.13f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = {}) { Text("Descartar") }
                Button(onClick = {}) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Revisar")
                }
            }
        }
    }
}
