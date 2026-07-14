package com.sionic.app.domain.chat.dto

import com.sionic.app.domain.chat.Chat
import java.time.ZonedDateTime

data class ChatResponse(
    val chatId: Long,
    val threadId: Long,
    val question: String,
    val answer: String,
    val createdAt: ZonedDateTime
) {
    companion object {
        fun from(chat: Chat) = ChatResponse(
            chatId = chat.id,
            threadId = chat.thread.id,
            question = chat.question,
            answer = chat.answer,
            createdAt = chat.createdAt
        )
    }
}
