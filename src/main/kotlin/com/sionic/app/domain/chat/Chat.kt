package com.sionic.app.domain.chat

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(
    name = "chats",
    indexes = [Index(name = "chats_thread_id_idx", columnList = "thread_id")]
)
class Chat(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    val thread: Thread,

    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val answer: String,

    @Column(nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)
