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
import ni.edu.uam.reuam.presentation.articles.PublishArticleScreen
import ni.edu.uam.reuam.presentation.auth.AuthViewModel
import ni.edu.uam.reuam.presentation.auth.LoginScreen
import ni.edu.uam.reuam.presentation.auth.SplashScreen
import ni.edu.uam.reuam.presentation.auth.WelcomeScreen
import ni.edu.uam.reuam.presentation.home.HomeScreen
import ni.edu.uam.reuam.presentation.profile.ProfileScreen

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

        composable(Routes.Home.route) {
            HomeScreen(
                currentRoute   = currentRoute,
                onNavigate     = { navigateTab(it) },
                onPublishClick = { navController.navigate(Routes.PublishArticle.route) },
                onArticleClick = { itemId -> navController.navigate(Routes.ArticleDetail.createRoute(itemId)) }
            )
        }

        composable(Routes.ArticleList.route) {
            ArticleListScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Placeholder temporal — la pantalla real de detalle llega en la
        // Fase 4. Por ahora solo registra la ruta con argumento para que
        // el clic en una tarjeta de artículo no provoque un crash de
        // "ruta no encontrada".
        composable(
            route = Routes.ArticleDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            ArticleListScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.PublishArticle.route) {
            PublishArticleScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.Requests.route) {
            ArticleListScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.Profile.route) {
            val context = LocalContext.current
            ProfileScreen(
                onLogoutClick = {
                    authViewModel.signOut(context)
                    navController.navigate(Routes.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}