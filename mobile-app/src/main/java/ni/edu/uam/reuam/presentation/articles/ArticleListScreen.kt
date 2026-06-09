package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ni.edu.uam.reuam.presentation.components.*
import ni.edu.uam.reuam.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

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

            Text(
                text = "Todos los artículos",
                style = MaterialTheme.typography.titleMedium,
                color = ReUAMTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Por ahora, como es una pantalla de "error" o "corrección", 
            // mostramos un estado vacío o una rejilla simple si tuviéramos datos.
            EmptyContent(
                title = "Próximamente",
                message = "La lista completa de artículos estará disponible pronto.",
                modifier = Modifier.weight(1f)
            )
        }
    }
}
