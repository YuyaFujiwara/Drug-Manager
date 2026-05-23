package com.example.drugmanage.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val intervalDays: Int, // N日置き (1なら毎日)
    val timeToTakeHour: Int,
    val timeToTakeMinute: Int,
    val totalQuantity: Int,
    val remainingQuantity: Int,
    val startDate: Long, // 開始日(エポックミリ秒)
    val advanceMinutes: Int = 0, // 何分前に通知するか (デフォルト0)
    val lastTakenDate: Long = 0L // 最後に飲んだ日時
) {
    /**
     * 次回の服薬予定日時を計算します。
     * 現在の時刻（およびadvanceMinutesを考慮した通知タイミング）より未来になる直近の時間を返します。
     */
    fun getNextIntakeTime(): Calendar {
        val now = Calendar.getInstance()
        val nextTime = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, timeToTakeHour)
            set(Calendar.MINUTE, timeToTakeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 直近の服用日時から少なくとも何時間後以降の予定を「次回」とするか（ここでは半日=12時間と設定）
        val marginMillis = 12 * 60 * 60 * 1000L

        while (true) {
            val alarmTime = nextTime.clone() as Calendar
            alarmTime.add(Calendar.MINUTE, -advanceMinutes)
            
            val isAfterNow = alarmTime.after(now) && alarmTime.timeInMillis > now.timeInMillis
            val isAfterLastTaken = nextTime.timeInMillis > lastTakenDate + marginMillis

            if (isAfterNow && isAfterLastTaken) {
                break
            }
            nextTime.add(Calendar.DAY_OF_YEAR, intervalDays)
        }
        return nextTime
    }
}
