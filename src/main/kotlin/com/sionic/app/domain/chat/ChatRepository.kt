package com.sionic.app.domain.chat

import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findAllByThreadOrderByCreatedAtAsc(thread: Thread): List<Chat>
    fun findAllByThreadInOrderByCreatedAtAsc(threads: Collection<Thread>): List<Chat>
    fun deleteAllByThread(thread: Thread)
}
