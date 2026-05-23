package com.example.drugmanage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drugmanage.ui.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MedicineViewModel,
    onBack: () -> Unit
) {
    val history by viewModel.allHistory.collectAsStateWithLifecycle()
    val medicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("服薬履歴") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history.sortedByDescending { it.takenTime }) { record ->
                val med = medicines.find { it.id == record.medicineId }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("日時: ${sdf.format(Date(record.takenTime))}", style = MaterialTheme.typography.bodyMedium)
                        Text("薬: ${med?.name ?: "削除された薬"}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
