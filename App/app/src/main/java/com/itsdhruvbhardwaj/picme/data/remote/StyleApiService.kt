package com.itsdhruvbhardwaj.picme.data.remote

import com.itsdhruvbhardwaj.picme.data.model.StyleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StyleApiService {
    @GET("api/styles")
    suspend fun getStyles(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("category") category: String? = null
    ): Response<StyleResponse>
}
