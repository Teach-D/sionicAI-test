package com.sionic.app.domain.feedback

import com.sionic.app.domain.chat.ChatRepository
import com.sionic.app.domain.feedback.dto.*
import com.sionic.app.domain.user.User
import com.sionic.app.domain.user.UserRepository
import com.sionic.app.exception.ChatNotFoundException
import com.sionic.app.exception.FeedbackAlreadyExistsException
import com.sionic.app.exception.FeedbackNotFoundException
import com.sionic.app.exception.ForbiddenException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val feedbackRepository: FeedbackRepository
) {

    @Transactional
    fun createFeedback(request: CreateFeedbackRequest, userEmail: String, isAdmin: Boolean): FeedbackResponse {
        val user = getUser(userEmail)
        val chat = chatRepository.findById(request.chatId!!)
            .orElseThrow { ChatNotFoundException(request.chatId) }

        if (!isAdmin && chat.thread.user.id != user.id) throw ForbiddenException()
        if (feedbackRepository.existsByUserAndChat(user, chat)) throw FeedbackAlreadyExistsException()

        val feedback = feedbackRepository.save(Feedback(user = user, chat = chat, isPositive = request.isPositive!!))
        return FeedbackResponse.from(feedback)
    }

    @Transactional(readOnly = true)
    fun getFeedbacks(
        userEmail: String,
        isAdmin: Boolean,
        page: Int,
        size: Int,
        direction: String,
        isPositive: Boolean?
    ): PagedResponse<FeedbackListItem> {
        val pageable = PageRequest.of(
            page, size,
            Sort.by(if (direction.uppercase() == "ASC") Sort.Direction.ASC else Sort.Direction.DESC, "createdAt")
        )

        val feedbackPage = if (isAdmin) {
            if (isPositive != null) feedbackRepository.findAllByIsPositive(isPositive, pageable)
            else feedbackRepository.findAll(pageable)
        } else {
            val user = getUser(userEmail)
            if (isPositive != null) feedbackRepository.findAllByUserAndIsPositive(user, isPositive, pageable)
            else feedbackRepository.findAllByUser(user, pageable)
        }

        return PagedResponse(
            content = feedbackPage.content.map { FeedbackListItem.from(it, isAdmin) },
            page = feedbackPage.number,
            size = feedbackPage.size,
            totalElements = feedbackPage.totalElements,
            totalPages = feedbackPage.totalPages
        )
    }

    @Transactional
    fun updateFeedbackStatus(feedbackId: Long, request: UpdateFeedbackStatusRequest): UpdateFeedbackStatusResponse {
        val feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow { FeedbackNotFoundException(feedbackId) }

        if (feedback.status == FeedbackStatus.RESOLVED && request.status == FeedbackStatus.PENDING) {
            throw IllegalArgumentException("RESOLVED 상태에서 PENDING으로 되돌릴 수 없습니다.")
        }

        feedback.status = request.status!!
        return UpdateFeedbackStatusResponse(feedbackId = feedback.id, status = feedback.status)
    }

    private fun getUser(email: String): User =
        userRepository.findByEmail(email).orElseThrow { IllegalStateException("사용자를 찾을 수 없습니다: $email") }
}
