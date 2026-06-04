package com.ubusmobilidade.ubus.ui.util

expect fun getTripDepartureMillis(tripDate: String, shift: String, direction: String = "OUTBOUND"): Long
expect fun isToday(dateString: String): Boolean
