package com.ubusmobilidade.ubus.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoleUsuario {
    SUPER_ADMIN, MANAGER, DRIVER, LEADER, STUDENT, RIDE_SHARE
}

@Serializable
enum class RegistrationStatus {
    PENDING, APPROVED, REJECTED, RENEWAL_PENDING, SUSPENDED, INACTIVE
}

@Serializable
enum class TripDirection {
    OUTBOUND, INBOUND
}

@Serializable
enum class TripStatus {
    SCHEDULED, OPEN_FOR_RESERVATION, ONGOING, FINISHED, CANCELLED
}

@Serializable
enum class ReservationStatus {
    CONFIRMED, PRESENT, ABSENT, CANCELLED_BY_SYSTEM, EXCESS
}

@Serializable
enum class TripShift {
    MORNING, AFTERNOON, NIGHT
}

@Serializable
enum class AttendanceBadge {
    PUNCTUAL, FREQUENT_RIDER, ECO_FRIENDLY, PERFECT_WEEK, PERFECT_MONTH
}

@Serializable
enum class AccessibilityReason {
    PCD,
    TEA,
    IDOSO,
    GESTANTE,
    LACTANTE,
    MOBILIDADE_REDUZIDA,
}

@Serializable
enum class AccessibilityStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    EXPIRED,
    REVOKED,
}

fun AccessibilityReason.isPermanent(): Boolean =
    this != AccessibilityReason.GESTANTE && this != AccessibilityReason.LACTANTE

fun AccessibilityReason.displayName(): String = when (this) {
    AccessibilityReason.PCD -> "Pessoa com Deficiência (PCD)"
    AccessibilityReason.TEA -> "Transtorno do Espectro Autista (TEA)"
    AccessibilityReason.IDOSO -> "Idoso (60+ anos)"
    AccessibilityReason.GESTANTE -> "Gestante"
    AccessibilityReason.LACTANTE -> "Lactante"
    AccessibilityReason.MOBILIDADE_REDUZIDA -> "Mobilidade Reduzida"
}

fun AccessibilityReason.requiredDocuments(): String = when (this) {
    AccessibilityReason.PCD -> "Laudo médico com CID ou relatório de deficiência"
    AccessibilityReason.TEA -> "Laudo médico com CID ou relatório de avaliação"
    AccessibilityReason.IDOSO -> "RG, CNH ou qualquer documento com data de nascimento"
    AccessibilityReason.GESTANTE -> "Cartão de pré-natal ou declaração médica"
    AccessibilityReason.LACTANTE -> "Declaração médica ou certidão de nascimento do bebê"
    AccessibilityReason.MOBILIDADE_REDUZIDA -> "Laudo ou declaração médica"
}

fun AccessibilityReason.needsWheelchairQuestion(): Boolean =
    this == AccessibilityReason.PCD || this == AccessibilityReason.MOBILIDADE_REDUZIDA


