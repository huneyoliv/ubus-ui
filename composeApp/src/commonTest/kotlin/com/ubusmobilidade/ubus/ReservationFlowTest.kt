package com.ubusmobilidade.ubus

import com.ubusmobilidade.ubus.data.model.Trip
import com.ubusmobilidade.ubus.data.model.TripDirection
import com.ubusmobilidade.ubus.data.model.TripStatus
import com.ubusmobilidade.ubus.data.model.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReservationFlowTest {

    @Test
    fun testTripGrouping() {
        val route1 = Route("route-1", "Rota A")
        val route2 = Route("route-2", "Rota B")

        val trip1 = Trip(
            tripId = "trip-1",
            tripDate = "2026-06-10",
            shift = "MORNING",
            direction = TripDirection.OUTBOUND,
            status = TripStatus.OPEN_FOR_RESERVATION,
            routeId = "route-1",
            busId = "bus-1",
            realCapacity = 40,
            votingOpen = "",
            votingClose = "",
            route = route1
        )

        val trip2 = Trip(
            tripId = "trip-2",
            tripDate = "2026-06-10",
            shift = "MORNING",
            direction = TripDirection.INBOUND,
            status = TripStatus.OPEN_FOR_RESERVATION,
            routeId = "route-1",
            busId = "bus-1",
            realCapacity = 40,
            votingOpen = "",
            votingClose = "",
            route = route1
        )

        val trip3 = Trip(
            tripId = "trip-3",
            tripDate = "2026-06-10",
            shift = "AFTERNOON",
            direction = TripDirection.OUTBOUND,
            status = TripStatus.OPEN_FOR_RESERVATION,
            routeId = "route-2",
            busId = "bus-2",
            realCapacity = 40,
            votingOpen = "",
            votingClose = "",
            route = route2
        )

        val trips = listOf(trip1, trip2, trip3)

        val grouped = trips.groupBy { Triple(it.routeId, it.tripDate, it.shift) }

        assertEquals(2, grouped.size)

        val morningGroup = grouped[Triple("route-1", "2026-06-10", "MORNING")]
        assertNotNull(morningGroup)
        assertEquals(2, morningGroup.size)

        val outbound = morningGroup.find { it.direction == TripDirection.OUTBOUND }
        val inbound = morningGroup.find { it.direction == TripDirection.INBOUND }

        assertEquals("trip-1", outbound?.tripId)
        assertEquals("trip-2", inbound?.tripId)

        val afternoonGroup = grouped[Triple("route-2", "2026-06-10", "AFTERNOON")]
        assertNotNull(afternoonGroup)
        assertEquals(1, afternoonGroup.size)

        val outbound2 = afternoonGroup.find { it.direction == TripDirection.OUTBOUND }
        val inbound2 = afternoonGroup.find { it.direction == TripDirection.INBOUND }

        assertEquals("trip-3", outbound2?.tripId)
        assertNull(inbound2)
    }
}
