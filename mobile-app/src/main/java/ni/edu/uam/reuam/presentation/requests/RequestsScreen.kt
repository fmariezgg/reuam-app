package ni.edu.uam.reuam.presentation.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.dto.ExchangeRequestStatus
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    onBackClick: () -> Unit = {},
    onArticleClick: (String) -> Unit = {},
    viewModel: RequestsViewModel = viewModel(),
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val sentState by viewModel.sentState.collectAsState()
    val receivedState by viewModel.receivedState.collectAsState()
    val processingIds by viewModel.processingIds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Solicitudes")
                        Text(
                            text = "Gestiona intercambios y respuestas",
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
        containerColor = ReUAMBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RequestsSegmentedTabs(
                selectedTab = selectedTab,
                sentState = sentState,
                receivedState = receivedState,
                onTabSelected = viewModel::onTabSelected,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    RequestsTab.RECEIVED -> RequestsTabContent(
                        state = receivedState,
                        onRetry = { viewModel.loadReceived() },
                        emptyTitle = "Sin solicitudes recibidas",
                        emptyMessage = "Cuando alguien solicite uno de tus artículos, aparecerá aquí.",
                        leadingIcon = { Icon(Icons.Filled.MarkEmailUnread, contentDescription = null) },
                    ) { requestUi ->
                        RequestCard(
                            requestUi = requestUi,
                            isReceived = true,
                            isProcessing = processingIds.contains(requestUi.request.id),
                            onArticleClick = { onArticleClick(requestUi.request.itemId) },
                            onAccept = {
                                viewModel.respondToRequest(requestUi, ExchangeRequestStatus.ACCEPTED) { message ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            },
                            onReject = {
                                viewModel.respondToRequest(requestUi, ExchangeRequestStatus.REJECTED) { message ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            },
                            onCancel = {},
                        )
                    }

                    RequestsTab.SENT -> RequestsTabContent(
                        state = sentState,
                        onRetry = { viewModel.loadSent() },
                        emptyTitle = "No has enviado solicitudes",
                        emptyMessage = "Explora artículos disponibles y envía una solicitud al que te interese.",
                        leadingIcon = { Icon(Icons.Filled.Outbox, contentDescription = null) },
                    ) { requestUi ->
                        RequestCard(
                            requestUi = requestUi,
                            isReceived = false,
                            isProcessing = processingIds.contains(requestUi.request.id),
                            onArticleClick = { onArticleClick(requestUi.request.itemId) },
                            onAccept = {},
                            onReject = {},
                            onCancel = {
                                viewModel.cancelMyRequest(requestUi) { message ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestsSegmentedTabs(
    selectedTab: RequestsTab,
    sentState: ApiResult<List<RequestUi>>,
    receivedState: ApiResult<List<RequestUi>>,
    onTabSelected: (RequestsTab) -> Unit,
) {
    val receivedCount = (receivedState as? ApiResult.Success)?.data?.size ?: 0
    val sentCount = (sentState as? ApiResult.Success)?.data?.size ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FilterChip(
            selected = selectedTab == RequestsTab.RECEIVED,
            onClick = { onTabSelected(RequestsTab.RECEIVED) },
            label = { Text("Recibidas $receivedCount") },
            leadingIcon = { Icon(Icons.Filled.MarkEmailUnread, contentDescription = null, modifier = Modifier.size(18.dp)) },
        )
        FilterChip(
            selected = selectedTab == RequestsTab.SENT,
            onClick = { onTabSelected(RequestsTab.SENT) },
            label = { Text("Enviadas $sentCount") },
            leadingIcon = { Icon(Icons.Filled.Outbox, contentDescription = null, modifier = Modifier.size(18.dp)) },
        )
    }
}

@Composable
private fun RequestsTabContent(
    state: ApiResult<List<RequestUi>>,
    onRetry: () -> Unit,
    emptyTitle: String,
    emptyMessage: String,
    leadingIcon: @Composable () -> Unit,
    content: @Composable (RequestUi) -> Unit,
) {
    when (state) {
        is ApiResult.Loading -> LoadingContent(message = "Cargando solicitudes...")
        is ApiResult.Error -> ErrorContent(message = state.message, onRetry = onRetry)
        is ApiResult.Success -> {
            if (state.data.isEmpty()) {
                EmptyContent(title = emptyTitle, message = emptyMessage)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        RequestsInfoCard(
                            title = emptyTitle.replace("Sin ", "").replace("No has enviado ", ""),
                            count = state.data.size,
                            icon = leadingIcon,
                        )
                    }
                    items(state.data, key = { it.request.id }) { requestUi -> content(requestUi) }
                }
            }
        }
    }
}

@Composable
private fun RequestsInfoCard(
    title: String,
    count: Int,
    icon: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ReUAMGreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides ReUAMGreen,
                    content = icon,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("$count solicitud${if (count == 1) "" else "es"} en esta sección", style = MaterialTheme.typography.bodySmall, color = ReUAMTextSecondary)
            }
        }
    }
}

@Composable
private fun RequestCard(
    requestUi: RequestUi,
    isReceived: Boolean,
    isProcessing: Boolean,
    onArticleClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
) {
    val request = requestUi.request
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onArticleClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(statusColor(request.status).copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = statusColor(request.status))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = requestUi.itemTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ReUAMTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (isReceived) "Solicitud recibida" else "Solicitud enviada",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReUAMTextSecondary,
                    )
                }
                RequestStatusPill(request.status)
            }

            if (!request.message.isNullOrBlank()) {
                Text(
                    text = if (isReceived) "Mensaje: ${request.message}" else "Tu mensaje: ${request.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReUAMTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ReUAMBackground)
                        .padding(12.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onArticleClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver artículo")
                }

                if (request.status == ExchangeRequestStatus.PENDING) {
                    if (isProcessing) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
                        }
                    } else if (isReceived) {
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = ReUAMGreen),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Aceptar")
                        }
                        OutlinedButton(onClick = onReject) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestStatusPill(status: ExchangeRequestStatus) {
    val color = statusColor(status)
    val label = when (status) {
        ExchangeRequestStatus.PENDING -> "Pendiente"
        ExchangeRequestStatus.ACCEPTED -> "Aceptada"
        ExchangeRequestStatus.REJECTED -> "Rechazada"
        ExchangeRequestStatus.CANCELLED -> "Cancelada"
        ExchangeRequestStatus.COMPLETED -> "Completada"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

private fun statusColor(status: ExchangeRequestStatus) = when (status) {
    ExchangeRequestStatus.PENDING -> ReUAMAccent
    ExchangeRequestStatus.ACCEPTED -> ReUAMGreen
    ExchangeRequestStatus.REJECTED -> ReUAMError
    ExchangeRequestStatus.CANCELLED -> ReUAMTextSecondary
    ExchangeRequestStatus.COMPLETED -> ReUAMGreen
}
