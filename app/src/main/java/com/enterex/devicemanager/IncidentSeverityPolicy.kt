package com.enterex.devicemanager

/**
 * Classifica códigos conhecidos de incidentes para orientar a resposta segura.
 * Esta política não monitora o aparelho nem executa restauração ou apagamento.
 */
enum class PolicyIncidentSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class MaintenanceDecision {
    NO_ACTION,
    ADMIN_REVIEW_REQUIRED,
    AUTOMATIC_SAFE_MAINTENANCE
}

object PolicyIncidentSeverityPolicy {
    fun classify(code: String): PolicyIncidentSeverity? =
        when (code.trim().uppercase(java.util.Locale.ROOT)) {
            "APP_CRASH_LOOP",
            "DATA_INTEGRITY_FAILURE",
            "UNSAFE_RESET_STATE" -> PolicyIncidentSeverity.CRITICAL

            "REPEATED_SERVICE_FAILURE",
            "STORAGE_WRITE_FAILURE",
            "DEVICE_COMMUNICATION_UNSTABLE" -> PolicyIncidentSeverity.HIGH

            "DEVICE_NOT_DETECTED",
            "NETWORK_UNAVAILABLE",
            "UPDATE_PENDING" -> PolicyIncidentSeverity.MEDIUM

            "MINOR_UI_WARNING",
            "OPTIONAL_FEATURE_UNAVAILABLE" -> PolicyIncidentSeverity.LOW

            else -> null
        }

    fun decisionFor(severity: PolicyIncidentSeverity): MaintenanceDecision =
        when (severity) {
            PolicyIncidentSeverity.CRITICAL,
            PolicyIncidentSeverity.HIGH -> MaintenanceDecision.AUTOMATIC_SAFE_MAINTENANCE

            PolicyIncidentSeverity.MEDIUM,
            PolicyIncidentSeverity.LOW -> MaintenanceDecision.ADMIN_REVIEW_REQUIRED
        }

    /**
     * Códigos desconhecidos nunca acionam manutenção automática:
     * ficam aguardando análise do administrador.
     */
    fun decisionForCode(code: String): MaintenanceDecision =
        decisionFor(classify(code) ?: PolicyIncidentSeverity.MEDIUM)
}
