package com.ubusmobilidade.ubus.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoleUsuario {
    SUPER_ADMIN, MANAGER, DRIVER, LEADER, STUDENT, RIDE_SHARE
}

@Serializable
enum class StatusCadastro {
    PENDING, APPROVED, REJECTED
}

@Serializable
enum class DirecaoViagem {
    OUTBOUND, INBOUND
}

@Serializable
enum class StatusViagem {
    SCHEDULED, OPEN_FOR_RESERVATION, ONGOING, FINISHED, CANCELLED
}

@Serializable
enum class StatusReserva {
    CONFIRMED, PRESENT, ABSENT, CANCELLED_BY_SYSTEM, EXCESS
}
