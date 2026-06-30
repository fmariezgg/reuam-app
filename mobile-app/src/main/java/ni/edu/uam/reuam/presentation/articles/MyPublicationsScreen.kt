package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.remote.dto.ItemStatus
import ni.edu.uam.reuam.data.remote.dto.ItemTransactionType
import ni.edu.uam.reuam.presentation.components.EmptyContent
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.ui.theme.ReUAMAccent
import ni.edu.uam.reuam.ui.theme.ReUAMBackground
import ni.edu.uam.reuam.ui.theme.ReUAMError
import ni.edu.uam.reuam.ui.theme.ReUAMGreen
import ni.edu.uam.reuam.ui.theme.ReUAMGreenSoft
import ni.edu.uam.reuam.ui.theme.ReUAMSurface
import ni.edu.uam.reuam.ui.theme.ReUAMTextPrimary
import ni.edu.uam.reuam.ui.theme.ReUAMTextSecondary

private enum class PublicationFilter(val label: String) {
    AVAILABLE("Disponibles"),
    RESERVED("Reservados"),
    COMPLETED("Completados"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPublicationsScreen(
    onBackClick: () -> Unit,
    onArticleClick: (String) -> Unit,
    onPublishClick: () -> Unit = {},
    onEditArticleClick: (String) -> Unit = {},
    viewModel: MyPublicationsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deletingIds by viewModel.deletingIds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf(PublicationFilter.AVAILABLE) }
    var pendingDelete by remember { mutableStateOf<ItemResponse?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis publicaciones")
                        Text(
                            text = "Gestiona los artículos que compartiste",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReUAMTextSecondary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onPublishClick,
                containerColor = ReUAMGreen,
                contentColor = ReUAMSurface,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Publicar")
            }
        },
        containerColor = ReUAMBackground,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ApiResult.Loading -> LoadingContent(message = "Cargando tus publicaciones...")
                is ApiResult.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadMyItems() }
                )
                is ApiResult.Success -> {
                    val allItems = state.data
                    val filtered = allItems.filter { item ->
                        when (selectedFilter) {
                            PublicationFilter.AVAILABLE -> item.status == ItemStatus.AVAILABLE || item.status == ItemStatus.INACTIVE
                            PublicationFilter.RESERVED -> item.status == ItemStatus.RESERVED
                            PublicationFilter.COMPLETED -> item.status == ItemStatus.COMPLETED
                        }
                    }

                    PublicationsContent(
                        allItems = allItems,
                        filteredItems = filtered,
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it },
                        deletingIds = deletingIds,
                        onArticleClick = onArticleClick,
                        onEditArticleClick = onEditArticleClick,
                        onDeleteClick = { pendingDelete = it },
                        onPublishClick = onPublishClick,
                    )
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("¿Eliminar publicación?") },
            text = {
                Text("Esta acción no se puede deshacer y puede cancelar solicitudes asociadas a este artículo.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(
                            itemId = item.id,
                            onSuccess = {
                                pendingDelete = null
                                coroutineScope.launch { snackbarHostState.showSnackbar("Publicación eliminada") }
                            },
                            onError = { message ->
                                pendingDelete = null
                                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReUAMError)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PublicationsContent(
    allItems: List<ItemResponse>,
    filteredItems: List<ItemResponse>,
    selectedFilter: PublicationFilter,
    onFilterSelected: (PublicationFilter) -> Unit,
    deletingIds: Set<String>,
    onArticleClick: (String) -> Unit,
    onEditArticleClick: (String) -> Unit,
    onDeleteClick: (ItemResponse) -> Unit,
    onPublishClick: () -> Unit,
) {
    if (allItems.isEmpty()) {
        EmptyContent(
            title = "Todavía no has publicado nada",
            message = "Comparte un artículo con la comunidad UAM y aparecerá aquí.",
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PublicationsSummaryCard(allItems = allItems)
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PublicationFilter.entries.forEach { filter ->
                    val count = allItems.count { item ->
                        when (filter) {
                            PublicationFilter.AVAILABLE -> item.status == ItemStatus.AVAILABLE || item.status == ItemStatus.INACTIVE
                            PublicationFilter.RESERVED -> item.status == ItemStatus.RESERVED
                            PublicationFilter.COMPLETED -> item.status == ItemStatus.COMPLETED
                        }
                    }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text("${filter.label} $count") },
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Sin publicaciones", style = MaterialTheme.typography.titleMedium, color = ReUAMTextPrimary)
                        Text(
                            text = if (selectedFilter == PublicationFilter.AVAILABLE) {
                                "Publica un artículo nuevo para que otros estudiantes puedan solicitarlo."
                            } else {
                                "No tenés publicaciones en este estado todavía."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReUAMTextSecondary,
                        )
                        if (selectedFilter == PublicationFilter.AVAILABLE) {
                            Button(onClick = onPublishClick, colors = ButtonDefaults.buttonColors(containerColor = ReUAMGreen)) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Publicar artículo")
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                PublicationCard(
                    item = item,
                    isDeleting = deletingIds.contains(item.id),
                    onViewClick = { onArticleClick(item.id) },
                    onEditClick = { onEditArticleClick(item.id) },
                    onDeleteClick = { onDeleteClick(item) },
                )
            }
        }
    }
}

@Composable
private fun PublicationsSummaryCard(allItems: List<ItemResponse>) {
    val active = allItems.count { it.status == ItemStatus.AVAILABLE || it.status == ItemStatus.INACTIVE }
    val reserved = allItems.count { it.status == ItemStatus.RESERVED }
    val completed = allItems.count { it.status == ItemStatus.COMPLETED }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MiniStat("Activas", active.toString(), ReUAMGreen, Modifier.weight(1f))
                MiniStat("Reservadas", reserved.toString(), ReUAMAccent, Modifier.weight(1f))
                MiniStat("Completadas", completed.toString(), ReUAMTextSecondary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = ReUAMTextSecondary)
    }
}

@Composable
private fun PublicationCard(
    item: ItemResponse,
    isDeleting: Boolean,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AsyncImage(
                    model = item.photos.firstOrNull()?.downloadUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(ReUAMGreenSoft),
                    contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ReUAMTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(item.status)
                        Text(item.transactionType.label(), style = MaterialTheme.typography.bodySmall, color = ReUAMTextSecondary)
                    }
                    Text(
                        item.location?.takeIf { it.isNotBlank() } ?: "Ubicación por coordinar",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReUAMTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (item.status == ItemStatus.AVAILABLE) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ReUAMGreenSoft)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.size(16.dp))
                    Text(
                        "Revisa tus solicitudes recibidas para dar seguimiento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReUAMGreen,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onViewClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver")
                }
                if (item.status != ItemStatus.COMPLETED) {
                    OutlinedButton(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Editar")
                    }
                }
                IconButton(
                    onClick = onDeleteClick,
                    enabled = !isDeleting,
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ReUAMError)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: ItemStatus) {
    val (label, color) = when (status) {
        ItemStatus.AVAILABLE -> "Disponible" to ReUAMGreen
        ItemStatus.RESERVED -> "Reservado" to ReUAMAccent
        ItemStatus.COMPLETED -> "Completado" to ReUAMTextSecondary
        ItemStatus.INACTIVE -> "Inactivo" to ReUAMTextSecondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

private fun ItemTransactionType.label(): String = when (this) {
    ItemTransactionType.DONATION -> "Donación"
    ItemTransactionType.EXCHANGE -> "Intercambio"
    ItemTransactionType.LOAN -> "Préstamo"
    ItemTransactionType.SYMBOLIC_SALE -> "Venta simbólica"
}
