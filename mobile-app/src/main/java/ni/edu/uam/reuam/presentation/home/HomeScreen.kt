package ni.edu.uam.reuam.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.dto.CategoryResponse
import ni.edu.uam.reuam.navigation.Routes
import ni.edu.uam.reuam.presentation.components.*
import ni.edu.uam.reuam.ui.theme.*

// ── Categoría "Todos" sintética, se agrega siempre al inicio del filtro ──────
// El backend no manda un id especial para "Todos"; se representa con un id
// que ninguna categoría real puede tener, y se maneja aparte en filterArticles().
private const val ALL_CATEGORIES_ID = -1

// ── Grid manual 2 columnas (evita LazyVerticalGrid dentro de verticalScroll) ──

@Composable
private fun ArticlesGrid(articles: List<HomeArticleUi>, onArticleClick: (String) -> Unit) {
    val rows = articles.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { article ->
                    ArticleCard(
                        imageUrl  = article.imageUrl,
                        title     = article.title,
                        category  = article.categoryLabel,
                        type      = article.type,
                        status    = article.status,
                        owner     = "",
                        onClick   = { onArticleClick(article.id) },
                        modifier  = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── Pantalla ──────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    currentRoute: String = Routes.Home.route,
    onNavigate: (String) -> Unit = {},
    onPublishClick: () -> Unit = {},
    onArticleClick: (String) -> Unit = {},
    shouldRefresh: Boolean = false,
    viewModel: HomeViewModel = viewModel(),
) {
    var activeCategoryId by remember { mutableStateOf(ALL_CATEGORIES_ID) }
    var searchQuery by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) viewModel.loadHome()
    }

    Scaffold(
        bottomBar = {
            ReUAMBottomBar(currentRoute = currentRoute, onNavigate = onNavigate)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onPublishClick,
                containerColor = ReUAMGreen,
                contentColor   = Color.White,
                shape          = CircleShape,
                modifier       = Modifier.size(56.dp)
            ) {
                Icon(Icons.Filled.Add, "Publicar artículo", Modifier.size(28.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = ReUAMBackground
    ) { paddingValues ->

        when (val state = uiState) {
            is ApiResult.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                LoadingContent(message = "Cargando artículos...")
            }

            is ApiResult.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadHome() }
                )
            }

            is ApiResult.Success -> HomeContent(
                paddingValues = paddingValues,
                categories = state.data.categories,
                articles = state.data.articles,
                activeCategoryId = activeCategoryId,
                onCategorySelected = { activeCategoryId = it },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSeeAllClick = { onNavigate(Routes.ArticleList.route) },
                onArticleClick = onArticleClick,
            )
        }
    }
}

@Composable
private fun HomeContent(
    paddingValues: PaddingValues,
    categories: List<CategoryResponse>,
    articles: List<HomeArticleUi>,
    activeCategoryId: Int,
    onCategorySelected: (Int) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onArticleClick: (String) -> Unit,
) {
    val filteredArticles = remember(articles, activeCategoryId, searchQuery) {
        filterArticles(articles, categories, activeCategoryId, searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {

        // ── HEADER verde ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(ReUAMGreen, Color(0xFF1B5E20))),
                    RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Hola 👋",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "¿Qué te gustaría reutilizar hoy?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReUAMGreenLight
                        )
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Outlined.Notifications, null,
                            modifier = Modifier.size(22.dp), tint = Color.White)
                    }
                }
                ReUAMSearchBar(
                    query         = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder   = "Buscar artículos, libros, tecnología...",
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        }

        // ── CATEGORÍAS ────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Categorías",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = ReUAMTextPrimary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryChip(
                        label    = "Todos",
                        isActive = activeCategoryId == ALL_CATEGORIES_ID,
                        onClick  = { onCategorySelected(ALL_CATEGORIES_ID) },
                        icon     = Icons.Outlined.MoreHoriz
                    )
                }
                items(categories) { cat ->
                    CategoryChip(
                        label    = cat.name,
                        isActive = activeCategoryId == cat.id,
                        onClick  = { onCategorySelected(cat.id) },
                        icon     = Icons.Outlined.Category
                    )
                }
            }
        }

        // ── ARTÍCULOS RECIENTES ───────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Artículos recientes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ReUAMTextPrimary
                )
                TextButton(onClick = onSeeAllClick) {
                    Text("Ver todos", color = ReUAMGreen,
                        style = MaterialTheme.typography.labelLarge)
                }
            }

            if (filteredArticles.isEmpty()) {
                EmptyContent(
                    title = "Sin artículos por aquí",
                    message = if (searchQuery.isBlank()) {
                        "Todavía no hay artículos publicados en esta categoría. ¡Sé el primero en publicar uno!"
                    } else {
                        "No encontramos artículos que coincidan con \"$searchQuery\"."
                    }
                )
            } else {
                // Grid manual — NO usa LazyVerticalGrid para evitar el conflicto
                // de medición con verticalScroll
                ArticlesGrid(articles = filteredArticles, onArticleClick = onArticleClick)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun filterArticles(
    articles: List<HomeArticleUi>,
    categories: List<CategoryResponse>,
    activeCategoryId: Int,
    searchQuery: String,
): List<HomeArticleUi> {
    val categoryFiltered = if (activeCategoryId == ALL_CATEGORIES_ID) {
        articles
    } else {
        val categoryName = categories.firstOrNull { it.id == activeCategoryId }?.name
        articles.filter { it.categoryLabel == categoryName }
    }

    if (searchQuery.isBlank()) return categoryFiltered

    return categoryFiltered.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }
}
