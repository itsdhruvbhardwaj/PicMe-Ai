package com.itsdhruvbhardwaj.picme.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Home : Screen("home")
    data object Explore : Screen("explore")
    data object Create : Screen("create")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object StyleDetails : Screen("style_details/{styleId}") {
        fun createRoute(styleId: String) = "style_details/$styleId"
    }
    data object SelectPhotos : Screen("select_photos")
    data object Generation : Screen("generation")
    data object Result : Screen("result")
    
    data object StylesGrid : Screen("styles_grid?category={category}") {
        fun createRoute(category: String? = null) = if (category != null) "styles_grid?category=$category" else "styles_grid"
    }
}
