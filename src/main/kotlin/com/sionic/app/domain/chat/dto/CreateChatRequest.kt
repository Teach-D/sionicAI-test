package com.sionic.app.domain.chat.dto

import jakarta.validation.constraints.NotBlank

data class CreateChatRequest(
    @field:NotBlank val question: String,
    val isStreaming: Boolean = false,
    val model: String = "gpt-4o-mini"
)
