package com.sionic.app.domain.chat

import com.sionic.app.domain.user.User
import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(
    name = "threads",
    indexes = [
        Index(name = "threads_user_id_idx", columnList = "user_id"),
        Index(name = "threads_user_last_chat_idx", columnList = "user_id, last_chat_at DESC")
    ]
)
class Thread(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    var lastChatAt: ZonedDateTime = ZonedDateTime.now()
)
