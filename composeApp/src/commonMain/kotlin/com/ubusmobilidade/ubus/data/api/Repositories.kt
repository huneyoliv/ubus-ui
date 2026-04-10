package com.ubusmobilidade.ubus.data.api

import com.ubusmobilidade.ubus.data.model.BackendReservationItem
import com.ubusmobilidade.ubus.data.model.CreateReservationPayload
import com.ubusmobilidade.ubus.data.model.Reservation
import com.ubusmobilidade.ubus.data.model.Trip
import com.ubusmobilidade.ubus.data.model.User
import com.ubusmobilidade.ubus.data.model.toReservation

class TripRepository(private val api: ApiClient) {
    suspend fun getTrips(): List<Trip> = api.get("/trips")
    suspend fun getTrip(id: String): Trip = api.get("/trips/$id")
}

class ReservationRepository(private val api: ApiClient) {
    suspend fun create(payload: CreateReservationPayload): Reservation =
        api.post("/reservations", payload)

    suspend fun getMyReservations(): List<Reservation> {
        val items: List<BackendReservationItem> = api.get("/reservations/minhas")
        return items.map { it.toReservation() }
    }

    suspend fun cancel(id: String): Reservation =
        api.delete("/reservations/$id")
}

class UserRepository(private val api: ApiClient) {
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
