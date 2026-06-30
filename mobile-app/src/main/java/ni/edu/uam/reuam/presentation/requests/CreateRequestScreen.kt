package ni.edu.uam.reuam.presentation.requests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.presentation.home.toDisplayLabel
import ni.edu.uam.reuam.ui.theme.ReUAMBackground
import ni.edu.uam.reuam.ui.theme.ReUAMGreen
import ni.edu.uam.reuam.ui.theme.ReUAMGreenSoft
import ni.edu.uam.reuam.ui.theme.ReUAMSurface
import ni.edu.uam.reuam.ui.theme.ReUAMTextPrimary
import ni.edu.uam.reuam.ui.theme.ReUAMTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onBackClick: () -> Unit,
    onRequestSent: () -> Unit,
    viewModel: CreateRequestViewModel = viewModel(factory = CreateRequestViewModel.Factory),
) {
    val message by viewModel.message.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val itemState by viewModel.itemState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Enviar solicitud", fontWeight = FontWeight.SemiBold)
                        Text("Comunícate con respeto y claridad", style = MaterialTheme.typography.bodySmall, color = ReUAMTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReUAMSurface),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ReUAMBackground,
    ) { paddingValues: PaddingValues ->
        when (val state = itemState) {
            is ApiResult.Loading -> LoadingContent(message = "Cargando artículo...")
            is ApiResult.Error -> ErrorContent(message = state.message, onRetry = { viewModel.loadItemSummary() })
            is ApiResult.Success -> CreateRequestContent(
                paddingValues = paddingValues,
                item = state.data,
                message = message,
                isSending = isSending,
                onMessageChange = viewModel::onMessageChange,
                onSendClick = {
                    viewModel.sendRequest(
                        onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Solicitud enviada correctamente")
                                onRequestSent()
                            }
                        },
                        onError = { errorMessage ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(errorMessage) }
                        }
                    )
                },
            )
        }
    }
}

@Composable
private fun CreateRequestContent(
    paddingValues: PaddingValues,
    item: ItemResponse,
    message: String,
    isSending: Boolean,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ArticleSummaryCard(item = item)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mensaje opcional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${message.length}/300", style = MaterialTheme.typography.labelMedium, color = ReUAMTextSecondary)
                }
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    placeholder = { Text("Ej. Hola, me interesa para mi clase de Física...") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                )
            }
        }

        CommunityNotice()

        ReUAMButton(
            text = if (isSending) "Enviando solicitud..." else "Enviar solicitud",
            onClick = onSendClick,
            enabled = !isSending,
        )

        if (isSending) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = ReUAMGreen)
                Text("Procesando...", modifier = Modifier.padding(start = 10.dp), color = ReUAMTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ArticleSummaryCard(item: ItemResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val imageUrl = item.photos.firstOrNull()?.downloadUrl
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ReUAMGreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                if (imageUrl.isNullOrBlank()) {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.size(32.dp))
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(shape = RoundedCornerShape(50), color = ReUAMGreenSoft) {
                    Text(
                        text = item.transactionType.toDisplayLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = ReUAMGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ReUAMTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = item.condition.toDisplayLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReUAMTextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CommunityNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ReUAMGreenSoft,
        border = BorderStroke(1.dp, ReUAMGreen.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.size(20.dp))
            Column {
                Text(
                    text = "Uso comunitario responsable",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ReUAMGreen,
                )
                Text(
                    text = "Tu solicitud será enviada al publicador. Mantén un tono respetuoso y espera su confirmación antes de coordinar la entrega.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReUAMGreen,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
