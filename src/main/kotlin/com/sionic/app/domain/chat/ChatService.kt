package com.sionic.app.domain.chat

import com.sionic.app.domain.chat.dto.ChatResponse
import com.sionic.app.domain.chat.dto.ChatInThread
import com.sionic.app.domain.chat.dto.CreateChatRequest
import com.sionic.app.domain.chat.dto.PagedResponse
import com.sionic.app.domain.chat.dto.ThreadWithChatsResponse
import com.sionic.app.domain.report.ActivityEventType
import com.sionic.app.domain.report.ActivityLog
import com.sionic.app.domain.report.ActivityLogRepository
import com.sionic.app.domain.user.User
import com.sionic.app.domain.user.UserRepository
import com.sionic.app.exception.ForbiddenException
import com.sionic.app.exception.ThreadNotFoundException
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Service
class ChatService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val openAiClient: OpenAiClient,
    private val activityLogRepository: ActivityLogRepository,
    transactionManager: PlatformTransactionManager,
    @Value("\${app.openai.system-prompt}") private val systemPrompt: String,
    @Value("\${app.chat.stream.pool-size}") private val streamPoolSize: Int,
    @Value("\${app.chat.stream.timeout-ms}") private val streamTimeoutMs: Long
) {
    private val executor: ExecutorService = run {
        val counter = AtomicInteger()
        Executors.newFixedThreadPool(streamPoolSize) { r ->
            Thread(r, "chat-stream-${counter.incrementAndGet()}").apply { isDaemon = true }
        }
    }
    private val tx = TransactionTemplate(transactionManager)

    @PreDestroy
    fun shutdown() {
        executor.shutdown()
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow()
        }
    }

    @Transactional
    fun createChat(request: CreateChatRequest, userEmail: String): ChatResponse {
        val user = getUser(userEmail)
        val thread = resolveThread(user)
        val messages = buildMessages(thread, request.question)
        val answer = openAiClient.complete(request.model, messages)
        val chat = chatRepository.save(Chat(thread = thread, question = request.question, answer = answer))
        thread.lastChatAt = chat.createdAt
        threadRepository.save(thread)
        activityLogRepository.save(ActivityLog(user = user, eventType = ActivityEventType.CHAT_CREATED))
        return ChatResponse.from(chat)
    }

    fun createChatStreaming(request: CreateChatRequest, userEmail: String): SseEmitter {
        var threadId = 0L
        var messages: List<OpenAiMessage> = emptyList()
        tx.executeWithoutResult {
            val user = getUser(userEmail)
            val thread = resolveThread(user)
            threadId = thread.id
            messages = buildMessages(thread, request.question)
        }

        val emitter = SseEmitter(streamTimeoutMs)
        val cancelled = AtomicBoolean(false)
        emitter.onTimeout { cancelled.set(true) }
        emitter.onError { cancelled.set(true) }
        emitter.onCompletion { cancelled.set(true) }

        try {
            executor.submit {
                try {
                    openAiClient.stream(request.model, messages, emitter, cancelled::get) { fullAnswer ->
                        if (cancelled.get()) return@stream
                        tx.executeWithoutResult {
                            val thread = threadRepository.findById(threadId).orElseThrow()
                            val chat = chatRepository.save(
                                Chat(thread = thread, question = request.question, answer = fullAnswer)
                            )
                            thread.lastChatAt = chat.createdAt
                            threadRepository.save(thread)
                            activityLogRepository.save(ActivityLog(user = thread.user, eventType = ActivityEventType.CHAT_CREATED))
                        }
                    }
                } catch (e: Exception) {
                    if (cancelled.get()) return@submit
                    runCatching {
                        emitter.send(SseEmitter.event().data("[ERROR]"))
                        emitter.complete()
                    }
                }
            }
        } catch (e: RejectedExecutionException) {
            runCatching { emitter.completeWithError(e) }
        }
        return emitter
    }

    @Transactional(readOnly = true)
    fun getThreads(
        userEmail: String,
        isAdmin: Boolean,
        page: Int,
        size: Int,
        direction: String
    ): PagedResponse<ThreadWithChatsResponse> {
        val sortDir = if (direction.uppercase() == "ASC") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDir, "createdAt"))

        val threadPage = if (isAdmin) {
            threadRepository.findAll(pageable)
        } else {
            threadRepository.findAllByUser(getUser(userEmail), pageable)
        }

        val chatsMap = chatRepository
            .findAllByThreadInOrderByCreatedAtAsc(threadPage.content)
            .groupBy { it.thread.id }

        val content = threadPage.content.map { thread ->
            ThreadWithChatsResponse(
                threadId = thread.id,
                createdAt = thread.createdAt,
                chats = (chatsMap[thread.id] ?: emptyList()).map { ChatInThread.from(it) }
            )
        }

        return PagedResponse(
            content = content,
            page = threadPage.number,
            size = threadPage.size,
            totalElements = threadPage.totalElements,
            totalPages = threadPage.totalPages
        )
    }

    @Transactional
    fun deleteThread(threadId: Long, userEmail: String) {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { ThreadNotFoundException(threadId) }
        if (thread.user.email != userEmail) throw ForbiddenException()
        chatRepository.deleteAllByThread(thread)
        threadRepository.delete(thread)
    }

    private fun getUser(email: String): User =
        userRepository.findByEmail(email).orElseThrow { IllegalStateException("사용자를 찾을 수 없습니다: $email") }

    private fun resolveThread(user: User): Thread {
        val latest = threadRepository.findFirstByUserOrderByLastChatAtDesc(user).orElse(null)
        return if (latest == null || ChronoUnit.MINUTES.between(latest.lastChatAt, ZonedDateTime.now()) >= 30) {
            threadRepository.save(Thread(user = user))
        } else {
            latest
        }
    }

    private fun buildMessages(thread: Thread, question: String): List<OpenAiMessage> {
        val history = chatRepository.findAllByThreadOrderByCreatedAtAsc(thread)
        val messages = mutableListOf(OpenAiMessage("system", systemPrompt))
        for (chat in history) {
            messages += OpenAiMessage("user", chat.question)
            messages += OpenAiMessage("assistant", chat.answer)
        }
        messages += OpenAiMessage("user", question)
        return messages
    }
}
