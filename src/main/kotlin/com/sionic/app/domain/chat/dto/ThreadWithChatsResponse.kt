package com.sionic.app.domain.chat.dto

import com.sionic.app.domain.chat.Chat
import com.sionic.app.domain.chat.Thread
import java.time.ZonedDateTime

data class ThreadWithChatsResponse(
    val threadId: Long,
    val createdAt: ZonedDateTime,
    val chats: List<ChatInThread>
) {
    companion object {
        fun from(thread: Thread, chats: List<Chat>) = ThreadWithChatsResponse(
            threadId = thread.id,
            createdAt = thread.createdAt,
            chats = chats.map { ChatInThread.from(it) }
        )
    }
}

data class ChatInThread(
    val chatId: Long,
    val question: String,
    val answer: String,
    val createdAt: ZonedDateTime
) {
    companion object {
        fun from(chat: Chat) = ChatInThread(
            chatId = chat.id,
            question = chat.question,
            answer = chat.answer,
            createdAt = chat.createdAt
        )
    }
}
