package com.sionic.app.domain.report

import com.sionic.app.domain.chat.ChatRepository
import com.sionic.app.domain.report.dto.ActivityResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class ReportService(
    private val activityLogRepository: ActivityLogRepository,
    private val chatRepository: ChatRepository
) {

    @Transactional(readOnly = true)
    fun getActivity(): ActivityResponse {
        val to = ZonedDateTime.now()
        val from = to.minusHours(24)
        return ActivityResponse(
            from = from,
            to = to,
            signUpCount = activityLogRepository.countByEventTypeAndCreatedAtBetween(ActivityEventType.SIGN_UP, from, to),
            loginCount = activityLogRepository.countByEventTypeAndCreatedAtBetween(ActivityEventType.LOGIN, from, to),
            chatCreatedCount = activityLogRepository.countByEventTypeAndCreatedAtBetween(ActivityEventType.CHAT_CREATED, from, to)
        )
    }

    @Transactional(readOnly = true)
    fun writeReportCsv(response: HttpServletResponse) {
        val to = ZonedDateTime.now()
        val from = to.minusHours(24)
        val filename = "report-${LocalDate.now(ZoneId.systemDefault())}.csv"

        response.contentType = "text/csv; charset=UTF-8"
        response.setHeader("Content-Disposition", "attachment; filename=\"$filename\"")

        val writer = response.writer
        writer.println("chatId,threadId,userId,userEmail,userName,question,answer,createdAt")

        chatRepository.findAllWithUserByCreatedAtBetween(from, to).forEach { chat ->
            val user = chat.thread.user
            writer.println(
                listOf(
                    chat.id.toString(),
                    chat.thread.id.toString(),
                    user.id.toString(),
                    user.email,
                    user.name,
                    chat.question,
                    chat.answer,
                    chat.createdAt.toString()
                ).joinToString(",") { escapeCsv(it) }
            )
        }
        writer.flush()
    }

    private fun escapeCsv(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
