package com.sionic.app.exception

import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
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

    @ExceptionHandler(com.sionic.app.exception.ChatNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleChatNotFound(e: com.sionic.app.exception.ChatNotFoundException): ErrorResponse =
        ErrorResponse(status = 404, code = "CHAT_NOT_FOUND", message = e.message ?: "대화를 찾을 수 없습니다.")

    @ExceptionHandler(com.sionic.app.exception.FeedbackNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleFeedbackNotFound(e: com.sionic.app.exception.FeedbackNotFoundException): ErrorResponse =
        ErrorResponse(status = 404, code = "FEEDBACK_NOT_FOUND", message = e.message ?: "피드백을 찾을 수 없습니다.")

    @ExceptionHandler(com.sionic.app.exception.FeedbackAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleFeedbackAlreadyExists(e: com.sionic.app.exception.FeedbackAlreadyExistsException): ErrorResponse =
        ErrorResponse(status = 409, code = "FEEDBACK_ALREADY_EXISTS", message = e.message ?: "해당 대화에 이미 피드백이 존재합니다.")

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(e: IllegalArgumentException): ErrorResponse =
        ErrorResponse(status = 400, code = "VALIDATION_ERROR", message = e.message ?: "잘못된 요청입니다.")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException): ErrorResponse =
        ErrorResponse(status = 400, code = "VALIDATION_ERROR", message = "요청 바디를 읽을 수 없습니다.")
}
