package com.sionic.app.domain.feedback

import com.sionic.app.domain.chat.Chat
import com.sionic.app.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun existsByUserAndChat(user: User, chat: Chat): Boolean
    fun findAllByUser(user: User, pageable: Pageable): Page<Feedback>
    fun findAllByUserAndIsPositive(user: User, isPositive: Boolean, pageable: Pageable): Page<Feedback>
    fun findAllByIsPositive(isPositive: Boolean, pageable: Pageable): Page<Feedback>
}
