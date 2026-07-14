package com.sionic.app.exception

class ThreadNotFoundException(threadId: Long) : RuntimeException("스레드를 찾을 수 없습니다: $threadId")
