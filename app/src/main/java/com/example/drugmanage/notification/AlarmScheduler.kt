package com.example.drugmanage.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.drugmanage.data.Medicine
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNextAlarm(medicine: Medicine) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MEDICINE_NAME", medicine.name)
            putExtra("MEDICINE_ID", medicine.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextTime = medicine.getNextIntakeTime()
        val alarmTime = nextTime.clone() as Calendar
        alarmTime.add(Calendar.MINUTE, -medicine.advanceMinutes)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Android 14+ may throw SecurityException if SCHEDULE_EXACT_ALARM is not granted
            e.printStackTrace()
        }
    }

    fun cancelAlarm(medicineId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
