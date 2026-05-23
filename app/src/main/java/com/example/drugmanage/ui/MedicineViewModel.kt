package com.example.drugmanage.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.drugmanage.data.AppDatabase
import com.example.drugmanage.data.IntakeHistory
import com.example.drugmanage.data.Medicine
import com.example.drugmanage.notification.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val medicineDao = db.medicineDao()
    private val historyDao = db.intakeHistoryDao()

    val allMedicines: StateFlow<List<Medicine>> = medicineDao.getAllMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHistory: StateFlow<List<IntakeHistory>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMedicine(medicine: Medicine, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = medicineDao.insertMedicine(medicine)
            onComplete(id)
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            medicineDao.deleteMedicine(medicine)
            val scheduler = AlarmScheduler(getApplication())
            scheduler.cancelAlarm(medicine.id)
        }
    }

    fun updateMedicine(medicine: Medicine, onComplete: () -> Unit) {
        viewModelScope.launch {
            medicineDao.updateMedicine(medicine)
            val scheduler = AlarmScheduler(getApplication())
            scheduler.scheduleNextAlarm(medicine)
            onComplete()
        }
    }

    fun takeMedicine(medicine: Medicine) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            historyDao.insertHistory(IntakeHistory(medicineId = medicine.id, takenTime = now))
            
            val updatedMedicine = medicine.copy(
                remainingQuantity = if (medicine.remainingQuantity > 0) medicine.remainingQuantity - 1 else 0,
                lastTakenDate = now
            )
            medicineDao.updateMedicine(updatedMedicine)

            // アラームを再スケジュール（フライング服用の場合、直近の予定がスキップされる）
            val scheduler = AlarmScheduler(getApplication())
            scheduler.scheduleNextAlarm(updatedMedicine)
        }
    }

    fun undoTakeMedicine(medicine: Medicine) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis
            
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = cal.timeInMillis

            historyDao.deleteHistory(medicine.id, startOfDay, endOfDay)

            if (medicine.remainingQuantity < medicine.totalQuantity) {
                medicineDao.updateMedicine(medicine.copy(remainingQuantity = medicine.remainingQuantity + 1))
            }
        }
    }
}
