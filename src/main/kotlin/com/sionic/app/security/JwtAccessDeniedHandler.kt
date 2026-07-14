package com.sionic.app.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.sionic.app.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class JwtAccessDeniedHandler(private val objectMapper: ObjectMapper) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_FORBIDDEN
        objectMapper.writeValue(
            response.writer,
            ErrorResponse(status = 403, code = "FORBIDDEN", message = "접근 권한이 없습니다.", timestamp = ZonedDateTime.now())
        )
    }
}
