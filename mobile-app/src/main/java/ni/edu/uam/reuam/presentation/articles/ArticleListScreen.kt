package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.presentation.home.HomeArticleUi
import ni.edu.uam.reuam.presentation.home.HomeViewModel
import ni.edu.uam.reuam.presentation.components.*
import ni.edu.uam.reuam.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    onBackClick: () -> Unit,
    onArticleClick: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explorar artículos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ReUAMSurface,
                    titleContentColor = ReUAMTextPrimary
                )
            )
        },
        containerColor = ReUAMBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            ReUAMSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar en ReUAM...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            when (val state = uiState) {
                is ApiResult.Loading -> Box(modifier = Modifier.weight(1f)) {
                    LoadingContent(message = "Cargando artículos...")
                }

                is ApiResult.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadHome() },
                    modifier = Modifier.weight(1f),
                )

                is ApiResult.Success -> {
                    val filteredArticles = remember(state.data.articles, searchQuery) {
                        filterArticles(state.data.articles, searchQuery)
                    }

                    Text(
                        text = if (searchQuery.isBlank()) {
                            "Todos los artículos"
                        } else {
                            "${filteredArticles.size} resultado(s)"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = ReUAMTextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (filteredArticles.isEmpty()) {
                        EmptyContent(
                            title = "Sin resultados",
                            message = "No encontramos artículos que coincidan con \"$searchQuery\".",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        ArticleListGrid(
                            articles = filteredArticles,
                            onArticleClick = onArticleClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleListGrid(
    articles: List<HomeArticleUi>,
    onArticleClick: (String) -> Unit,
) {
    val rows = articles.chunked(2)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { article ->
                    ArticleCard(
                        imageUrl = article.imageUrl,
                        title = article.title,
                        category = article.categoryLabel,
                        type = article.type,
                        status = article.status,
                        owner = "",
                        onClick = { onArticleClick(article.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun filterArticles(
    articles: List<HomeArticleUi>,
    searchQuery: String,
): List<HomeArticleUi> {
    if (searchQuery.isBlank()) return articles

    val normalizedQuery = searchQuery.trim()
    return articles.filter { article ->
        article.title.contains(normalizedQuery, ignoreCase = true) ||
            article.categoryLabel.contains(normalizedQuery, ignoreCase = true) ||
            article.type.label.contains(normalizedQuery, ignoreCase = true)
    }
}
