package com.sionic.app.domain.report.dto

import java.time.ZonedDateTime

data class ActivityResponse(
    val from: ZonedDateTime,
    val to: ZonedDateTime,
    val signUpCount: Long,
    val loginCount: Long,
    val chatCreatedCount: Long
)
