package com.sionic.app.domain.chat

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findAllByThreadOrderByCreatedAtAsc(thread: Thread): List<Chat>
    fun findAllByThreadInOrderByCreatedAtAsc(threads: Collection<Thread>): List<Chat>
    fun deleteAllByThread(thread: Thread)

    @Query("SELECT c FROM Chat c JOIN FETCH c.thread t JOIN FETCH t.user u WHERE c.createdAt BETWEEN :from AND :to ORDER BY c.createdAt ASC")
    fun findAllWithUserByCreatedAtBetween(from: ZonedDateTime, to: ZonedDateTime): List<Chat>
}
