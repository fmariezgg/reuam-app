package ni.edu.uam.reuam.navigation

sealed class Routes(val route: String) {
    data object Splash          : Routes("splash")
    data object Welcome         : Routes("welcome")
    data object Login           : Routes("login")
    data object Home            : Routes("home")
    data object ArticleList     : Routes("article_list")
    data object PublishArticle  : Routes("publish_article")
    data object ArticleDetail   : Routes("article_detail/{itemId}") {
        fun createRoute(itemId: String) = "article_detail/$itemId"
    }
    data object Requests        : Routes("requests")
    data object Profile         : Routes("profile")
    data object MyPublications  : Routes("my_publications")
    data object AdminPanel      : Routes("admin_panel")
}