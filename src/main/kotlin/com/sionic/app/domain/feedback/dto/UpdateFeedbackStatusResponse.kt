package com.sionic.app.domain.feedback.dto

import com.sionic.app.domain.feedback.FeedbackStatus

data class UpdateFeedbackStatusResponse(
    val feedbackId: Long,
    val status: FeedbackStatus
)
