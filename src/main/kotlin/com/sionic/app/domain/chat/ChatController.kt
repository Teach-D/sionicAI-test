package com.sionic.app.domain.chat

import com.sionic.app.domain.chat.dto.CreateChatRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ChatController(private val chatService: ChatService) {

    @PostMapping("/chats", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE])
    fun createChat(
        @Valid @RequestBody request: CreateChatRequest,
        authentication: Authentication
    ): Any {
        val userEmail = authentication.name
        return if (request.isStreaming) {
            chatService.createChatStreaming(request, userEmail)
        } else {
            ResponseEntity.status(HttpStatus.CREATED).body(chatService.createChat(request, userEmail))
        }
    }

    @GetMapping("/threads")
    fun getThreads(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "DESC") direction: String,
        authentication: Authentication
    ) = chatService.getThreads(
        userEmail = authentication.name,
        isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" },
        page = page,
        size = size,
        direction = direction
    )

    @DeleteMapping("/threads/{threadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteThread(@PathVariable threadId: Long, authentication: Authentication) =
        chatService.deleteThread(threadId, authentication.name)
}
