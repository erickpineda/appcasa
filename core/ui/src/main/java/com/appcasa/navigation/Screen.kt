package com.appcasa.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.appcasa.core.ui.R
import kotlinx.serialization.Serializable

sealed interface Screen {

  // ─── Flujo de Bienvenida y Colaboración ────────────
  @Serializable data object Auth : Screen
  @Serializable data class HouseSetup(val code: String? = null) : Screen

  // ─── Grafos de Navegación ───────────────────────────
  @Serializable data object ManagementGraph
  @Serializable data object FamilyGraph
  @Serializable data object UtilitiesGraph

  // ─── Destinos del menú inferior (Los 4 Pilares) ─────
  @Serializable data object Dashboard : Screen
  @Serializable data object Management : Screen
  @Serializable data object FamilyHub : Screen
  @Serializable data object Utilities : Screen

  // ─── Destinos secundarios ───────────────────────────
  @Serializable data object Settings : Screen
  @Serializable data object Tasks : Screen
  @Serializable data object Calendar : Screen
  @Serializable data object Family : Screen
  @Serializable data object Lists : Screen

  // ─── Destinos de detalle (sin barra inferior) ────────
  @Serializable data class TaskDetail(val taskId: String) : Screen
  @Serializable data class PetDetail(val petId: String) : Screen
  @Serializable data class MemberDetail(val memberId: String) : Screen
  @Serializable data object AddMember : Screen
  @Serializable data object AddTask : Screen
  @Serializable data class EditMember(val memberId: String) : Screen
  @Serializable data class ListDetail(val listId: String) : Screen
  
  @Serializable data object DosageCalculator : Screen
  @Serializable data object BMICalculator : Screen
  @Serializable data object MortgageCalculator : Screen
  @Serializable data object AgeCalculator : Screen
  @Serializable data object ConsumptionCalculator : Screen
  @Serializable data object SavingsCalculator : Screen
  @Serializable data object Inventory : Screen
  @Serializable data object VehicleManager : Screen
  @Serializable data object Expenses : Screen
  @Serializable data object PhotoToPdf : Screen
  @Serializable data object WifiQR : Screen
  @Serializable data object CocinaConverter : Screen
  @Serializable data object FeedingCalculator : Screen
  @Serializable data object SmartSafe : Screen
  @Serializable data object HomeMaintenance : Screen
  @Serializable data class MaintenanceDetail(val id: String) : Screen
  @Serializable data object FinanceStats : Screen
  @Serializable data object RewardStore : Screen
  @Serializable data object Archive : Screen

  companion object {
    val managementTabRoutes = listOf(
      Management::class, Tasks::class, Lists::class, ListDetail::class, 
      Inventory::class, AddTask::class, TaskDetail::class, HomeMaintenance::class, 
      MaintenanceDetail::class, RewardStore::class, Archive::class
    )
    val familyTabRoutes = listOf(
      FamilyHub::class, Family::class, Calendar::class, PetDetail::class, 
      MemberDetail::class, EditMember::class, AddMember::class
    )
    val utilitiesTabRoutes = listOf(
      Utilities::class, DosageCalculator::class, BMICalculator::class, 
      MortgageCalculator::class, AgeCalculator::class, ConsumptionCalculator::class, 
      SavingsCalculator::class, Expenses::class, VehicleManager::class, SmartSafe::class, 
      PhotoToPdf::class, WifiQR::class, CocinaConverter::class, FeedingCalculator::class,
      FinanceStats::class
    )
  }
}

data class BottomNavItem(
  val screen: Screen,
  val labelRes: Int,
  val icon: ImageVector
)

val bottomNavItems = listOf(
  BottomNavItem(Screen.Dashboard,  R.string.nav_dashboard,  Icons.Default.Home),
  BottomNavItem(Screen.Management, R.string.nav_management, Icons.Default.CheckCircle),
  BottomNavItem(Screen.FamilyHub,  R.string.nav_family,      Icons.Default.Groups),
  BottomNavItem(Screen.Utilities,  R.string.nav_utilities,   Icons.Default.Apps),
)
