package ni.edu.uam.reuam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ni.edu.uam.reuam.presentation.admin.AdminPanelScreen
import ni.edu.uam.reuam.presentation.articles.ArticleListScreen
import ni.edu.uam.reuam.presentation.articles.PublishArticleScreen
import ni.edu.uam.reuam.presentation.auth.LoginScreen
import ni.edu.uam.reuam.presentation.auth.SplashScreen
import ni.edu.uam.reuam.presentation.auth.WelcomeScreen
import ni.edu.uam.reuam.presentation.home.HomeScreen
import ni.edu.uam.reuam.presentation.profile.ProfileScreen
import ni.edu.uam.reuam.presentation.publications.MyPublicationsScreen
import ni.edu.uam.reuam.presentation.requests.RequestsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: Routes.Home.route

    // Navegación del BottomBar sin apilar pantallas duplicadas
    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {

        // ── Auth ──────────────────────────────────────────────────────────────

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

        // ── Tabs principales ──────────────────────────────────────────────────

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

        composable(Routes.Requests.route) {
            RequestsScreen(
                currentRoute = currentRoute,
                onNavigate   = { navigateTab(it) },
                onBack       = { navController.popBackStack() }
            )
        }

        composable(Routes.Profile.route) {
            ProfileScreen(
                currentRoute      = currentRoute,
                onNavigate        = { navigateTab(it) },
                onMyPublications  = { navController.navigate(Routes.MyPublications.route) },
                onRequests        = { navController.navigate(Routes.Requests.route) },
                onLogout          = {
                    navController.navigate(Routes.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Pantallas secundarias ─────────────────────────────────────────────

        composable(Routes.PublishArticle.route) {
            PublishArticleScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.MyPublications.route) {
            MyPublicationsScreen(
                currentRoute  = currentRoute,
                onNavigate    = { navigateTab(it) },
                onBack        = { navController.popBackStack() },
                onPublishNew  = { navController.navigate(Routes.PublishArticle.route) }
            )
        }

        composable(Routes.AdminPanel.route) {
            AdminPanelScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}