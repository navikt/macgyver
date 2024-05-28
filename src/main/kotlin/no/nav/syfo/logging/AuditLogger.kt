package no.nav.syfo.logging

import java.time.ZonedDateTime.now

private val application = "macgyver"

internal class AuditLogger(
    val email: String,
) {
    fun createcCefMessage(
        fnr: String?,
        operation: Operation,
        requestPath: String,
        permit: Permit,
    ): String {
        val now = now().toInstant().toEpochMilli()
        val subject = fnr?.padStart(11, '0')
        val duidStr = subject?.let { " duid=$it" } ?: ""

        return "CEF:0|$application|auditLog|1.0|${operation.logString}|Sporingslogg|INFO|end=$now$duidStr" +
            " suid=$email request=$requestPath flexString1Label=Decision flexString1=$permit"
    }

    enum class Operation(val logString: String) {
        READ("audit:access"),
        WRITE("audit:update"),
        UNKNOWN("audit:unknown"),
    }

    enum class Permit(val logString: String) {
        PERMIT("Permit"),
        DENY("Deny"),
    }
}
