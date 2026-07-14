package com.sionic.app.domain.report

import com.sionic.app.domain.report.dto.ActivityResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class ReportController(
    private val reportService: ReportService
) {

    @GetMapping("/activity")
    fun getActivity(): ResponseEntity<ActivityResponse> =
        ResponseEntity.ok(reportService.getActivity())

    @GetMapping("/report/csv")
    fun downloadReport(response: HttpServletResponse) =
        reportService.writeReportCsv(response)
}
