package com.sionic.app.domain.feedback.dto

import com.sionic.app.domain.feedback.FeedbackStatus
import jakarta.validation.constraints.NotNull

data class UpdateFeedbackStatusRequest(
    @field:NotNull val status: FeedbackStatus?
)
