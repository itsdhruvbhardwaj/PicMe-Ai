package com.itsdhruvbhardwaj.picme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsdhruvbhardwaj.picme.data.model.Style
import com.itsdhruvbhardwaj.picme.data.repository.StyleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StyleDetailsUiState {
    object Loading : StyleDetailsUiState
    data class Success(val style: Style) : StyleDetailsUiState
    data class Error(val message: String) : StyleDetailsUiState
}

class StyleDetailsViewModel(
    private val styleRepository: StyleRepository,
    private val styleId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<StyleDetailsUiState>(StyleDetailsUiState.Loading)
    val uiState: StateFlow<StyleDetailsUiState> = _uiState.asStateFlow()

    init {
        loadStyleDetails()
    }

    private fun loadStyleDetails() {
        viewModelScope.launch {
            _uiState.value = StyleDetailsUiState.Loading
            
            // First try to get from cache
            val cachedStyle = styleRepository.getStyleById(styleId)
            if (cachedStyle != null) {
                _uiState.value = StyleDetailsUiState.Success(cachedStyle)
            } else {
                // If not in cache (e.g. direct link or process death), we fetch it.
                // Since our API currently only supports list/pagination, we fetch a small page to find it.
                // In a production app, we'd have a specific GET /api/styles/{id} endpoint.
                val result = styleRepository.getStyles(page = 1, limit = 50)
                result.fold(
                    onSuccess = { response ->
                        val style = response.styles.find { it.id == styleId }
                        if (style != null) {
                            _uiState.value = StyleDetailsUiState.Success(style)
                        } else {
                            _uiState.value = StyleDetailsUiState.Error("Style not found")
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = StyleDetailsUiState.Error(error.message ?: "Failed to load style details")
                    }
                )
            }
        }
    }
}
