package ni.edu.uam.reuam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ni.edu.uam.reuam.presentation.articles.ArticleListScreen
import ni.edu.uam.reuam.presentation.articles.PublishArticleScreen
import ni.edu.uam.reuam.presentation.auth.LoginScreen
import ni.edu.uam.reuam.presentation.auth.SplashScreen
import ni.edu.uam.reuam.presentation.auth.WelcomeScreen
import ni.edu.uam.reuam.presentation.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: Routes.Home.route

    // Navega desde el BottomBar sin apilar pantallas duplicadas
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
        startDestination = Routes.Splash.route   // ← CORREGIDO: Splash como punto de entrada
    ) {

        composable(Routes.Splash.route) {
            SplashScreen(
                onFinish = {
                    navController.navigate(Routes.Welcome.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Welcome.route) {
            WelcomeScreen(
                onStartClick = { navController.navigate(Routes.Login.route) },
                onLoginClick = { navController.navigate(Routes.Login.route) }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                currentRoute   = currentRoute,
                onNavigate     = { navigateTab(it) },
                onPublishClick = { navController.navigate(Routes.PublishArticle.route) }
            )
        }

        composable(Routes.ArticleList.route) {
            ArticleListScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PublishArticle.route) {
            PublishArticleScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Placeholders para las rutas del BottomBar aún no implementadas
        composable(Routes.Requests.route) {
            ArticleListScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.Profile.route) {
            ArticleListScreen(onBackClick = { navController.popBackStack() })
        }
    }
}