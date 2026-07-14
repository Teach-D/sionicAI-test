package com.sionic.app.domain.feedback

import com.sionic.app.domain.chat.Chat
import com.sionic.app.domain.user.User
import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [UniqueConstraint(name = "feedbacks_user_chat_uq", columnNames = ["user_id", "chat_id"])],
    indexes = [
        Index(name = "feedbacks_user_id_idx", columnList = "user_id"),
        Index(name = "feedbacks_chat_id_idx", columnList = "chat_id"),
        Index(name = "feedbacks_created_at_idx", columnList = "created_at")
    ]
)
class Feedback(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,

    @Column(nullable = false)
    val isPositive: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus = FeedbackStatus.PENDING,

    @Column(nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)
