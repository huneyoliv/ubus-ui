package com.ubusmobilidade.ubus.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
    val preferredRouteId: String? = null,
    val pendingPreferredRouteId: String? = null,
    val defaultPointId: String? = null,
    val needsWheelchair: Boolean? = null,
    val accessibilityReason: AccessibilityReason? = null,
    val accessibilityDocUrl: String? = null,
    val accessibilityStatus: AccessibilityStatus? = null,
    val accessibilityApprovedAt: String? = null,
    val accessibilityReviewNote: String? = null,
    val accessibilityConsecutivePeriods: Int = 0,
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
    val departureTimeOutbound: String? = null,
    val departureTimeInbound: String? = null,
    val active: Boolean = true,
    val requiresElevator: Boolean = false,
)

/* ── PickupPoint ── */

@Serializable
data class PickupPoint(
    val id: String,
    val name: String,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val routeId: String? = null,
)

@Serializable
data class DropoffPoint(
    val id: String,
    val name: String,
    val address: String? = null,
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
    val hasElevator: Boolean = false,
    val preferentialSeats: List<Int>? = null,
    val routeId: String? = null,
    val active: Boolean = true,
)

/* ── Trip ── */

@Serializable
data class Trip(
    @SerialName("id") val tripId: String,
    val tripDate: String,
    val shift: String,
    val direction: TripDirection,
    val status: TripStatus,
    val routeId: String,
    val busId: String,
    val driverId: String? = null,
    @SerialName("actualCapacity") val realCapacity: Int,
    @SerialName("votingOpenAt") val votingOpen: String,
    @SerialName("votingCloseAt") val votingClose: String,
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
    val phone: String,
    val photoUrl: String? = null,
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
    val seatNumber: Int? = null,
    val userId: String? = null,
    val userName: String? = null,
)

@Serializable
data class DriverAssignmentResponse(
    val assignmentId: String? = null,
    val tripOutId: String? = null,
    val tripBackId: String? = null,
)

@Serializable
data class DriverRouteInfo(
    val id: String,
    val name: String,
    val description: String? = null,
    val departureTimeOutbound: String? = null,
    val departureTimeInbound: String? = null,
)

@Serializable
data class DriverBusInfo(
    val id: String,
    val identificationNumber: String,
    val plate: String? = null,
    val standardCapacity: Int,
    val hasElevator: Boolean = false,
    val hasBathroom: Boolean = false,
)

@Serializable
data class DriverTripPoint(
    val pointId: String,
    val pointName: String,
    val studentsCount: Int,
    val lat: Double? = null,
    val lng: Double? = null,
    val type: String? = null, // "BOARDING" | "ALIGHTING"
)

@Serializable
data class DriverCurrentTripSummary(
    val phase: String? = null,
    val tripId: String? = null,
    val tripDate: String? = null,
    val shift: String? = null,
    val direction: TripDirection? = null,
    val noTripToday: Boolean = false,
    val route: DriverRouteInfo? = null,
    val bus: DriverBusInfo? = null,
    val points: List<DriverTripPoint> = emptyList(),
)

@Serializable
data class RouteCalendarResponse(
    val scheduledDates: List<String> = emptyList(),
)

/* ── Ratings and Gamification ── */

@Serializable
data class TripRating(
    val id: String,
    val reservationId: String,
    val tripId: String,
    val userId: String,
    val cleanlinessRating: Int,
    val punctualityRating: Int,
    val driverRating: Int,
    val comment: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class AttendanceScore(
    val userId: String,
    val score: Double,
    val badges: List<AttendanceBadge> = emptyList(),
)

/* ── Bus Layout Models ── */

@Serializable
data class BusCell(
    val col: Int,
    val type: CellType,
    val virtualNumber: Int? = null,
    val physicalNumber: Int? = null,
    val position: SeatPosition? = null,
    val isDpm: Boolean = false,
)

@Serializable
data class BusLayoutRow(val cells: List<BusCell>)

@Serializable
data class BusLayout(
    val busId: String,
    val numberingMode: SeatNumberingMode,
    val numerationSide: NumerationSide,
    val rows: List<BusLayoutRow>,
    val dpmSeatVirtualNumber: Int? = null,
    val updatedAt: String? = null,
)

