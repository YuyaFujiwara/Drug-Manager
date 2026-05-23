package com.example.drugmanage

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.drugmanage.ui.MedicineViewModel
import com.example.drugmanage.ui.screens.HomeScreen
import com.example.drugmanage.ui.screens.AddMedicineScreen
import com.example.drugmanage.ui.screens.CalendarScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val medicineViewModel: MedicineViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = medicineViewModel,
                onNavigateToAdd = { navController.navigate("addMedicine") },
                onNavigateToEdit = { id -> navController.navigate("editMedicine/$id") },
                onNavigateToCalendar = { navController.navigate("calendarHistory") }
            )
        }
        composable("addMedicine") {
            AddMedicineScreen(
                viewModel = medicineViewModel,
                medicineId = null,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "editMedicine/{medicineId}",
            arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("medicineId")
            AddMedicineScreen(
                viewModel = medicineViewModel,
                medicineId = id,
                onBack = { navController.popBackStack() }
            )
        }
        composable("calendarHistory") {
            CalendarScreen(
                viewModel = medicineViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
