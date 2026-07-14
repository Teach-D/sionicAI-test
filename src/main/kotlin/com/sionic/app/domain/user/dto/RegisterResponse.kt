package com.sionic.app.domain.user.dto

import com.sionic.app.domain.user.User
import java.time.ZonedDateTime

data class RegisterResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: String,
    val createdAt: ZonedDateTime
) {
    companion object {
        fun from(user: User) = RegisterResponse(
            id = user.id,
            email = user.email,
            name = user.name,
            role = user.role.name,
            createdAt = user.createdAt
        )
    }
}
