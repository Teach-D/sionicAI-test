package com.sionic.app.exception

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ErrorResponse {
        val message = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ErrorResponse(status = 400, code = "VALIDATION_ERROR", message = message)
    }

    @ExceptionHandler(DuplicateEmailException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateEmail(e: DuplicateEmailException): ErrorResponse =
        ErrorResponse(status = 409, code = "DUPLICATE_EMAIL", message = e.message ?: "이미 사용 중인 이메일입니다.")

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleBadCredentials(e: BadCredentialsException): ErrorResponse =
        ErrorResponse(status = 401, code = "UNAUTHORIZED", message = e.message ?: "이메일 또는 비밀번호가 올바르지 않습니다.")

    @ExceptionHandler(ThreadNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleThreadNotFound(e: ThreadNotFoundException): ErrorResponse =
        ErrorResponse(status = 404, code = "THREAD_NOT_FOUND", message = e.message ?: "스레드를 찾을 수 없습니다.")

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbidden(e: ForbiddenException): ErrorResponse =
        ErrorResponse(status = 403, code = "FORBIDDEN", message = e.message ?: "접근 권한이 없습니다.")

    @ExceptionHandler(OpenAiException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun handleOpenAi(e: OpenAiException): ErrorResponse =
        ErrorResponse(status = 502, code = "OPENAI_ERROR", message = e.message ?: "OpenAI API 호출에 실패했습니다.")
}
