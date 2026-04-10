package com.ubusmobilidade.ubus.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoleUsuario {
    SUPER_ADMIN, MANAGER, DRIVER, LEADER, STUDENT, RIDE_SHARE
}

@Serializable
enum class RegistrationStatus {
    PENDING, APPROVED, REJECTED
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
