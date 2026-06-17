package com.ubusmobilidade.ubus.ui.util

expect fun getTripDepartureMillis(tripDate: String, shift: String, direction: String = "OUTBOUND", customDepartureTime: String? = null): Long
expect fun isToday(dateString: String): Boolean
expect fun isPast(dateString: String): Boolean
