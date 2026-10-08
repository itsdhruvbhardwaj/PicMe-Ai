package com.itsdhruvbhardwaj.picme.data.repository

import com.itsdhruvbhardwaj.picme.data.model.Style
import com.itsdhruvbhardwaj.picme.data.model.StyleResponse
import com.itsdhruvbhardwaj.picme.data.remote.StyleApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class StyleRepository(
    private val apiService: StyleApiService
) {
    // Cache to store styles that have been fetched
    private val _cachedStyles = MutableStateFlow<Map<String, Style>>(emptyMap())
    val cachedStyles: StateFlow<Map<String, Style>> = _cachedStyles.asStateFlow()

    suspend fun getStyles(
        page: Int,
        limit: Int,
        category: String? = null
    ): Result<StyleResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStyles(page, limit, category)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Update cache with new styles
                _cachedStyles.update { currentCache ->
                    currentCache + body.styles.associateBy { it.id }
                }
                Result.success(body)
            } else {
                Result.failure(Exception("Failed to fetch styles: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getStyleById(styleId: String): Style? {
        return _cachedStyles.value[styleId]
    }
}
