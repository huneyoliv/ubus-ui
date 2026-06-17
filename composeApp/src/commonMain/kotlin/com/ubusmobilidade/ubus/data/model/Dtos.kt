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
    val preferredRouteId: String? = null,
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
    val votingOpenDaysBefore: Int? = null,
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
    val votingOpenDaysBefore: Int? = null,
    val active: Boolean? = null,
    val departureTimeOutbound: String? = null,
    val departureTimeInbound: String? = null,
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
    val preferentialSeats: List<Int>? = null,
)

@Serializable
data class UpdateBusPayload(
    val identificationNumber: String? = null,
    val plate: String? = null,
    val standardCapacity: Int? = null,
    val hasBathroom: Boolean? = null,
    val hasAirConditioning: Boolean? = null,
    val active: Boolean? = null,
    val preferentialSeats: List<Int>? = null,
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

enum class VerificationChannel { EMAIL, WHATSAPP }
enum class VerificationContext { CHANGE_EMAIL, RESET_PASSWORD, REGISTER }

@Serializable
data class SendVerificationCodePayload(
    val identifier: String,
    val channel: VerificationChannel,
    val context: VerificationContext,
)

@Serializable
data class VerifyCodePayload(
    val identifier: String,
    val code: String,
    val channel: VerificationChannel,
    val context: VerificationContext,
)

@Serializable
data class VerifyCodeResponse(
    val verified: Boolean,
    val token: String? = null,
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

@Serializable
data class UpdatePreferredRoutePayload(val routeId: String?)

@Serializable
data class SwapDriverBusPayload(val busId: String)

/* ── Semester Renewal ── */

@Serializable
data class SemesterRenewalPayload(
    val gradeFileUrl: String? = null,
    val residenciaFileUrl: String? = null,
    val preferredRouteId: String? = null,
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
    val proofDocUrl: String,
    val needsWheelchair: Boolean? = null,
)

@Serializable
data class CreateTripRatingPayload(
    val reservationId: String,
    val tripId: String,
    val cleanlinessRating: Int,
    val punctualityRating: Int,
    val driverRating: Int,
    val comment: String? = null,
)

/* ── Bus Layout DTOs ── */

data class BusWizardAnswers(
    val plate: String,
    val identificationNumber: String,
    val p1: SeatNumberingMode,
    val p2: FrontRowLayout,
    val p3: RearLayout,
    val p4capacity: Int,
    val p5: AccessibilityFeature,
    val p6: NumerationSide,
    val p6b: NumberingPattern = NumberingPattern.SEQUENTIAL,
    val p7physicalNumbers: Map<Int, Int> = emptyMap(),
)

@Serializable
data class SaveBusLayoutPayload(
    val numberingMode: SeatNumberingMode,
    val numerationSide: NumerationSide,
    val rows: List<BusLayoutRow>,
    val dpmSeatVirtualNumber: Int?,
    val preferentialSeats: List<Int>,
)

