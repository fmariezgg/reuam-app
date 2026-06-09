package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.navigation.Routes
import ni.edu.uam.reuam.presentation.components.*
import ni.edu.uam.reuam.ui.theme.*

private data class Category(val id: String, val label: String, val icon: ImageVector)

private val categories = listOf(
    Category("all",        "Todos",       Icons.Outlined.MoreHoriz),
    Category("books",      "Libros",      Icons.Outlined.MenuBook),
    Category("tech",       "Tecnología",  Icons.Outlined.Laptop),
    Category("stationery", "Papelería",   Icons.Outlined.Edit),
    Category("uniforms",   "Uniformes",   Icons.Outlined.Checkroom),
    Category("lab",        "Laboratorio", Icons.Outlined.Science),
)

private val typeFilters = listOf(
    "all" to "Todos",
    "Donación" to "Donación",
    "Intercambio" to "Intercambio",
    "Préstamo" to "Préstamo",
    "Venta simbólica" to "Venta simbólica"
)

private data class ArticleSample(
    val id: Int, val imageUrl: String, val title: String,
    val category: String, val type: ArticleType,
    val status: ArticleStatus, val owner: String
)

private val allArticles = listOf(
    ArticleSample(1, "https://images.unsplash.com/photo-1598690042638-1b9844b7ef83?w=400",
        "Calculadora científica Casio FX-991", "Tecnología",
        ArticleType.PRESTAMO, ArticleStatus.DISPONIBLE, "Carlos M."),
    ArticleSample(2, "https://images.unsplash.com/photo-1676302447092-14a103558511?w=400",
        "Libro de Cálculo II - James Stewart", "Libros",
        ArticleType.DONACION, ArticleStatus.DISPONIBLE, "Ana R."),
    ArticleSample(3, "https://images.unsplash.com/photo-1580982172477-9373ff52ae43?w=400",
        "Bata de laboratorio blanca talla M", "Laboratorio",
        ArticleType.VENTA_SIMBOLICA, ArticleStatus.RESERVADO, "Luis P."),
    ArticleSample(4, "https://images.unsplash.com/photo-1615988938302-bd2a5a7023bc?w=400",
        "Pack de marcadores y folders", "Papelería",
        ArticleType.INTERCAMBIO, ArticleStatus.DISPONIBLE, "María G."),
    ArticleSample(5, "https://images.unsplash.com/photo-1492107376256-4026437926cd?w=400",
        "Cable USB-C nuevo en caja", "Tecnología",
        ArticleType.DONACION, ArticleStatus.ENTREGADO, "Pedro S."),
    ArticleSample(6, "https://images.unsplash.com/photo-1580982167011-2a54f6e440ca?w=400",
        "Kit de pipetas y tubos de ensayo", "Laboratorio",
        ArticleType.PRESTAMO, ArticleStatus.DISPONIBLE, "Sofía V."),
    ArticleSample(7, "https://images.unsplash.com/photo-1632571401005-458e9d244591?w=400",
        "Calculadora gráfica TI-84", "Tecnología",
        ArticleType.VENTA_SIMBOLICA, ArticleStatus.DISPONIBLE, "Roberto L."),
    ArticleSample(8, "https://images.unsplash.com/photo-1654931800100-2ecf6eee7c64?w=400",
        "Set de reglas y compás profesional", "Papelería",
        ArticleType.DONACION, ArticleStatus.DISPONIBLE, "Laura T."),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    currentRoute: String = Routes.ArticleList.route,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var activeCategory by remember { mutableStateOf("all") }
    var activeType     by remember { mutableStateOf("all") }
    var searchQuery    by remember { mutableStateOf("") }
    var showFilters    by remember { mutableStateOf(false) }

    val filtered = allArticles.filter { article ->
        val matchCat  = activeCategory == "all" || article.category.lowercase().contains(activeCategory)
        val matchType = activeType == "all" || article.type.label == activeType
        val matchQ    = searchQuery.isBlank() || article.title.lowercase().contains(searchQuery.lowercase())
        matchCat && matchType && matchQ
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp, color = ReUAMSurface) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás",
                                tint = ReUAMTextPrimary)
                        }
                        Text(
                            "Explorar artículos",
                            modifier = Modifier.weight(1f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ReUAMTextPrimary
                        )
                        IconButton(
                            onClick = { showFilters = !showFilters },
                            modifier = Modifier
                                .size(40.dp)
                                .background(ReUAMGreenSoft, CircleShape)
                        ) {
                            Icon(Icons.Outlined.Tune, "Filtros", tint = ReUAMGreen)
                        }
                    }
                    // Search bar sticky
                    Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                        ReUAMSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Buscar por nombre, categoría..."
                        )
                    }
                }
            }
        },
        bottomBar = {
            ReUAMBottomBar(currentRoute = currentRoute, onNavigate = onNavigate)
        },
        containerColor = ReUAMBackground
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // ── Panel de filtros plegable ─────────────────────────────────────
            if (showFilters) {
                item {
                    Surface(
                        color = ReUAMSurface,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Categorías
                            FilterSection(
                                title = "Categoría",
                                hasActiveFilter = activeCategory != "all",
                                onClear = { activeCategory = "all" }
                            ) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(categories) { cat ->
                                        CategoryChip(
                                            label = cat.label,
                                            isActive = activeCategory == cat.id,
                                            onClick = { activeCategory = cat.id },
                                            icon = cat.icon
                                        )
                                    }
                                }
                            }
                            // Tipos
                            FilterSection(
                                title = "Tipo de publicación",
                                hasActiveFilter = activeType != "all",
                                onClear = { activeType = "all" }
                            ) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(typeFilters) { (id, label) ->
                                        CategoryChip(
                                            label = label,
                                            isActive = activeType == id,
                                            onClick = { activeType = id }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Contador + limpiar ────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${filtered.size} artículos encontrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReUAMTextSecondary
                    )
                    if (activeCategory != "all" || activeType != "all" || searchQuery.isNotBlank()) {
                        TextButton(onClick = {
                            activeCategory = "all"; activeType = "all"; searchQuery = ""
                        }) {
                            Icon(Icons.Filled.Close, null, Modifier.size(14.dp), ReUAMGreen)
                            Spacer(Modifier.width(4.dp))
                            Text("Limpiar filtros", color = ReUAMGreen,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // ── Lista de artículos ────────────────────────────────────────────
            if (filtered.isEmpty()) {
                item { EmptyArticles(onClear = { activeCategory = "all"; activeType = "all"; searchQuery = "" }) }
            } else {
                items(filtered) { article ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        ArticleCard(
                            imageUrl = article.imageUrl,
                            title = article.title,
                            category = article.category,
                            type = article.type,
                            status = article.status,
                            owner = article.owner
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    hasActiveFilter: Boolean,
    onClear: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = ReUAMTextPrimary)
            if (hasActiveFilter) {
                TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
                    Text("Limpiar", color = ReUAMGreen,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        content()
    }
}

@Composable
private fun EmptyArticles(onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(ReUAMGreenSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Inventory2, null,
                Modifier.size(48.dp), ReUAMGreenLight)
        }
        Text("No encontramos artículos",
            fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ReUAMTextPrimary)
        Text("Intenta ajustar tus filtros o buscar con otras palabras clave",
            style = MaterialTheme.typography.bodyMedium, color = ReUAMTextSecondary)
        OutlinedButton(
            onClick = onClear,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ReUAMGreen)
        ) {
            Text("Limpiar búsqueda")
        }
    }
}
