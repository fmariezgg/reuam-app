package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.dto.ItemPhotoResponse
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.data.remote.dto.ItemTransactionType
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.presentation.components.StatusBadge
import ni.edu.uam.reuam.presentation.home.toArticleStatus
import ni.edu.uam.reuam.presentation.home.toDisplayLabel
import ni.edu.uam.reuam.ui.theme.ReUAMBackground
import ni.edu.uam.reuam.ui.theme.ReUAMGreen
import ni.edu.uam.reuam.ui.theme.ReUAMGreenSoft
import ni.edu.uam.reuam.ui.theme.ReUAMTextPrimary
import ni.edu.uam.reuam.ui.theme.ReUAMTextSecondary
import ni.edu.uam.reuam.data.remote.dto.ItemStatus as BackendItemStatus

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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) viewModel.loadItem()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del artículo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val state = uiState
                    if (state is ApiResult.Success && state.data.isOwner) {
                        IconButton(onClick = { onEditClick(state.data.item) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    paddingValues = PaddingValues(0.dp),
                    data = state.data,
                    onRequestClick = { onRequestClick(state.data.item) },
                )
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("¿Eliminar este artículo?") },
                text = { Text("Esta acción no se puede deshacer. El artículo dejará de ser visible para otros estudiantes.") },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirm = false
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
                        }
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.height(16.dp))
                        } else {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun ItemDetailContent(
    paddingValues: PaddingValues,
    data: ItemDetailUiData,
    onRequestClick: () -> Unit,
) {
    val item = data.item

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        ItemPhotoGallery(photos = item.photos)

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box {
                StatusBadge(status = item.status.toArticleStatus())
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                color = ReUAMTextPrimary,
            )

            Text(
                text = item.transactionType.toDisplayLabel(),
                style = MaterialTheme.typography.titleMedium,
                color = ReUAMGreen,
            )

            if (item.transactionType == ItemTransactionType.SYMBOLIC_SALE
                && item.priceCents != null
            ) {
                Text(
                    text = "Precio simbólico: C$ ${item.priceCents}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ReUAMTextPrimary,
                )
            }

            HorizontalDivider(color = ReUAMGreenSoft)

            Text(
                text = "Descripción",
                style = MaterialTheme.typography.titleSmall,
                color = ReUAMTextPrimary,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = ReUAMTextSecondary,
            )

            Text(
                text = "Condición: ${item.condition.toDisplayLabel()}",
                style = MaterialTheme.typography.bodyMedium,
                color = ReUAMTextSecondary,
            )

            if (!item.location.isNullOrBlank()) {
                Text(
                    text = "Lugar de entrega: ${item.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReUAMTextSecondary,
                )
            }

            if (!data.isOwner && item.status == BackendItemStatus.AVAILABLE) {
                ReUAMButton(
                    text = "Solicitar artículo",
                    onClick = onRequestClick,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else if (data.isOwner) {
                Text(
                    text = "Este es uno de tus artículos publicados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReUAMTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = "Este artículo ya no está disponible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReUAMTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
                .height(220.dp)
                .background(ReUAMGreenSoft)
        )
        return
    }

    LazyRow {
        items(photos) { photo ->
            AsyncImage(
                model = photo.downloadUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(0.dp)),
            )
        }
    }
}
