package no.nav.syfo.legeerklaring.kafka.model

data class ReceivedLegeerklaring(
    val msgId: String,
    val legeerklaering: Legeerklaering
)

data class Legeerklaering(
    val id: String
)

data class ValidationResult(
    val status: Status,
    val ruleHits: List<RuleInfo>
)

data class RuleInfo(
    val ruleName: String,
    val messageForSender: String,
    val messageForUser: String,
    val ruleStatus: Status
)

enum class Status {
    OK,
    INVALID
}
