package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.dto.ItemPhotoResponse
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.remote.dto.ItemStatus as BackendItemStatus
import ni.edu.uam.reuam.data.remote.dto.ItemTransactionType
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.presentation.home.toArticleStatus
import ni.edu.uam.reuam.presentation.home.toDisplayLabel
import ni.edu.uam.reuam.ui.theme.ReUAMAccent
import ni.edu.uam.reuam.ui.theme.ReUAMBackground
import ni.edu.uam.reuam.ui.theme.ReUAMError
import ni.edu.uam.reuam.ui.theme.ReUAMGreen
import ni.edu.uam.reuam.ui.theme.ReUAMGreenSoft
import ni.edu.uam.reuam.ui.theme.ReUAMSurface
import ni.edu.uam.reuam.ui.theme.ReUAMTextPrimary
import ni.edu.uam.reuam.ui.theme.ReUAMTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onBackClick: () -> Unit,
    onRequestClick: (ItemResponse) -> Unit,
    onEditClick: (ItemResponse) -> Unit = {},
    onDeletedSuccessfully: () -> Unit = onBackClick,
    shouldRefresh: Boolean = false,
    viewModel: ItemDetailViewModel = viewModel(factory = ItemDetailViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val showDeleteConfirm = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) viewModel.loadItem()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val state = uiState
                    if (state is ApiResult.Success && state.data.isOwner) {
                        IconButton(onClick = { onEditClick(state.data.item) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteConfirm.value = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = ReUAMError)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReUAMSurface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val state = uiState
            if (state is ApiResult.Success) {
                DetailBottomBar(
                    data = state.data,
                    onRequestClick = { onRequestClick(state.data.item) },
                    onEditClick = { onEditClick(state.data.item) },
                )
            }
        },
        containerColor = ReUAMBackground,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is ApiResult.Loading -> LoadingContent(message = "Cargando artículo...")

                is ApiResult.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadItem() }
                )

                is ApiResult.Success -> ItemDetailContent(
                    data = state.data,
                    contentPadding = PaddingValues(bottom = 20.dp),
                )
            }
        }

        if (showDeleteConfirm.value) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm.value = false },
                icon = { Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = ReUAMError) },
                title = { Text("¿Eliminar este artículo?") },
                text = { Text("Esta acción no se puede deshacer. El artículo dejará de estar disponible para otros estudiantes.") },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirm.value = false
                            viewModel.deleteItem(
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Artículo eliminado")
                                        onDeletedSuccessfully()
                                    }
                                },
                                onError = { message ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            )
                        },
                        enabled = !isDeleting,
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Eliminar", color = ReUAMError)
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteConfirm.value = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun ItemDetailContent(
    data: ItemDetailUiData,
    contentPadding: PaddingValues,
) {
    val item = data.item

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        ItemPhotoGallery(photos = item.photos)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ReUAMTextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatusPill(item.status.toArticleStatus().name.lowercase().replaceFirstChar { it.uppercase() })
            }

            InfoPillsRow(item)

            if (!item.location.isNullOrBlank()) {
                DetailInlineInfo(
                    icon = Icons.Outlined.LocationOn,
                    text = item.location,
                )
            }

            DetailCard(title = "Descripción") {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReUAMTextPrimary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }

            DetailCard(title = "Detalles del intercambio") {
                DetailRow(label = "Tipo", value = item.transactionType.toDisplayLabel())
                DetailRow(label = "Condición", value = item.condition.toDisplayLabel())
                if (item.transactionType == ItemTransactionType.SYMBOLIC_SALE && item.priceCents != null) {
                    DetailRow(label = "Precio simbólico", value = "C$ ${item.priceCents}")
                }
                DetailRow(label = "Publicado", value = item.createdAt.formatDate())
            }

            DetailCard(title = "Publicado por") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ReUAMGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (data.isOwner) "Tú" else "Estudiante ReUAM",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ReUAMTextPrimary,
                        )
                        Text(
                            text = if (data.isOwner) "Este artículo fue publicado por tu cuenta" else "Miembro de la comunidad universitaria",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReUAMTextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBottomBar(
    data: ItemDetailUiData,
    onRequestClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    Surface(
        color = ReUAMSurface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                !data.isOwner && data.item.status == BackendItemStatus.AVAILABLE -> {
                    ReUAMButton(
                        text = "Solicitar artículo",
                        onClick = onRequestClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                data.isOwner -> {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar")
                    }
                    Text(
                        text = "Artículo propio",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReUAMTextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
                else -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ReUAMGreenSoft,
                    ) {
                        Text(
                            text = "Este artículo ya no está disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReUAMTextSecondary,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemPhotoGallery(photos: List<ItemPhotoResponse>) {
    if (photos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(ReUAMGreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.size(42.dp))
                Text(
                    text = "Sin fotos disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReUAMTextSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        return
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
    ) {
        itemsIndexed(photos.sortedBy { it.sortOrder }) { _, photo ->
            AsyncImage(
                model = photo.downloadUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillParentMaxWidth()
                    .height(250.dp),
            )
        }
    }
}

@Composable
private fun InfoPillsRow(item: ItemResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SoftInfoPill(icon = Icons.Outlined.LocalOffer, label = "Cat. ${item.categoryId ?: "-"}")
        SoftInfoPill(icon = Icons.Outlined.Star, label = item.condition.toDisplayLabel())
        SoftInfoPill(icon = Icons.Outlined.Info, label = item.transactionType.toDisplayLabel())
    }
}

@Composable
private fun SoftInfoPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = ReUAMSurface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ReUAMGreen.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(icon, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.size(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ReUAMTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusPill(label: String) {
    val color = when (label.lowercase()) {
        "disponible" -> ReUAMGreen
        "reservado" -> ReUAMAccent
        else -> ReUAMTextSecondary
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ReUAMGreen,
            )
            content()
        }
    }
}

@Composable
private fun DetailInlineInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = ReUAMTextSecondary)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = ReUAMTextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = ReUAMTextPrimary,
        )
    }
}

private fun Long.formatDate(): String = try {
    SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-NI")).format(Date(this))
} catch (_: Exception) {
    "Fecha no disponible"
}
