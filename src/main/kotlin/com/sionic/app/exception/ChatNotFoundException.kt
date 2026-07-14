package com.sionic.app.exception

class ChatNotFoundException(chatId: Long) : RuntimeException("대화를 찾을 수 없습니다: $chatId")
