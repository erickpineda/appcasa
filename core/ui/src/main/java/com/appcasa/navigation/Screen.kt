package com.appcasa.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {

  // ─── Destinos del menú inferior (Los 4 Pilares) ─────
  data object Dashboard  : Screen("dashboard")
  data object Management : Screen("management") // Fusión Tareas + Listas
  data object FamilyHub  : Screen("family_hub")  // Fusión Familia + Agenda
  data object Utilities  : Screen("utilities")

  // ─── Destinos secundarios ───────────────────────────
  data object Settings   : Screen("settings")
  data object Tasks      : Screen("tasks")
  data object Calendar   : Screen("calendar")
  data object Family     : Screen("family")
  data object Lists      : Screen("lists")

  // ─── Destinos de detalle (sin barra inferior) ────────
  data object TaskDetail : Screen("tasks/{taskId}") {
    fun createRoute(taskId: Long) = "tasks/$taskId"
  }
  data object PetDetail  : Screen("pets/{petId}") {
    fun createRoute(petId: Long) = "pets/$petId"
  }
  data object MemberDetail : Screen("members/{memberId}") {
    fun createRoute(memberId: Long) = "members/$memberId"
  }
  data object AddMember : Screen("add_member")
  data object AddTask   : Screen("add_task")
  data object EditMember : Screen("edit_member/{memberId}") {
    fun createRoute(memberId: Long) = "edit_member/$memberId"
  }
  data object ListDetail : Screen("lists/{listId}") {
    fun createRoute(listId: Long) = "lists/$listId"
  }
  data object DosageCalculator : Screen("dosage_calculator")
  data object BMICalculator    : Screen("bmi_calculator")
  data object MortgageCalculator : Screen("mortgage_calculator")
  data object AgeCalculator    : Screen("age_calculator")
  data object ConsumptionCalculator : Screen("consumption_calculator")
  data object SavingsCalculator : Screen("savings_calculator")
  data object Inventory      : Screen("inventory")
  data object VehicleManager : Screen("vehicle_management")
  data object Expenses       : Screen("expenses")
  data object PhotoToPdf     : Screen("photo_to_pdf")
  data object WifiQR         : Screen("wifi_qr")
  data object CocinaConverter : Screen("cocina_converter")
  data object FeedingCalculator : Screen("feeding_calculator")
  data object SmartSafe      : Screen("smart_safe")
}

data class BottomNavItem(
  val screen: Screen,
  val label: String,
  val icon: ImageVector
)

val bottomNavItems = listOf(
  BottomNavItem(Screen.Dashboard,  "Inicio",       Icons.Default.Home),
  BottomNavItem(Screen.Management, "Gestión",      Icons.Default.CheckCircle),
  BottomNavItem(Screen.FamilyHub,  "Familia",      Icons.Default.Groups),
  BottomNavItem(Screen.Utilities,  "Utilidades",   Icons.Default.Apps),
)
