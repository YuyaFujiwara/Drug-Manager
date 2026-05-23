package com.example.drugmanage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.drugmanage.data.Medicine
import com.example.drugmanage.notification.AlarmScheduler
import com.example.drugmanage.ui.MedicineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    viewModel: MedicineViewModel,
    medicineId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val medicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var intervalDays by remember { mutableStateOf("1") }
    var hour by remember { mutableStateOf("12") }
    var minute by remember { mutableStateOf("0") }
    var quantity by remember { mutableStateOf("30") }
    var advanceMinutes by remember { mutableStateOf(0) }
    var daysSinceLastTaken by remember { mutableStateOf("0") }
    var isInitialized by remember { mutableStateOf(false) }

    val advanceOptions = listOf(0 to "時間通り", 10 to "10分前", 30 to "30分前", 60 to "1時間前")

    LaunchedEffect(medicineId, medicines) {
        if (medicineId != null && !isInitialized) {
            val med = medicines.find { it.id == medicineId }
            if (med != null) {
                name = med.name
                intervalDays = med.intervalDays.toString()
                hour = med.timeToTakeHour.toString()
                minute = med.timeToTakeMinute.toString()
                quantity = med.totalQuantity.toString()
                advanceMinutes = med.advanceMinutes
                if (med.lastTakenDate > 0) {
                    val diff = System.currentTimeMillis() - med.lastTakenDate
                    daysSinceLastTaken = (diff / (24 * 60 * 60 * 1000L)).toInt().toString()
                }
                isInitialized = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (medicineId == null) "薬の登録" else "薬の編集") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("薬の名前") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = intervalDays,
                onValueChange = { intervalDays = it },
                label = { Text("何日ごとに飲むか (毎日=1, 1日空け=2)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text("時 (0-23)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minute,
                    onValueChange = { minute = it },
                    label = { Text("分 (0-59)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("総量 (残弾)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = daysSinceLastTaken,
                onValueChange = { daysSinceLastTaken = it },
                label = { Text("最後に飲んだのは何日前？ (今日=0, 昨日=1)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("通知タイミング", style = MaterialTheme.typography.bodyLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    advanceOptions.forEach { (minutes, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = advanceMinutes == minutes,
                                onClick = { advanceMinutes = minutes }
                            )
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val n = name.trim()
                    val iDays = intervalDays.toIntOrNull() ?: 1
                    val h = hour.toIntOrNull() ?: 12
                    val m = minute.toIntOrNull() ?: 0
                    val q = quantity.toIntOrNull() ?: 30

                    if (n.isNotEmpty()) {
                        val daysAgo = daysSinceLastTaken.toIntOrNull() ?: 0
                        val now = System.currentTimeMillis()
                        val baseDate = now - daysAgo * 24 * 60 * 60 * 1000L

                        val medicine = Medicine(
                            id = medicineId ?: 0,
                            name = n,
                            intervalDays = iDays,
                            timeToTakeHour = h,
                            timeToTakeMinute = m,
                            totalQuantity = q,
                            remainingQuantity = q, // For add, it's q. For edit, we'll override it below
                            startDate = baseDate,
                            advanceMinutes = advanceMinutes,
                            lastTakenDate = if (daysAgo > 0) baseDate else 0L
                        )

                        if (medicineId == null) {
                            viewModel.addMedicine(medicine) { id ->
                                val scheduler = AlarmScheduler(context)
                                scheduler.scheduleNextAlarm(medicine.copy(id = id))
                                Toast.makeText(context, "登録しました", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        } else {
                            val oldMed = medicines.find { it.id == medicineId }
                            val updatedMed = medicine.copy(
                                remainingQuantity = oldMed?.remainingQuantity ?: q
                            )
                            viewModel.updateMedicine(updatedMed) {
                                Toast.makeText(context, "更新しました", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (medicineId == null) "登録する" else "更新する")
            }
        }
    }
}
