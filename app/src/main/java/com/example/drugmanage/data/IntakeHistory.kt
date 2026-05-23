package com.example.drugmanage.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "intake_history",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId")]
)
data class IntakeHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val takenTime: Long // 飲んだ時間(エポックミリ秒)
)
