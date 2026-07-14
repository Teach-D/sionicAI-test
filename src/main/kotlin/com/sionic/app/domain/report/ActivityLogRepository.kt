package com.sionic.app.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

interface ActivityLogRepository : JpaRepository<ActivityLog, Long> {
    fun countByEventTypeAndCreatedAtBetween(
        eventType: ActivityEventType,
        from: ZonedDateTime,
        to: ZonedDateTime
    ): Long
}
