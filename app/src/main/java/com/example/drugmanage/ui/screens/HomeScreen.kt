package com.example.drugmanage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drugmanage.data.Medicine
import com.example.drugmanage.ui.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MedicineViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    val medicines by viewModel.allMedicines.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("薬の管理") },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.DateRange, contentDescription = "カレンダー")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "薬を追加")
            }
        }
    ) { padding ->
        if (medicines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("登録されている薬はありません")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medicines) { medicine ->
                    MedicineCard(
                        medicine = medicine, 
                        onTake = { viewModel.takeMedicine(it) },
                        onEdit = { onNavigateToEdit(it.id) },
                        onDelete = { viewModel.deleteMedicine(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicineCard(
    medicine: Medicine, 
    onTake: (Medicine) -> Unit,
    onEdit: (Medicine) -> Unit,
    onDelete: (Medicine) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("削除の確認") },
            text = { Text("「${medicine.name}」を削除してもよろしいですか？") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete(medicine)
                }) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = medicine.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onEdit(medicine) }) {
                        Icon(Icons.Default.Edit, contentDescription = "編集")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "削除", tint = MaterialTheme.colorScheme.error)
                    }
                }
                Text(text = "飲む時間: %02d:%02d".format(medicine.timeToTakeHour, medicine.timeToTakeMinute))
                Text(text = "残量: ${medicine.remainingQuantity} / ${medicine.totalQuantity}")
                Text(text = "間隔: ${if (medicine.intervalDays == 1) "毎日" else "${medicine.intervalDays}日ごと"}")
                val formatter = SimpleDateFormat("M月d日 HH:mm", Locale.getDefault())
                Text(
                    text = "次回予定: ${formatter.format(medicine.getNextIntakeTime().time)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = { onTake(medicine) }) {
                Text("飲んだ")
            }
        }
    }
}
