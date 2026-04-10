package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.model.*

/* ═══════════════════════════════════════════════
   Trip Repository — /trips
   ═══════════════════════════════════════════════ */

class TripRepository(private val api: ApiClient) {

    /** GET /trips/open */
    suspend fun getOpenTrips(): List<Trip> = api.get("/trips/open")

    /** GET /trips/{tripId} */
    suspend fun getTrip(tripId: String): Trip = api.get("/trips/$tripId")

    /** PATCH /trips/{tripId} */
    suspend fun updateTrip(tripId: String, payload: UpdateTripPayload): Trip =
        api.patch("/trips/$tripId", payload)

    /** POST /trips/{tripId}/confirmation-alert */
    suspend fun sendConfirmationAlert(tripId: String) {
        api.post<String>("/trips/$tripId/confirmation-alert", null)
    }

    /** POST /trips/{tripId}/finish-and-punish */
    suspend fun finishAndPunish(tripId: String) {
        api.post<String>("/trips/$tripId/finish-and-punish", null)
    }

    /** POST /trips/{tripId}/relocation */
    suspend fun relocateStudents(tripId: String, destinationTripId: String) {
        api.post<String>("/trips/$tripId/relocation", RelocationPayload(destinationTripId))
    }

    /** GET /trips/{tripId}/location */
    suspend fun getLocation(tripId: String): TripLocation =
        api.get("/trips/$tripId/location")

    /** PATCH /trips/{tripId}/location */
    suspend fun updateLocation(tripId: String, lat: Double, lng: Double): TripLocation =
        api.patch("/trips/$tripId/location", TripLocation(lat, lng))

    /** GET /trips/{tripId}/alert-status */
    suspend fun getAlertStatus(tripId: String): Map<String, Boolean> =
        api.get("/trips/$tripId/alert-status")
}

/* ═══════════════════════════════════════════════
   Reservation Repository — /reservations
   ═══════════════════════════════════════════════ */

class ReservationRepository(private val api: ApiClient) {

    /** POST /reservations */
    suspend fun create(payload: CreateReservationPayload): Reservation =
        api.post("/reservations", payload)

    /** GET /reservations/mine */
    suspend fun getMyReservations(): List<Reservation> =
        api.get("/reservations/mine")

    /** GET /reservations/trip/{tripId} */
    suspend fun listByTrip(tripId: String): List<Reservation> =
        api.get("/reservations/trip/$tripId")

    /** GET /reservations/trip/{tripId}/occupied-seats */
    suspend fun getOccupiedSeats(tripId: String): List<OccupiedSeat> =
        api.get("/reservations/trip/$tripId/occupied-seats")

    /** GET /reservations/{id} */
    suspend fun getById(id: String): Reservation =
        api.get("/reservations/$id")

    /** PATCH /reservations/{id} */
    suspend fun update(id: String, payload: UpdateReservationPayload): Reservation =
        api.patch("/reservations/$id", payload)

    /** DELETE /reservations/{id} */
    suspend fun cancel(id: String): Unit =
        api.delete("/reservations/$id")
}

/* ═══════════════════════════════════════════════
   User Repository — /users
   ═══════════════════════════════════════════════ */

class UserRepository(private val api: ApiClient) {

    /** GET /users/pending */
    suspend fun listPending(): List<User> = api.get("/users/pending")

    /** PATCH /users/{id}/status */
    suspend fun updateStatus(id: String, status: RegistrationStatus): User =
        api.patch("/users/$id/status", UpdateUserStatusPayload(status))

    /** PATCH /users/me/point */
    suspend fun updateMyPoint(pointId: String): User =
        api.patch("/users/me/point", UpdatePointPayload(pointId))

    // Not in Swagger but expected to exist in backend
    suspend fun getMe(): User = api.get("/users/me")

    suspend fun updateMe(data: Map<String, String>): User =
        api.patch("/users/me", data)

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        api.patch<String>(
            "/users/me/password",
            mapOf("currentPassword" to currentPassword, "newPassword" to newPassword)
        )
    }
}

/* ═══════════════════════════════════════════════
   Fleet Repository — /fleet
   ═══════════════════════════════════════════════ */

class FleetRepository(private val api: ApiClient) {

    /* ── Routes ── */

    /** GET /fleet/routes */
    suspend fun listRoutes(): List<Route> = api.get("/fleet/routes")

    /** POST /fleet/routes */
    suspend fun createRoute(payload: CreateRoutePayload): Route =
        api.post("/fleet/routes", payload)

    /** PATCH /fleet/routes/{id} */
    suspend fun updateRoute(id: String, payload: UpdateRoutePayload): Route =
        api.patch("/fleet/routes/$id", payload)

    /** GET /fleet/routes/{id}/points */
    suspend fun listPickupPoints(routeId: String): List<PickupPoint> =
        api.get("/fleet/routes/$routeId/points")

    /* ── Buses ── */

    /** GET /fleet/buses */
    suspend fun listBuses(): List<Bus> = api.get("/fleet/buses")

    /** POST /fleet/buses */
    suspend fun createBus(payload: CreateBusPayload): Bus =
        api.post("/fleet/buses", payload)

    /** GET /fleet/buses/mine */
    suspend fun listMyBuses(): List<Bus> = api.get("/fleet/buses/mine")

    /** PATCH /fleet/buses/{id} */
    suspend fun updateBus(id: String, payload: UpdateBusPayload): Bus =
        api.patch("/fleet/buses/$id", payload)
}

/* ═══════════════════════════════════════════════
   Management Repository — /management
   ═══════════════════════════════════════════════ */

class ManagementRepository(private val api: ApiClient) {

    /** GET /management/public — list active municipalities (no auth) */
    suspend fun listPublicMunicipalities(): List<Municipality> =
        api.get("/management/public")

    /** GET /management */
    suspend fun listMunicipalities(): List<Municipality> =
        api.get("/management")

    /** POST /management */
    suspend fun createMunicipality(payload: CreateMunicipalityPayload): Municipality =
        api.post("/management", payload)

    /** GET /management/{id} */
    suspend fun getMunicipality(id: String): Municipality =
        api.get("/management/$id")

    /** PATCH /management/{id} */
    suspend fun updateMunicipality(id: String, payload: UpdateMunicipalityPayload): Municipality =
        api.patch("/management/$id", payload)

    /** POST /management/managers */
    suspend fun createManager(payload: CreateManagerPayload): User =
        api.post("/management/managers", payload)

    /** DELETE /management/{id}/manager */
    suspend fun removeManager(municipalityId: String): Unit =
        api.delete("/management/$municipalityId/manager")
}

/* ═══════════════════════════════════════════════
   Metrics Repository — /metrics
   ═══════════════════════════════════════════════ */

class MetricsRepository(private val api: ApiClient) {

    /** GET /metrics/dashboard */
    suspend fun getDashboard(): DashboardMetrics = api.get("/metrics/dashboard")
}
