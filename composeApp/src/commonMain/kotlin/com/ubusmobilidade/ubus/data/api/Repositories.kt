package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.model.*

class TripRepository(private val api: ApiClient) {
    suspend fun getOpenTrips(): List<Trip> {
        println("DEBUG: TripRepository - getOpenTrips")
        return api.get("/trips/open")
    }

    suspend fun getTrip(tripId: String): Trip = api.get("/trips/$tripId")

    suspend fun updateTrip(tripId: String, payload: UpdateTripPayload): Trip =
        api.patch("/trips/$tripId", payload)

    suspend fun sendConfirmationAlert(tripId: String) {
        println("DEBUG: TripRepository - sendConfirmationAlert for trip: $tripId")
        api.post<String>("/trips/$tripId/confirmation-alert", null)
    }

    suspend fun finishAndPunish(tripId: String) {
        api.post<String>("/trips/$tripId/finish-and-punish", null)
    }

    suspend fun relocateStudents(tripId: String, destinationTripId: String) {
        api.post<String>("/trips/$tripId/relocation", RelocationPayload(destinationTripId))
    }

    suspend fun getLocation(tripId: String): TripLocation =
        api.get("/trips/$tripId/location")

    suspend fun updateLocation(tripId: String, lat: Double, lng: Double): TripLocation {
        println("DEBUG: TripRepository - updateLocation: $lat, $lng")
        return api.patch("/trips/$tripId/location", TripLocation(lat, lng))
    }

    suspend fun getAlertStatus(tripId: String): Map<String, Boolean> =
        api.get("/trips/$tripId/alert-status")
}

class ReservationRepository(private val api: ApiClient) {
    suspend fun create(payload: CreateReservationPayload): Reservation {
        println("DEBUG: ReservationRepository - create reservation for trip: ${payload.tripId}")
        return api.post("/reservations", payload)
    }

    suspend fun createWithPickupPoint(
        tripId: String,
        pickupPointId: String,
        seatNumber: Int? = null,
        isRideShare: Boolean? = null,
    ): Reservation {
        println("DEBUG: ReservationRepository - createWithPickupPoint trip=$tripId point=$pickupPointId")
        return create(
            CreateReservationPayload(
                tripId = tripId,
                pickupPointId = pickupPointId,
                seatNumber = seatNumber,
                isRideShare = isRideShare,
            )
        )
    }

    suspend fun getMyReservations(): List<Reservation> {
        println("DEBUG: ReservationRepository - getMyReservations")
        return api.get("/reservations/mine")
    }

    suspend fun listByTrip(tripId: String): List<Reservation> =
        api.get("/reservations/trip/$tripId")

    suspend fun getOccupiedSeats(tripId: String): List<OccupiedSeat> =
        api.get("/reservations/trip/$tripId/occupied-seats")

    suspend fun getById(id: String): Reservation =
        api.get("/reservations/$id")

    suspend fun update(id: String, payload: UpdateReservationPayload): Reservation =
        api.patch("/reservations/$id", payload)

    suspend fun cancel(id: String): Unit =
        api.delete("/reservations/$id")
}

class UserRepository(private val api: ApiClient) {
    suspend fun listPending(): List<User> = api.get("/users/pending")

    suspend fun listUsers(municipalityId: String? = null, role: RoleUsuario? = null): List<User> {
        val params = mutableMapOf<String, String>()
        municipalityId?.let { params["municipalityId"] = it }
        role?.let { params["role"] = it.name }
        
        return try {
            api.get("/users", params)
        } catch (e: Exception) {
            println("DEBUG: UserRepository - /users failed, trying /users/pending as fallback: ${e.message}")
            // Fallback to listPending if /users is not available
            val pending = listPending()
            if (role != null) pending.filter { it.role == role } else pending
        }
    }

    @Deprecated("Use listPending and filter locally since GET /users/{id} is not in Swagger")
    suspend fun getUser(id: String): User = api.get("/users/$id")

    suspend fun updateUser(id: String, payload: RegisterPayload): User = 
        api.patch("/users/$id", payload)

    suspend fun deleteUser(id: String) {
        println("DEBUG: UserRepository - deleteUser (soft delete via status REJECTED)")
        updateStatus(id, RegistrationStatus.REJECTED)
    }

    suspend fun updateStatus(id: String, status: RegistrationStatus): User =
        api.patch("/users/$id/status", UpdateUserStatusPayload(status))

    suspend fun updateMyPoint(pointId: String): User =
        api.patch("/users/me/point", UpdatePointPayload(pointId))

    suspend fun getMe(): User {
        println("DEBUG: UserRepository - getMe (using storage with fallback)")
        return api.authStorage.user ?: api.get("/users/me")
    }

    suspend fun updateMe(data: UpdateProfilePayload): User {
        println("DEBUG: UserRepository - updateMe")
        val updated = api.patch<User>("/users/me", data)
        api.authStorage.user = updated
        return updated
    }

    @Deprecated("Use updateMe(UpdateProfilePayload) instead")
    suspend fun updateMe(data: Map<String, String>): User =
        api.patch("/users/me", data)

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        println("DEBUG: UserRepository - changePassword")
        api.patch<String>(
            "/users/me/password",
            ChangePasswordPayload(currentPassword, newPassword)
        )
    }

    suspend fun requestSemesterRenewal(payload: SemesterRenewalPayload): SemesterRenewalResponse {
        println("DEBUG: UserRepository - requestSemesterRenewal")
        return api.post("/users/me/semester-renewal", payload)
    }
}

