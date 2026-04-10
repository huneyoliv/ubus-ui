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

@Serializable
data class PasswordRedefinitionPayload(
    val token: String,
    val password: String,
)

/* ── Reservation ── */

@Serializable
data class CreateReservationPayload(
    val tripId: String,
    val seatNumber: Int? = null,
    val isRideShare: Boolean? = null,
)

@Serializable
data class UpdateReservationPayload(
    val seatNumber: Int? = null,
    val status: ReservationStatus? = null,
)

/* ── Fleet ── */

@Serializable
data class CreateRoutePayload(
    val municipalityId: String? = null,
    val name: String,
    val description: String? = null,
    val weekDays: List<Int>,
    val votingOpenTime: String,
    val votingCloseTime: String,
)

@Serializable
data class UpdateRoutePayload(
    val name: String? = null,
    val description: String? = null,
    val weekDays: List<Int>? = null,
    val votingOpenTime: String? = null,
    val votingCloseTime: String? = null,
    val active: Boolean? = null,
)

@Serializable
data class CreateBusPayload(
    val municipalityId: String? = null,
    val identificationNumber: String,
    val plate: String,
    val standardCapacity: Int,
    val hasBathroom: Boolean? = null,
    val hasAirConditioning: Boolean? = null,
)

@Serializable
data class UpdateBusPayload(
    val identificationNumber: String? = null,
    val plate: String? = null,
    val standardCapacity: Int? = null,
    val hasBathroom: Boolean? = null,
    val hasAirConditioning: Boolean? = null,
    val active: Boolean? = null,
)

/* ── Trips ── */

@Serializable
data class UpdateTripPayload(
    val tripDate: String? = null,
    val shift: String? = null,
    val direction: TripDirection? = null,
    val routeId: String? = null,
    val busId: String? = null,
    val driverId: String? = null,
    val realCapacity: Int? = null,
    val votingOpen: String? = null,
    val votingClose: String? = null,
    val leaderIds: List<String>? = null,
    val status: TripStatus? = null,
)

@Serializable
data class RelocationPayload(
    val destinationTripId: String,
)

/* ── Management ── */

@Serializable
data class CreateMunicipalityPayload(
    val name: String,
)

@Serializable
data class UpdateMunicipalityPayload(
    val name: String? = null,
    val active: Boolean? = null,
)

@Serializable
data class CreateManagerPayload(
    val municipalityId: String,
    val cpf: String,
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
)

/* ── Users ── */

@Serializable
data class UpdateUserStatusPayload(
    val status: RegistrationStatus,
)

@Serializable
data class UpdatePointPayload(
    val pointId: String,
)
