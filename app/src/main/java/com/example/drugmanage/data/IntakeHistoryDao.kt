package com.example.drugmanage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeHistoryDao {
    @Query("SELECT * FROM intake_history")
    fun getAllHistory(): Flow<List<IntakeHistory>>

    @Query("SELECT * FROM intake_history WHERE medicineId = :medicineId")
    fun getHistoryByMedicine(medicineId: Long): Flow<List<IntakeHistory>>

    @Insert
    suspend fun insertHistory(history: IntakeHistory): Long
    
    @Query("DELETE FROM intake_history WHERE medicineId = :medicineId AND takenTime >= :startOfDay AND takenTime < :endOfDay")
    suspend fun deleteHistory(medicineId: Long, startOfDay: Long, endOfDay: Long)
}
