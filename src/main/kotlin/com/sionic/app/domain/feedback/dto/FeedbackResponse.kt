package com.sionic.app.domain.feedback.dto

import com.sionic.app.domain.feedback.Feedback
import com.sionic.app.domain.feedback.FeedbackStatus
import java.time.ZonedDateTime

data class FeedbackResponse(
    val feedbackId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: ZonedDateTime
) {
    companion object {
        fun from(feedback: Feedback) = FeedbackResponse(
            feedbackId = feedback.id,
            chatId = feedback.chat.id,
            isPositive = feedback.isPositive,
            status = feedback.status,
            createdAt = feedback.createdAt
        )
    }
}
