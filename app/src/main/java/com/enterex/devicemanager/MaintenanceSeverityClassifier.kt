package com.enterex.devicemanager

/**
 * Deterministic first-pass severity rules.
 *
 * This class only classifies evidence supplied by a trusted diagnostic source.
 * It does not activate maintenance and must not be fed arbitrary AI conclusions
 * as if they were verified device facts.
 */
enum class IncidentSeverity(val rank: Int) {
    INFO(0),
    MODERATE(1),
    HIGH(2),
    CRITICAL(3)
}

enum class DiagnosticSignal {
    ROUTINE_EVENT,
    DEGRADED_NON_ESSENTIAL_FEATURE,
    REPEATED_SERVICE_FAILURE,
    VERIFIED_DATA_INTEGRITY_RISK,
    VERIFIED_ACTIVE_SECURITY_THREAT,
    ESSENTIAL_SERVICE_UNAVAILABLE
}

data class DiagnosticEvidence(
    val signal: DiagnosticSignal,
    val verified: Boolean,
    val occurrencesInWindow: Int = 1
)

data class SeverityAssessment(
    val severity: IncidentSeverity,
    val reasonCode: String,
    val requiresAdminDecision: Boolean,
    val automaticMaintenanceEligible: Boolean
)

object MaintenanceSeverityClassifier {
    /**
     * Conservative deterministic policy:
     * - unverified claims cannot trigger HIGH/CRITICAL;
     * - critical requires verified evidence;
     * - repeated failure requires at least 3 occurrences in the caller's
     *   configured observation window;
     * - this is eligibility only, not a command to enter maintenance.
     */
    fun classify(evidence: DiagnosticEvidence): SeverityAssessment {
        val count = evidence.occurrencesInWindow.coerceAtLeast(1)

        if (!evidence.verified) {
            return SeverityAssessment(
                severity = IncidentSeverity.MODERATE,
                reasonCode = "UNVERIFIED_EVIDENCE",
                requiresAdminDecision = true,
                automaticMaintenanceEligible = false
            )
        }

        return when (evidence.signal) {
            DiagnosticSignal.ROUTINE_EVENT -> SeverityAssessment(
                IncidentSeverity.INFO,
                "ROUTINE_EVENT",
                requiresAdminDecision = false,
                automaticMaintenanceEligible = false
            )

            DiagnosticSignal.DEGRADED_NON_ESSENTIAL_FEATURE -> SeverityAssessment(
                IncidentSeverity.MODERATE,
                "NON_ESSENTIAL_FEATURE_DEGRADED",
                requiresAdminDecision = true,
                automaticMaintenanceEligible = false
            )

            DiagnosticSignal.REPEATED_SERVICE_FAILURE -> {
                if (count >= 3) {
                    SeverityAssessment(
                        IncidentSeverity.HIGH,
                        "REPEATED_FAILURE_THRESHOLD_REACHED",
                        requiresAdminDecision = true,
                        automaticMaintenanceEligible = false
                    )
                } else {
                    SeverityAssessment(
                        IncidentSeverity.MODERATE,
                        "FAILURE_NOT_YET_REPEATED",
                        requiresAdminDecision = true,
                        automaticMaintenanceEligible = false
                    )
                }
            }

            DiagnosticSignal.VERIFIED_DATA_INTEGRITY_RISK ->
                critical("VERIFIED_DATA_INTEGRITY_RISK")

            DiagnosticSignal.VERIFIED_ACTIVE_SECURITY_THREAT ->
                critical("VERIFIED_ACTIVE_SECURITY_THREAT")

            DiagnosticSignal.ESSENTIAL_SERVICE_UNAVAILABLE ->
                critical("ESSENTIAL_SERVICE_UNAVAILABLE")
        }
    }

    private fun critical(reason: String) = SeverityAssessment(
        severity = IncidentSeverity.CRITICAL,
        reasonCode = reason,
        requiresAdminDecision = false,
        automaticMaintenanceEligible = true
    )
}
