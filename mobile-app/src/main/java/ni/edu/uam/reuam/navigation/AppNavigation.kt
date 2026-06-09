package ni.edu.uam.reuam.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                onFinish = {
                    navController.navigate(Routes.Welcome.route) {
                        popUpTo(Routes.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.Welcome.route) {
            WelcomeScreen(
                onStartClick = {
                    navController.navigate(Routes.Login.route)
                },
                onLoginClick = {
                    navController.navigate(Routes.Login.route)
                }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Welcome.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                currentRoute = Routes.Home.route,
                onNavigate = { route ->
                    navController.navigateSafely(route)
                },
                onPublishClick = {
                    navController.navigateSafely(Routes.PublishArticle.route)
                }
            )
        }

        composable(Routes.ArticleList.route) {
            ArticleListScreen(
                currentRoute = Routes.ArticleList.route,
                onNavigate = { route ->
                    navController.navigateSafely(route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PublishArticle.route) {
            PublishArticleScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun NavHostController.navigateSafely(route: String) {
    if (currentDestination?.route != route) {
        navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }
}