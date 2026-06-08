package ni.edu.uam.reuam.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ni.edu.uam.reuam.presentation.auth.LoginScreen
import ni.edu.uam.reuam.presentation.auth.WelcomeScreen
import ni.edu.uam.reuam.presentation.home.HomeScreen
import ni.edu.uam.reuam.presentation.articles.PublishArticleScreen
import ni.edu.uam.reuam.presentation.auth.SplashScreen

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
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onPublishClick = {
                    navController.navigate(Routes.PublishArticle.route)
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