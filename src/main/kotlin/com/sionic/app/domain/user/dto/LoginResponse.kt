package com.sionic.app.domain.user.dto

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)
