package com.ubusmobilidade.ubus.ui.util

import com.ubusmobilidade.ubus.data.model.Reservation

expect class NotificationScheduler() {
    fun scheduleEmbarkAlert(reservation: Reservation, minutesBefore: Int)
    fun cancelAlerts(reservationId: String)
}
