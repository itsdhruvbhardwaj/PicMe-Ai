package com.itsdhruvbhardwaj.picme.data.model

data class Style(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val prompt: String = "",
    val thumbnail: String? = null,
    val previewImages: List<String> = emptyList(),
    val imageCount: Int = 1,
    val category: String = ""
)

data class PaginationMetadata(
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val hasNextPage: Boolean = false
)

data class StyleResponse(
    val styles: List<Style> = emptyList(),
    val pagination: PaginationMetadata = PaginationMetadata()
)
