package com.sionic.app.domain.feedback.dto

import jakarta.validation.constraints.NotNull

data class CreateFeedbackRequest(
    @field:NotNull val chatId: Long?,
    @field:NotNull val isPositive: Boolean?
)
