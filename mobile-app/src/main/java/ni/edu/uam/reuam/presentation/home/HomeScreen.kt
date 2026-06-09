package ni.edu.uam.reuam.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.navigation.Routes
import ni.edu.uam.reuam.presentation.components.*
import ni.edu.uam.reuam.ui.theme.*

// ── Datos de muestra ──────────────────────────────────────────────────────────

private data class Category(val id: String, val label: String, val icon: ImageVector)

private val sampleCategories = listOf(
    Category("all",        "Todos",       Icons.Outlined.MoreHoriz),
    Category("books",      "Libros",      Icons.Outlined.MenuBook),
    Category("tech",       "Tecnología",  Icons.Outlined.Laptop),
    Category("stationery", "Papelería",   Icons.Outlined.Edit),
    Category("uniforms",   "Uniformes",   Icons.Outlined.Checkroom),
    Category("lab",        "Laboratorio", Icons.Outlined.Science),
)

private data class ArticleSample(
    val id: Int,
    val imageUrl: String,
    val title: String,
    val category: String,
    val type: ArticleType,
    val status: ArticleStatus,
    val owner: String
)

private val sampleArticles = listOf(
    ArticleSample(1,
        "https://images.unsplash.com/photo-1598690042638-1b9844b7ef83?w=400",
        "Calculadora científica Casio FX-991",
        "Tecnología", ArticleType.PRESTAMO, ArticleStatus.DISPONIBLE, "Carlos M."),
    ArticleSample(2,
        "https://images.unsplash.com/photo-1676302447092-14a103558511?w=400",
        "Libro de Cálculo II - James Stewart",
        "Libros", ArticleType.DONACION, ArticleStatus.DISPONIBLE, "Ana R."),
    ArticleSample(3,
        "https://images.unsplash.com/photo-1580982172477-9373ff52ae43?w=400",
        "Bata de laboratorio blanca talla M",
        "Laboratorio", ArticleType.VENTA_SIMBOLICA, ArticleStatus.RESERVADO, "Luis P."),
    ArticleSample(4,
        "https://images.unsplash.com/photo-1615988938302-bd2a5a7023bc?w=400",
        "Pack de marcadores y folders",
        "Papelería", ArticleType.INTERCAMBIO, ArticleStatus.DISPONIBLE, "María G."),
    ArticleSample(5,
        "https://images.unsplash.com/photo-1492107376256-4026437926cd?w=400",
        "Cable USB-C nuevo en caja",
        "Tecnología", ArticleType.DONACION, ArticleStatus.ENTREGADO, "Pedro S."),
    ArticleSample(6,
        "https://images.unsplash.com/photo-1580982167011-2a54f6e440ca?w=400",
        "Kit de pipetas y tubos de ensayo",
        "Laboratorio", ArticleType.PRESTAMO, ArticleStatus.DISPONIBLE, "Sofía V."),
)

// ── Pantalla ──────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    currentRoute: String = Routes.Home.route,
    onNavigate: (String) -> Unit = {},
    onPublishClick: () -> Unit = {}
) {
    var activeCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            ReUAMBottomBar(currentRoute = currentRoute, onNavigate = onNavigate)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onPublishClick,
                containerColor = ReUAMGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-72).dp) // sube por encima del BottomBar
            ) {
                Icon(Icons.Filled.Add, "Publicar artículo", Modifier.size(28.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = ReUAMBackground
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            // ── HEADER verde con gradiente ────────────────────────────────────
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
                    // Saludo + campanita
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Hola, Fátima 👋",
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

                    // Barra de búsqueda
                    ReUAMSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Buscar artículos, libros, tecnología...",
                        modifier = Modifier.fillMaxWidth()
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
                    items(sampleCategories) { cat ->
                        CategoryChip(
                            label = cat.label,
                            isActive = activeCategory == cat.id,
                            onClick = { activeCategory = cat.id },
                            icon = cat.icon
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
                    TextButton(onClick = { onNavigate(Routes.ArticleList.route) }) {
                        Text("Ver todos", color = ReUAMGreen,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Grid 2 columnas
                val filteredArticles = if (activeCategory == "all") sampleArticles
                else sampleArticles.filter { it.category.lowercase().contains(activeCategory) }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(
                        // Altura aproximada: ceil(n/2) * (cardHeight + gap)
                        ((filteredArticles.size + 1) / 2 * 300).dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false // el scroll lo maneja el Column externo
                ) {
                    items(filteredArticles) { article ->
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}