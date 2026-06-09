package ni.edu.uam.reuam.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.navigation.Routes
import ni.edu.uam.reuam.ui.theme.*

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun ReUAMBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(Routes.Home.route,            "Inicio",      Icons.Filled.Home,        Icons.Outlined.Home),
        BottomNavItem(Routes.ArticleList.route,     "Buscar",      Icons.Filled.Search,      Icons.Outlined.Search),
        BottomNavItem(Routes.PublishArticle.route,  "Publicar",    Icons.Filled.AddCircle,   Icons.Outlined.AddCircle),
        BottomNavItem(Routes.Requests.route,        "Solicitudes", Icons.Filled.Description, Icons.Outlined.Description),
        BottomNavItem(Routes.Profile.route,         "Perfil",      Icons.Filled.Person,      Icons.Outlined.Person),
    )

    Surface(
        color = ReUAMSurface,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isActive = currentRoute == item.route
                BottomNavTab(
                    item = item,
                    isActive = isActive,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    item: BottomNavItem,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isActive) ReUAMGreen else ReUAMTextSecondary

    TextButton(
        onClick = onClick,
        modifier = Modifier.width(64.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (isActive) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            Text(
                text = item.label,
                fontSize = 10.sp,
                color = contentColor,
                maxLines = 1
            )
            // Indicador activo
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .background(ReUAMGreen, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}