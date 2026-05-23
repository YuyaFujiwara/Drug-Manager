package com.example.drugmanage.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.drugmanage.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "薬"
        val medicineId = intent.getLongExtra("MEDICINE_ID", -1L)

        if (medicineId != -1L) {
            showNotification(context, medicineName, medicineId)

            // Reschedule for next time based on intervalDays
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val medicine = db.medicineDao().getMedicineById(medicineId)
                if (medicine != null) {
                    val scheduler = AlarmScheduler(context)
                    scheduler.scheduleNextAlarm(medicine)
                }
            }
        }
    }

    private fun showNotification(context: Context, name: String, id: Long) {
        val channelId = "drug_manage_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "薬の通知", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("薬の時間です！")
            .setContentText("${name}を飲む時間です。残量も確認してください。")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(id.toInt(), builder.build())
    }
}
