package com.myluggagepartner.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ReminderScheduler {

    private const val REMINDER_HOUR = 9

    fun schedule(context: Context, tripId: Long, tripName: String, departureDateEpoch: Long) {
        val departureDate = LocalDate.ofEpochDay(departureDateEpoch)
        val reminderDate = departureDate.minusDays(1)
        val now = LocalDate.now()
        if (reminderDate.isBefore(now) || reminderDate.isEqual(now)) return

        val triggerMillis = reminderDate
            .atTime(LocalTime.of(REMINDER_HOUR, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TRIP_ID, tripId)
            putExtra(ReminderReceiver.EXTRA_TRIP_NAME, tripName)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            tripId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
    }

    fun cancel(context: Context, tripId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            tripId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pending)
    }
}
