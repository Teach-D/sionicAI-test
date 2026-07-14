package com.sionic.app.domain.feedback

import com.sionic.app.domain.feedback.dto.CreateFeedbackRequest
import com.sionic.app.domain.feedback.dto.UpdateFeedbackStatusRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(private val feedbackService: FeedbackService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createFeedback(
        @Valid @RequestBody request: CreateFeedbackRequest,
        authentication: Authentication
    ) = feedbackService.createFeedback(
        request = request,
        userEmail = authentication.name,
        isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" }
    )

    @GetMapping
    fun getFeedbacks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "DESC") direction: String,
        @RequestParam isPositive: Boolean?,
        authentication: Authentication
    ) = feedbackService.getFeedbacks(
        userEmail = authentication.name,
        isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" },
        page = page,
        size = size,
        direction = direction,
        isPositive = isPositive
    )

    @PatchMapping("/{feedbackId}/status")
    fun updateFeedbackStatus(
        @PathVariable feedbackId: Long,
        @Valid @RequestBody request: UpdateFeedbackStatusRequest
    ) = feedbackService.updateFeedbackStatus(feedbackId, request)
}
