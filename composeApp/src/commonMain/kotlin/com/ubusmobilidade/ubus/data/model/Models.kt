package com.ubusmobilidade.ubus.data.model

import kotlinx.serialization.Serializable

/* ── User ── */

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val cpf: String,
    val role: RoleUsuario,
    val municipalityId: String? = null,
    val phone: String? = null,
    val status: RegistrationStatus? = null,
    val priorityLevel: Int? = null,
    val defaultRouteId: String? = null,
    val defaultPointId: String? = null,
    val needsWheelchair: Boolean? = null,
    val photoUrl: String? = null,
    val gradeFileUrl: String? = null,
    val residenciaFileUrl: String? = null,
    val createdAt: String? = null,
)

/* ── Municipality ── */

@Serializable
data class Municipality(
    val id: String,
    val name: String,
    val active: Boolean = true,
    val managerId: String? = null,
    val createdAt: String? = null,
)

/* ── Route ── */

@Serializable
data class Route(
    val id: String,
    val name: String,
    val description: String? = null,
    val municipalityId: String? = null,
    val weekDays: List<Int>? = null,
    val votingOpenTime: String? = null,
    val votingCloseTime: String? = null,
    val active: Boolean = true,
)

/* ── PickupPoint ── */

@Serializable
data class PickupPoint(
    val id: String,
    val name: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val routeId: String? = null,
)

/* ── Bus ── */

@Serializable
data class Bus(
    val id: String,
    val identificationNumber: String,
    val plate: String? = null,
    val standardCapacity: Int,
    val municipalityId: String? = null,
    val driverId: String? = null,
    val hasBathroom: Boolean = false,
    val hasAirConditioning: Boolean = false,
    val active: Boolean = true,
)

/* ── Trip ── */

@Serializable
data class Trip(
    val tripId: String,
    val tripDate: String,
    val shift: String,
    val direction: TripDirection,
    val status: TripStatus,
    val routeId: String,
    val busId: String,
    val driverId: String? = null,
    val realCapacity: Int,
    val votingOpen: String,
    val votingClose: String,
    val leaderIds: List<String>? = null,
    val route: Route? = null,
    val bus: Bus? = null,
)

/* ── Reservation ── */

@Serializable
data class Reservation(
    val id: String,
    val tripId: String,
    val userId: String,
    val seatNumber: Int? = null,
    val status: ReservationStatus,
    val isRideShare: Boolean = false,
    val createdAt: String? = null,
    val user: ReservationUser? = null,
    val trip: Trip? = null,
)

@Serializable
data class ReservationUser(
    val id: String,
    val name: String,
    val cpf: String,
)

/* ── Location ── */

@Serializable
data class TripLocation(
    val lat: Double,
    val lng: Double,
)

/* ── Dashboard Metrics ── */

@Serializable
data class DashboardMetrics(
    val totalStudents: Int? = null,
    val totalDrivers: Int? = null,
    val totalBuses: Int? = null,
    val totalRoutes: Int? = null,
    val activeTrips: Int? = null,
    val pendingUsers: Int? = null,
    val totalReservationsToday: Int? = null,
)

/* ── Seat ── */

@Serializable
data class OccupiedSeat(
    val seatNumber: Int,
    val userId: String? = null,
    val userName: String? = null,
)
