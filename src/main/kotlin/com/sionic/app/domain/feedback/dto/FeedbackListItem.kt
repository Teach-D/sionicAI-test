package com.sionic.app.domain.feedback.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.sionic.app.domain.feedback.Feedback
import com.sionic.app.domain.feedback.FeedbackStatus
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FeedbackListItem(
    val feedbackId: Long,
    val chatId: Long,
    val userId: Long?,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: ZonedDateTime
) {
    companion object {
        fun from(feedback: Feedback, isAdmin: Boolean) = FeedbackListItem(
            feedbackId = feedback.id,
            chatId = feedback.chat.id,
            userId = if (isAdmin) feedback.user.id else null,
            isPositive = feedback.isPositive,
            status = feedback.status,
            createdAt = feedback.createdAt
        )
    }
}
