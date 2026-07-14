package com.sionic.app.exception

class FeedbackNotFoundException(feedbackId: Long) : RuntimeException("피드백을 찾을 수 없습니다: $feedbackId")
