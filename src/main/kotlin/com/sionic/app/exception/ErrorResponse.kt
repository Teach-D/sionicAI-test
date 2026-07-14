package com.sionic.app.exception

import java.time.ZonedDateTime

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
)
