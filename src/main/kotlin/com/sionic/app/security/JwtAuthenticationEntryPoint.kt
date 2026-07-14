package com.sionic.app.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.sionic.app.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class JwtAuthenticationEntryPoint(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        objectMapper.writeValue(
            response.writer,
            ErrorResponse(status = 401, code = "UNAUTHORIZED", message = "인증이 필요합니다.", timestamp = ZonedDateTime.now())
        )
    }
}
