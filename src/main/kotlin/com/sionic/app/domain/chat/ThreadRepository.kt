package com.sionic.app.domain.chat

import com.sionic.app.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ThreadRepository : JpaRepository<Thread, Long> {
    fun findFirstByUserOrderByLastChatAtDesc(user: User): Optional<Thread>
    fun findAllByUser(user: User, pageable: Pageable): Page<Thread>
}
