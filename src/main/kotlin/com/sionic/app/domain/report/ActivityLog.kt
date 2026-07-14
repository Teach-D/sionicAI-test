package com.sionic.app.domain.report

import com.sionic.app.domain.user.User
import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(
    name = "activity_logs",
    indexes = [
        Index(name = "activity_logs_created_at_idx", columnList = "created_at"),
        Index(name = "activity_logs_event_type_created_at_idx", columnList = "event_type, created_at")
    ]
)
class ActivityLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val eventType: ActivityEventType,

    @Column(nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)
