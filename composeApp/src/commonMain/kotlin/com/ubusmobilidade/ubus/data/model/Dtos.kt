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
    val defaultPointId: String? = null,
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
    val pickupPointId: String? = null,
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
    val votingOpenTime: String? = null,
    val votingCloseTime: String? = null,
    val departureTimeOutbound: String? = null,
    val departureTimeInbound: String? = null,
)

@Serializable
data class UpdateRoutePayload(
    val name: String? = null,
    val description: String? = null,
    val weekDays: List<Int>? = null,
    val votingOpenTime: String? = null,
    val votingCloseTime: String? = null,
    val departureTimeOutbound: String? = null,
    val departureTimeInbound: String? = null,
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
    val hasElevator: Boolean? = null,
)

@Serializable
data class UpdateBusPayload(
    val identificationNumber: String? = null,
    val plate: String? = null,
    val standardCapacity: Int? = null,
    val hasBathroom: Boolean? = null,
    val hasAirConditioning: Boolean? = null,
    val routeId: String? = null,
    val active: Boolean? = null,
)

@Serializable
data class CreatePickupPointPayload(
    val name: String,
    val lat: Double,
    val lng: Double,
)

@Serializable
data class UpdatePickupPointPayload(
    val name: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
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

@Serializable
data class UpdateProfilePayload(
    val phone: String? = null,
    val email: String? = null,
    val needsWheelchair: Boolean? = null,
    val photoUrl: String? = null,
)

@Serializable
data class ChangePasswordPayload(
    val currentPassword: String,
    val newPassword: String,
)

/* ── Email Verification ── */

@Serializable
data class SendEmailCodePayload(
    val email: String,
    val context: String, // "REGISTER" or "CHANGE_EMAIL"
)

@Serializable
data class VerifyEmailCodePayload(
    val email: String,
    val code: String,
)

@Serializable
data class VerifyEmailCodeResponse(
    val verified: Boolean,
    val message: String? = null,
)

/* ── Notifications ── */

@Serializable
data class SendNotificationPayload(
    val title: String,
    val message: String,
    val target: String, // "MUNICIPALITY" or "ROUTE"
    val targetId: String,
)

@Serializable
data class NotificationResponse(
    val id: String? = null,
    val title: String? = null,
    val message: String? = null,
    val sentAt: String? = null,
    val recipientCount: Int? = null,
)

/* ── Driver operations ── */

@Serializable
data class DriverAssignmentPayload(
    val busId: String,
    val serviceDate: String,
)

@Serializable
data class DriverDepartingPayload(
    val departingNow: Boolean = true,
)

/* ── Trip scheduling ── */

@Serializable
data class ScheduleTripsPayload(
    val routeId: String,
    val dates: List<String>,
    val direction: TripDirection,
    val shift: TripShift,
    val busId: String? = null,
    val driverId: String? = null,
)

@Serializable
data class AssignTripDriverPayload(
    val driverId: String,
)

@Serializable
data class CreateTripPayload(
    val tripId: String,
    val tripDate: String,
    val shift: String,
    val direction: TripDirection,
    val routeId: String,
    val busId: String,
    val driverId: String? = null,
    val realCapacity: Int,
    val votingOpen: String,
    val votingClose: String,
    val leaderIds: List<String> = emptyList(),
    val status: TripStatus = TripStatus.OPEN_FOR_RESERVATION,
)

/* ── Semester Renewal ── */

@Serializable
data class SemesterRenewalPayload(
    val gradeFileUrl: String? = null,
    val residenciaFileUrl: String? = null,
)

@Serializable
data class SemesterRenewalResponse(
    val message: String? = null,
    val status: String? = null,
)

enum class UploadType {
    PROFILE_PHOTO,
    GRADE_DOCUMENT,
    RESIDENCIA_DOCUMENT,
    ACCESSIBILITY_PROOF,
}

@Serializable
data class UploadResponse(
    val fileUrl: String,
    val type: String,
    val path: String? = null,
    val expiresAt: String? = null,
)

@Serializable
data class AccessibilityRequestPayload(
    val reason: AccessibilityReason,
    val needsWheelchair: Boolean = false,
    val accessibilityDocUrl: String,
)

@Serializable
data class BusLayoutPayload(
    val hasElevator: Boolean,
    val preferentialSeats: List<Int>,
)

/* ── Ratings and Gamification Dtos ── */

@Serializable
data class CreateTripRatingPayload(
    val reservationId: String,
    val cleanlinessRating: Int,
    val punctualityRating: Int,
    val driverRating: Int,
    val comment: String? = null,
)

