package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.dto.ItemResponse
import ni.edu.uam.reuam.presentation.components.ArticleCard
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.presentation.components.EmptyContent
import ni.edu.uam.reuam.presentation.home.toArticleStatus
import ni.edu.uam.reuam.presentation.home.toArticleType
import ni.edu.uam.reuam.ui.theme.ReUAMBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPublicationsScreen(
    onBackClick: () -> Unit,
    onArticleClick: (String) -> Unit,
    viewModel: MyPublicationsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis publicaciones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = ReUAMBackground,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is ApiResult.Loading -> LoadingContent(message = "Cargando tus publicaciones...")

                is ApiResult.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadMyItems() }
                )

                is ApiResult.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyContent(
                            title = "Todavía no has publicado nada",
                            message = "Cuando publiques un artículo, aparecerá aquí."
                        )
                    } else {
                        MyPublicationsGrid(items = state.data, onArticleClick = onArticleClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPublicationsGrid(items: List<ItemResponse>, onArticleClick: (String) -> Unit) {
    val rows = items.chunked(2)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { item ->
                    ArticleCard(
                        imageUrl = item.photos.firstOrNull()?.downloadUrl
                            ?: "https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=400",
                        title = item.title,
                        category = "",
                        type = item.transactionType.toArticleType(),
                        status = item.status.toArticleStatus(),
                        owner = "",
                        onClick = { onArticleClick(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
