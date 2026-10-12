package com.enterex.devicemanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceSeverityClassifierTest {
    @Test fun routineVerifiedEventIsInformational() {
        val result = MaintenanceSeverityClassifier.classify(
            DiagnosticEvidence(DiagnosticSignal.ROUTINE_EVENT, verified = true)
        )
        assertEquals(IncidentSeverity.INFO, result.severity)
        assertFalse(result.automaticMaintenanceEligible)
    }

    @Test fun unverifiedCriticalClaimCannotTriggerAutomaticMaintenance() {
        val result = MaintenanceSeverityClassifier.classify(
            DiagnosticEvidence(
                DiagnosticSignal.VERIFIED_ACTIVE_SECURITY_THREAT,
                verified = false
            )
        )
        assertEquals(IncidentSeverity.MODERATE, result.severity)
        assertTrue(result.requiresAdminDecision)
        assertFalse(result.automaticMaintenanceEligible)
    }

    @Test fun oneOrTwoFailuresRemainModerate() {
        listOf(1, 2).forEach { count ->
            val result = MaintenanceSeverityClassifier.classify(
                DiagnosticEvidence(
                    DiagnosticSignal.REPEATED_SERVICE_FAILURE,
                    verified = true,
                    occurrencesInWindow = count
                )
            )
            assertEquals(IncidentSeverity.MODERATE, result.severity)
            assertFalse(result.automaticMaintenanceEligible)
        }
    }

    @Test fun threeRepeatedFailuresBecomeHighButNeedAdminDecision() {
        val result = MaintenanceSeverityClassifier.classify(
            DiagnosticEvidence(
                DiagnosticSignal.REPEATED_SERVICE_FAILURE,
                verified = true,
                occurrencesInWindow = 3
            )
        )
        assertEquals(IncidentSeverity.HIGH, result.severity)
        assertTrue(result.requiresAdminDecision)
        assertFalse(result.automaticMaintenanceEligible)
    }

    @Test fun verifiedCriticalRiskIsEligibleButNotAnAction() {
        val result = MaintenanceSeverityClassifier.classify(
            DiagnosticEvidence(
                DiagnosticSignal.VERIFIED_DATA_INTEGRITY_RISK,
                verified = true
            )
        )
        assertEquals(IncidentSeverity.CRITICAL, result.severity)
        assertTrue(result.automaticMaintenanceEligible)
    }
}