class NotificationRepository(private val api: ApiClient) {
    suspend fun send(payload: SendNotificationPayload): NotificationResponse {
        println("DEBUG: NotificationRepository - send notification: ${payload.title}")
        return api.post("/notifications/send", payload)
    }
}

class FleetRepository(private val api: ApiClient) {
    suspend fun listRoutes(): List<Route> = api.get("/fleet/routes")

    suspend fun getRoute(id: String): Route {
        println("DEBUG: FleetRepository - getRoute (local filter)")
        return listRoutes().find { it.id == id } 
            ?: throw ApiError(404, "Route Not Found", "Rota $id não encontrada na listagem local.")
    }

    suspend fun listBusesByRoute(routeId: String): List<Bus> {
        println("DEBUG: FleetRepository - listBusesByRoute (local filter)")
        return listBuses().filter { it.routeId == routeId }
    }

    suspend fun assignBusToRoute(routeId: String, busId: String) {
        println("DEBUG: FleetRepository - assignBusToRoute using PATCH /buses/$busId")
        updateBus(busId, UpdateBusPayload(routeId = routeId))
    }

    suspend fun removeBusFromRoute(routeId: String, busId: String) {
        println("DEBUG: FleetRepository - removeBusFromRoute using PATCH /buses/$busId")
        updateBus(busId, UpdateBusPayload(routeId = null))
    }

    suspend fun createRoute(payload: CreateRoutePayload): Route =
        api.post("/fleet/routes", payload)

    suspend fun updateRoute(id: String, payload: UpdateRoutePayload): Route =
        api.patch("/fleet/routes/$id", payload)

    suspend fun listPickupPoints(routeId: String): List<PickupPoint> =
        api.get("/fleet/routes/$routeId/points")

    suspend fun listBuses(): List<Bus> = api.get("/fleet/buses")

    suspend fun getBus(id: String): Bus {
        println("DEBUG: FleetRepository - getBus (local filter)")
        return listBuses().find { it.id == id }
            ?: throw ApiError(404, "Bus Not Found", "Ônibus $id não encontrado na listagem local.")
    }

    suspend fun getBusLayout(id: String, tripId: String? = null): List<OccupiedSeat> {
        println("DEBUG: FleetRepository - getBusLayout (frontend logic placeholder)")
        // No futuro, isso será calculado dinamicamente cruzando dados de ocupação
        return api.get("/reservations/trip/$tripId/occupied-seats")
    }

    suspend fun createBus(payload: CreateBusPayload): Bus {
        println("DEBUG: FleetRepository - createBus: ${payload.plate}")
        return api.post("/fleet/buses", payload)
    }

    suspend fun listMyBuses(): List<Bus> {
        println("DEBUG: FleetRepository - listMyBuses")
        return api.get("/fleet/buses/mine")
    }

    suspend fun updateBus(id: String, payload: UpdateBusPayload): Bus =
        api.patch("/fleet/buses/$id", payload)

    suspend fun getRouteCalendar(routeId: String, month: String): RouteCalendarResponse =
        api.get("/trips/route/$routeId/calendar", params = mapOf("month" to month))

    suspend fun scheduleTrips(payload: ScheduleTripsPayload): String =
        api.post("/trips/schedule", payload)

    suspend fun assignDriverToTrip(tripId: String, driverId: String): Trip =
        api.patch("/trips/$tripId/driver", AssignTripDriverPayload(driverId))
}

class ManagementRepository(private val api: ApiClient) {
    suspend fun listPublicMunicipalities(): List<Municipality> =
        api.get("/management/public")

    suspend fun listMunicipalities(): List<Municipality> =
        api.get("/management")

    suspend fun createMunicipality(payload: CreateMunicipalityPayload): Municipality =
        api.post("/management", payload)

    suspend fun getMunicipality(id: String): Municipality =
        api.get("/management/$id")

    suspend fun updateMunicipality(id: String, payload: UpdateMunicipalityPayload): Municipality =
        api.patch("/management/$id", payload)

    suspend fun createManager(payload: CreateManagerPayload): User =
        api.post("/management/managers", payload)

    suspend fun removeManager(municipalityId: String): Unit =
        api.delete("/management/$municipalityId/manager")

    suspend fun listPublicPickupPoints(municipalityId: String): List<PickupPoint> =
        api.get("/management/public/$municipalityId/pickup-points")
}

class MetricsRepository(private val api: ApiClient) {
    suspend fun getDashboard(): DashboardMetrics = api.get("/metrics/dashboard")
}

class DriverRepository(private val api: ApiClient) {
    suspend fun assignForToday(busId: String, serviceDate: String): DriverAssignmentResponse =
        api.post("/driver/assignment", DriverAssignmentPayload(busId = busId, serviceDate = serviceDate))

    suspend fun getCurrentTripSummary(): DriverCurrentTripSummary =
        api.get("/driver/trips/current")

    suspend fun notifyDeparting(tripId: String): String =
        api.post("/driver/trips/$tripId/departing", DriverDepartingPayload(departingNow = true))
}
