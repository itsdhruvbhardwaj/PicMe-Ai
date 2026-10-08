package com.itsdhruvbhardwaj.picme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsdhruvbhardwaj.picme.data.model.Style
import com.itsdhruvbhardwaj.picme.data.repository.StyleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val categories: List<String>,
        val trendingStyles: List<Style>,
        val popularStyles: List<Style>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val styleRepository: StyleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            // In a real app, we might want to fetch these in parallel
            val trendingResult = styleRepository.getStyles(page = 1, limit = 10, category = "Trending")
            val popularResult = styleRepository.getStyles(page = 1, limit = 10, category = null) // All as popular
            
            if (trendingResult.isSuccess && popularResult.isSuccess) {
                _uiState.value = HomeUiState.Success(
                    categories = listOf("All", "Trending", "Portrait", "Artistic"),
                    trendingStyles = trendingResult.getOrThrow().styles,
                    popularStyles = popularResult.getOrThrow().styles
                )
            } else {
                val error = trendingResult.exceptionOrNull() ?: popularResult.exceptionOrNull()
                _uiState.value = HomeUiState.Error(error?.message ?: "Unknown error occurred")
            }
        }
    }
}
