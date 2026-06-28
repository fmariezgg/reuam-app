package ni.edu.uam.reuam.presentation.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.presentation.components.ReUAMTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onBackClick: () -> Unit,
    onRequestSent: () -> Unit,
    viewModel: CreateRequestViewModel = viewModel(factory = CreateRequestViewModel.Factory),
) {
    val message by viewModel.message.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar artículo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Cuéntale al dueño por qué te interesa este artículo. Este mensaje es opcional pero ayuda a que respondan más rápido.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ReUAMTextField(
                value = message,
                onValueChange = viewModel::onMessageChange,
                label = "Mensaje (opcional)",
                placeholder = "Ej. Hola, me interesa para mi clase de Física...",
                singleLine = false,
            )

            if (isSending) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            } else {
                ReUAMButton(
                    text = "Enviar solicitud",
                    onClick = {
                        viewModel.sendRequest(
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Solicitud enviada")
                                    onRequestSent()
                                }
                            },
                            onError = { errorMessage ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(errorMessage) }
                            }
                        )
                    }
                )
            }
        }
    }
}
