package com.enterex.devicemanager

/**
 * Classifica códigos conhecidos de incidentes para orientar a resposta segura.
 * Esta política não monitora o aparelho nem executa restauração ou apagamento.
 */
enum class IncidentSeverity {
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

object IncidentSeverityPolicy {
    fun classify(code: String): IncidentSeverity? =
        when (code.trim().uppercase(java.util.Locale.ROOT)) {
            "APP_CRASH_LOOP",
            "DATA_INTEGRITY_FAILURE",
            "UNSAFE_RESET_STATE" -> IncidentSeverity.CRITICAL

            "REPEATED_SERVICE_FAILURE",
            "STORAGE_WRITE_FAILURE",
            "DEVICE_COMMUNICATION_UNSTABLE" -> IncidentSeverity.HIGH

            "DEVICE_NOT_DETECTED",
            "NETWORK_UNAVAILABLE",
            "UPDATE_PENDING" -> IncidentSeverity.MEDIUM

            "MINOR_UI_WARNING",
            "OPTIONAL_FEATURE_UNAVAILABLE" -> IncidentSeverity.LOW

            else -> null
        }

    fun decisionFor(severity: IncidentSeverity): MaintenanceDecision =
        when (severity) {
            IncidentSeverity.CRITICAL,
            IncidentSeverity.HIGH -> MaintenanceDecision.AUTOMATIC_SAFE_MAINTENANCE

            IncidentSeverity.MEDIUM,
            IncidentSeverity.LOW -> MaintenanceDecision.ADMIN_REVIEW_REQUIRED
        }

    /**
     * Códigos desconhecidos nunca acionam manutenção automática:
     * ficam aguardando análise do administrador.
     */
    fun decisionForCode(code: String): MaintenanceDecision =
        decisionFor(classify(code) ?: IncidentSeverity.MEDIUM)
}
