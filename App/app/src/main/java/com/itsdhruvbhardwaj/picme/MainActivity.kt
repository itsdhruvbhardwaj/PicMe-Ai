package com.itsdhruvbhardwaj.picme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.itsdhruvbhardwaj.picme.data.local.AuthTokenStorage
import com.itsdhruvbhardwaj.picme.data.remote.AuthApiService
import com.itsdhruvbhardwaj.picme.data.remote.StyleApiService
import com.itsdhruvbhardwaj.picme.data.repository.AuthRepository
import com.itsdhruvbhardwaj.picme.data.repository.StyleRepository
import com.itsdhruvbhardwaj.picme.navigation.NavGraph
import com.itsdhruvbhardwaj.picme.ui.auth.AuthViewModel
import com.itsdhruvbhardwaj.picme.ui.screens.home.HomeViewModel
import com.itsdhruvbhardwaj.picme.ui.screens.home.StylesGridViewModel
import com.itsdhruvbhardwaj.picme.ui.theme.PicMeTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Manual Dependency Injection
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val authApiService = retrofit.create(AuthApiService::class.java)
        val styleApiService = retrofit.create(StyleApiService::class.java)
        
        val tokenStorage = AuthTokenStorage(applicationContext)
        
        val authRepository = AuthRepository(authApiService, tokenStorage)
        val styleRepository = StyleRepository(styleApiService)
        
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> 
                        AuthViewModel(authRepository) as T
                    modelClass.isAssignableFrom(HomeViewModel::class.java) -> 
                        HomeViewModel(styleRepository) as T
                    // Note: StylesGridViewModel needs category, so it might be handled differently 
                    // or via a separate factory/assisted injection if we used Hilt.
                    // For now, we'll handle StylesGridViewModel instantiation in NavGraph or via a factory that can handle params.
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        val authViewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]
        val homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        setContent {
            PicMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        homeViewModel = homeViewModel,
                        styleRepository = styleRepository // Pass repository to handle param-based VMs in NavGraph
                    )
                }
            }
        }
    }
}
