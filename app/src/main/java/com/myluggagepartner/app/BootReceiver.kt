package com.myluggagepartner.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myluggagepartner.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(context).dao()
                val trips = dao.allTripsOnce()
                for (trip in trips) {
                    val epoch = trip.departureDateEpoch ?: continue
                    ReminderScheduler.schedule(context, trip.id, trip.name, epoch)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
