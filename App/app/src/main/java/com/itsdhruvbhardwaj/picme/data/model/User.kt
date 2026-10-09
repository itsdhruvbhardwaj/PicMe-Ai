package com.itsdhruvbhardwaj.picme.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean,
    val isActive: Boolean = true,
    val profileImage: String? = null
)
