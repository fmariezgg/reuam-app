package ni.edu.uam.reuam.navigation

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")
    data object Welcome : Routes("welcome")
    data object Login : Routes("login")
    data object Home : Routes("home")
    data object PublishArticle : Routes("publish_article")
    data object ArticleDetail : Routes("article_detail")
    data object MyPublications : Routes("my_publications")
    data object Requests : Routes("requests")
    data object Profile : Routes("profile")
    data object AdminPanel : Routes("admin_panel")
}