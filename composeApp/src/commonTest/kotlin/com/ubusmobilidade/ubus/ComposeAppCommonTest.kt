package com.ubusmobilidade.ubus

import com.ubusmobilidade.ubus.data.model.Bus
import com.ubusmobilidade.ubus.data.model.LoginResponse
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.Trip
import com.ubusmobilidade.ubus.data.model.TripDirection
import com.ubusmobilidade.ubus.data.model.TripStatus
import com.ubusmobilidade.ubus.data.model.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ComposeAppCommonTest {

    private val fakeUser = User(
        id = "user-1",
        name = "Teste",
        email = "teste@ubus.me",
        cpf = "12345678901",
        phone = "11999999999",
        role = RoleUsuario.STUDENT,
        municipalityId = "muni-1",
        priorityLevel = 1,
        active = true
    )

    @Test
    fun testLoginResponseEffectiveTokenFallback() {
        val withAccessToken = LoginResponse(accessToken = "token_principal", token = null, user = fakeUser)
        assertEquals("token_principal", withAccessToken.effectiveToken)

        val withLegacyToken = LoginResponse(accessToken = null, token = "token_legado", user = fakeUser)
        assertEquals("token_legado", withLegacyToken.effectiveToken)

        val withBoth = LoginResponse(accessToken = "token_novo", token = "token_velho", user = fakeUser)
        assertEquals("token_novo", withBoth.effectiveToken)

        val empty = LoginResponse(accessToken = null, token = null, user = fakeUser)
        assertEquals("", empty.effectiveToken)
    }

    @Test
    fun testTripResilientNullBus() {
        val tripWithoutBus = Trip(
            tripId = "trip-1",
            tripDate = "2026-10-02",
            shift = "MORNING",
            direction = TripDirection.OUTBOUND,
            status = TripStatus.SCHEDULED,
            routeId = "route-1",
            busId = null,
            realCapacity = 0,
            votingOpen = null,
            votingClose = null
        )
        assertNull(tripWithoutBus.busId)
        assertEquals(0, tripWithoutBus.realCapacity)
    }

    @Test
    fun testBusDefaultCapacity() {
        val bus = Bus(
            id = "bus-1",
            identificationNumber = "001"
        )
        assertEquals(40, bus.standardCapacity)
    }
}