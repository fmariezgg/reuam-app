package ni.edu.uam.reuam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ni.edu.uam.reuam.presentation.articles.ArticleListScreen
import ni.edu.uam.reuam.presentation.articles.ItemDetailScreen
import ni.edu.uam.reuam.presentation.articles.MyPublicationsScreen
import ni.edu.uam.reuam.presentation.articles.PublishArticleScreen
import ni.edu.uam.reuam.presentation.admin.AdminPanelScreen
import ni.edu.uam.reuam.presentation.auth.AuthViewModel
import ni.edu.uam.reuam.presentation.auth.LoginScreen
import ni.edu.uam.reuam.presentation.auth.SplashScreen
import ni.edu.uam.reuam.presentation.auth.WelcomeScreen
import ni.edu.uam.reuam.presentation.home.HomeScreen
import ni.edu.uam.reuam.presentation.profile.ProfileScreen
import ni.edu.uam.reuam.presentation.requests.CreateRequestScreen
import ni.edu.uam.reuam.presentation.requests.RequestsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: Routes.Home.route

    // ViewModel compartido entre Login y cualquier pantalla que necesite el usuario
    val authViewModel: AuthViewModel = viewModel()

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {

        composable(Routes.Splash.route) {
            SplashScreen(
                onFinish = {
                    // Si ya hay sesión activa → ir directo a Home; si no → Welcome
                    val destination = if (authViewModel.isLoggedIn) {
                        Routes.Home.route
                    } else {
                        Routes.Welcome.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Welcome.route) {
            WelcomeScreen(
                onStartClick = { navController.navigate(Routes.Login.route) },
                onLoginClick  = { navController.navigate(Routes.Login.route) }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        // Limpia Welcome y Login del back-stack; el usuario no debe
                        // poder "volver atrás" a la pantalla de login tras autenticarse
                        popUpTo(Routes.Welcome.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Routes.Home.route) { backStackEntry ->
            // Si PublishArticleScreen señaló "refresh" al volver, recarga
            // Home una vez y limpia la señal para no recargar en loop.
            val shouldRefresh = backStackEntry.savedStateHandle
                .remove<Boolean>("should_refresh_home") ?: false

            HomeScreen(
                currentRoute   = currentRoute,
                onNavigate     = { navigateTab(it) },
                onPublishClick = { navController.navigate(Routes.PublishArticle.route) },
                onArticleClick = { itemId -> navController.navigate(Routes.ArticleDetail.createRoute(itemId)) },
                shouldRefresh = shouldRefresh,
            )
        }

        composable(Routes.ArticleList.route) {
            ArticleListScreen(
                onBackClick = { navController.popBackStack() },
                onArticleClick = { itemId -> navController.navigate(Routes.ArticleDetail.createRoute(itemId)) }
            )
        }

        // Detalle real de artículo (Fase 4). "Solicitar" navega a la pantalla
        // real de crear solicitud (Fase 5). "Editar" navega al formulario de
        // edición (Fase 6), reutilizando PublishArticleScreen en modo edición.
        composable(
            route = Routes.ArticleDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shouldRefresh = backStackEntry.savedStateHandle
                .remove<Boolean>("should_refresh_detail") ?: false

            ItemDetailScreen(
                onBackClick = { navController.popBackStack() },
                onRequestClick = { item ->
                    navController.navigate(Routes.CreateRequest.createRoute(item.id))
                },
                onEditClick = { item ->
                    navController.navigate(Routes.EditArticle.createRoute(item.id))
                },
                shouldRefresh = shouldRefresh,
            )
        }

        composable(Routes.PublishArticle.route) {
            PublishArticleScreen(
                onBackClick = { navController.popBackStack() },
                onPublished = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_home", true)
                    navController.popBackStack()
                },
            )
        }

        // Reutiliza PublishArticleScreen/PublishArticleViewModel: al recibir
        // el argumento "itemId" en la ruta, el ViewModel entra en modo
        // edición automáticamente (ver isEditMode en PublishArticleViewModel).
        composable(
            route = Routes.EditArticle.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            PublishArticleScreen(
                onBackClick = { navController.popBackStack() },
                onPublished = {
                    // El detalle al que se vuelve necesita recargar para ver
                    // los datos actualizados; Home también, por si el título
                    // o categoría cambiados afectan lo que ya se ve ahí.
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_detail", true)
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Routes.CreateRequest.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            CreateRequestScreen(
                onBackClick = { navController.popBackStack() },
                onRequestSent = { navController.popBackStack() },
            )
        }

        composable(Routes.Requests.route) {
            RequestsScreen(
                onBackClick = { navController.popBackStack() },
                onArticleClick = { itemId -> navController.navigate(Routes.ArticleDetail.createRoute(itemId)) }
            )
        }

        composable(Routes.MyPublications.route) {
            MyPublicationsScreen(
                onBackClick = { navController.popBackStack() },
                onArticleClick = { itemId -> navController.navigate(Routes.ArticleDetail.createRoute(itemId)) },
                onPublishClick = { navController.navigate(Routes.PublishArticle.route) },
                onEditArticleClick = { itemId -> navController.navigate(Routes.EditArticle.createRoute(itemId)) },
            )
        }

        composable(Routes.AdminPanel.route) {
            AdminPanelScreen(
                onBackClick = { navController.popBackStack() },
                isAdmin = true,
            )
        }

        composable(Routes.Profile.route) {
            val context = LocalContext.current
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.signOut(context)
                    navController.navigate(Routes.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onMyPublicationsClick = { navController.navigate(Routes.MyPublications.route) },
                onRequestsClick = { navController.navigate(Routes.Requests.route) },
                onAdminPanelClick = { navController.navigate(Routes.AdminPanel.route) },
            )
        }
    }
}
