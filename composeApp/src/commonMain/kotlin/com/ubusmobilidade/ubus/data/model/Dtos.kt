package com.ubusmobilidade.ubus.data.model

import kotlinx.serialization.Serializable

/* ── Auth ── */

@Serializable
data class LoginPayload(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val user: User,
)

@Serializable
data class RegisterPayload(
    val municipalityId: String,
    val cpf: String,
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val role: RoleUsuario? = null,
    val priorityLevel: Int? = null,
    val defaultRouteId: String? = null,
    val needsWheelchair: Boolean? = null,
    val photoUrl: String? = null,
    val gradeFileUrl: String? = null,
    val residenciaFileUrl: String? = null,
)

/* ── Reservation ── */

@Serializable
data class CreateReservationPayload(
    val tripId: String,
    val seatNumber: Int? = null,
    val rideShare: Boolean? = null,
)

/* ── Backend reservation mapping ── */

@Serializable
data class BackendReservationItem(
    val reserva: BackendReserva,
    val viagem: Trip,
)

@Serializable
data class BackendReserva(
    val id: String,
    val idViagem: String,
    val idUsuario: String,
    val numeroAssento: Int? = null,
    val isCarona: Boolean,
    val status: StatusReserva,
    val criadoEm: String,
)

fun BackendReservationItem.toReservation(): Reservation = Reservation(
    id = reserva.id,
    idViagem = reserva.idViagem,
    idUsuario = reserva.idUsuario,
    numeroAssento = reserva.numeroAssento,
    isCarona = reserva.isCarona,
    status = reserva.status,
    createdAt = reserva.criadoEm,
    viagem = viagem,
)
