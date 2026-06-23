package ni.edu.uam.reuam.presentation.requests

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.dto.ExchangeRequestStatus
import ni.edu.uam.reuam.presentation.components.EmptyContent
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.ui.theme.ReUAMBackground
import ni.edu.uam.reuam.ui.theme.ReUAMGreen
import ni.edu.uam.reuam.ui.theme.ReUAMTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
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
        topBar = { TopAppBar(title = { Text("Solicitudes") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ReUAMBackground,
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == RequestsTab.RECEIVED,
                    onClick = { viewModel.onTabSelected(RequestsTab.RECEIVED) },
                    text = { Text("Recibidas") }
                )
                Tab(
                    selected = selectedTab == RequestsTab.SENT,
                    onClick = { viewModel.onTabSelected(RequestsTab.SENT) },
                    text = { Text("Enviadas") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    RequestsTab.RECEIVED -> RequestsTabContent(
                        state = receivedState,
                        onRetry = { viewModel.loadReceived() },
                        emptyTitle = "Sin solicitudes recibidas",
                        emptyMessage = "Cuando alguien solicite uno de tus artículos, aparecerá aquí.",
                        content = { requestUi ->
                            ReceivedRequestCard(
                                requestUi = requestUi,
                                isProcessing = processingIds.contains(requestUi.request.id),
                                onAccept = {
                                    viewModel.respondToRequest(
                                        requestUi, ExchangeRequestStatus.ACCEPTED
                                    ) { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                                },
                                onReject = {
                                    viewModel.respondToRequest(
                                        requestUi, ExchangeRequestStatus.REJECTED
                                    ) { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                                },
                                onClick = { onArticleClick(requestUi.request.itemId) }
                            )
                        }
                    )

                    RequestsTab.SENT -> RequestsTabContent(
                        state = sentState,
                        onRetry = { viewModel.loadSent() },
                        emptyTitle = "No has enviado solicitudes",
                        emptyMessage = "Explora artículos en Home y solicita el que te interese.",
                        content = { requestUi ->
                            SentRequestCard(
                                requestUi = requestUi,
                                isProcessing = processingIds.contains(requestUi.request.id),
                                onCancel = {
                                    viewModel.cancelMyRequest(requestUi) { msg ->
                                        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                }
                            ) { onArticleClick(requestUi.request.itemId) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestsTabContent(
    state: ApiResult<List<RequestUi>>,
    onRetry: () -> Unit,
    emptyTitle: String,
    emptyMessage: String,
    content: @Composable (RequestUi) -> Unit,
) {
    when (state) {
        is ApiResult.Loading -> LoadingContent(message = "Cargando solicitudes...")
        is ApiResult.Error -> ErrorContent(message = state.message, onRetry = onRetry)
        is ApiResult.Success -> {
            if (state.data.isEmpty()) {
                EmptyContent(title = emptyTitle, message = emptyMessage)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.data.forEach { requestUi -> content(requestUi) }
                }
            }
        }
    }
}

@Composable
private fun ReceivedRequestCard(
    requestUi: RequestUi,
    isProcessing: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(requestUi.itemTitle, style = MaterialTheme.typography.titleMedium)
            RequestStatusChip(requestUi.request.status)

            if (!requestUi.request.message.isNullOrBlank()) {
                Text(
                    text = "\"${requestUi.request.message}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReUAMTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (requestUi.request.status == ExchangeRequestStatus.PENDING) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                } else {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = ReUAMGreen)) {
                            Text("Aceptar")
                        }
                        OutlinedButton(onClick = onReject) {
                            Text("Rechazar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SentRequestCard(
    requestUi: RequestUi,
    isProcessing: Boolean,
    onCancel: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(requestUi.itemTitle, style = MaterialTheme.typography.titleMedium)
            RequestStatusChip(requestUi.request.status)

            if (!requestUi.request.message.isNullOrBlank()) {
                Text(
                    text = "Tu mensaje: \"${requestUi.request.message}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReUAMTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (requestUi.request.status == ExchangeRequestStatus.PENDING) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                } else {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        Text("Cancelar solicitud")
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestStatusChip(status: ExchangeRequestStatus) {
    val (label, color) = when (status) {
        ExchangeRequestStatus.PENDING -> "Pendiente" to Color(0xFFF9A825)
        ExchangeRequestStatus.ACCEPTED -> "Aceptada" to ReUAMGreen
        ExchangeRequestStatus.REJECTED -> "Rechazada" to Color(0xFFB00020)
        ExchangeRequestStatus.CANCELLED -> "Cancelada" to Color(0xFF9E9E9E)
        ExchangeRequestStatus.COMPLETED -> "Completada" to Color(0xFF1565C0)
    }
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}
