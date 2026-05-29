package com.appcasa.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appcasa.features.calendar.presentation.screen.CalendarScreen
import com.appcasa.features.dashboard.presentation.screen.DashboardScreen
import com.appcasa.features.dashboard.presentation.screen.FamilyHubScreen
import com.appcasa.features.dashboard.presentation.screen.ManagementHubScreen
import com.appcasa.features.family.presentation.screen.AddMemberScreen
import com.appcasa.features.family.presentation.screen.EditMemberScreen
import com.appcasa.features.family.presentation.screen.FamilyScreen
import com.appcasa.features.family.presentation.screen.MemberDetailScreen
import com.appcasa.features.finance.presentation.screen.ExpenseScreen
import com.appcasa.features.inventory.presentation.screen.StockScreen
import com.appcasa.features.lists.presentation.screen.ListDetailScreen
import com.appcasa.features.lists.presentation.screen.ListsScreen
import com.appcasa.features.pets.presentation.screen.PetDetailScreen
import com.appcasa.features.settings.presentation.screen.SettingsScreen
import com.appcasa.features.tasks.presentation.screen.AddTaskScreen
import com.appcasa.features.tasks.presentation.screen.TaskDetailScreen
import com.appcasa.features.tasks.presentation.screen.TasksScreen
import com.appcasa.features.utilities.presentation.screen.AgeCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.BMICalculatorScreen
import com.appcasa.features.utilities.presentation.screen.ConsumptionCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.DosageCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.appcasa.features.utilities.presentation.screen.MortgageCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.SavingsCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.UtilitiesScreen
import com.appcasa.features.utilities.presentation.screen.VehicleManagementScreen

@Composable
fun AppNavigation() {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  Scaffold(
    bottomBar = {
      NavigationBar {
        bottomNavItems.forEach { item ->
          val isManagementTab = item.screen == Screen.Management && currentDestination?.route in listOf(
            Screen.Management.route, Screen.Tasks.route, Screen.Lists.route, 
            Screen.ListDetail.route, Screen.Inventory.route, Screen.AddTask.route, Screen.TaskDetail.route
          )
          val isFamilyTab = item.screen == Screen.FamilyHub && currentDestination?.route in listOf(
            Screen.FamilyHub.route, Screen.Family.route, Screen.Calendar.route, 
            Screen.PetDetail.route, Screen.MemberDetail.route, Screen.EditMember.route, Screen.AddMember.route
          )
          val isUtilitiesTab = item.screen == Screen.Utilities && currentDestination?.route in listOf(
            Screen.Utilities.route, Screen.DosageCalculator.route, Screen.BMICalculator.route, 
            Screen.MortgageCalculator.route, Screen.AgeCalculator.route, Screen.ConsumptionCalculator.route, 
            Screen.SavingsCalculator.route, Screen.Expenses.route, Screen.VehicleManager.route
          )
          val isDashboardTab = item.screen == Screen.Dashboard && currentDestination?.route == Screen.Dashboard.route

          val selected = isDashboardTab || isManagementTab || isFamilyTab || isUtilitiesTab

          NavigationBarItem(
            selected = selected,
            onClick = {
              val isAtHub = currentDestination?.route == item.screen.route
              if (isAtHub) {
                // Ya estamos en la raíz de la pestaña, no hacemos nada
              } else if (selected) {
                // Estamos en una sub-pantalla de esta sección, intentamos volver a su Hub
                val popped = navController.popBackStack(item.screen.route, inclusive = false)
                if (!popped) {
                  // Si no estaba en el stack (ej: navegación directa), forzamos ir al Hub
                  navController.navigate(item.screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                    launchSingleTop = true
                    restoreState = false
                  }
                }
              } else {
                // Vamos a una pestaña diferente: siempre a su pantalla principal (Hub)
                navController.navigate(item.screen.route) {
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = false
                  }
                  launchSingleTop = true
                  restoreState = false // Esto evita que se restaure la sub-pantalla anterior
                }
              }
            },
            icon = { Icon(item.icon, contentDescription = item.label) },
            label = { if (selected) Text(item.label) }
          )
        }
      }
    }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = Screen.Dashboard.route,
      modifier = Modifier.padding(innerPadding),
      enterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)) },
      exitTransition = { fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f, animationSpec = tween(200)) },
      popEnterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 1.05f, animationSpec = tween(300)) },
      popExitTransition = { fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200)) }
    ) {
      composable(Screen.Dashboard.route) {
        DashboardScreen(navController = navController)
      }
      composable(Screen.Management.route) {
        ManagementHubScreen(navController = navController)
      }
      composable(Screen.FamilyHub.route) {
        FamilyHubScreen(navController = navController)
      }
      composable(Screen.Utilities.route) {
        UtilitiesScreen(navController = navController)
      }
      composable(Screen.Settings.route) {
        SettingsScreen(navController = navController, innerPadding = innerPadding)
      }
      
      composable(Screen.Tasks.route) { TasksScreen(navController = navController) }
      composable(Screen.Calendar.route) { CalendarScreen(navController = navController) }
      composable(Screen.Family.route) { FamilyScreen(navController = navController) }
      composable(Screen.Lists.route) { ListsScreen(navController = navController) }
      
      composable(
        route = Screen.PetDetail.route,
        arguments = listOf(androidx.navigation.navArgument("petId") { type = androidx.navigation.NavType.LongType })
      ) { PetDetailScreen(navController = navController) }

      composable(
        route = Screen.TaskDetail.route,
        arguments = listOf(androidx.navigation.navArgument("taskId") { type = androidx.navigation.NavType.LongType })
      ) { TaskDetailScreen(navController = navController) }

      composable(
        route = Screen.MemberDetail.route,
        arguments = listOf(androidx.navigation.navArgument("memberId") { type = androidx.navigation.NavType.LongType })
      ) { backStackEntry ->
        val memberId = backStackEntry.arguments?.getLong("memberId") ?: 0L
        MemberDetailScreen(navController = navController, memberId = memberId)
      }

      composable(Screen.AddMember.route) { AddMemberScreen(navController = navController) }
      
      composable(
        route = Screen.EditMember.route,
        arguments = listOf(androidx.navigation.navArgument("memberId") { type = androidx.navigation.NavType.LongType })
      ) { EditMemberScreen(navController = navController) }

      composable(Screen.AddTask.route) { AddTaskScreen(navController = navController) }

      composable(
        route = Screen.ListDetail.route,
        arguments = listOf(androidx.navigation.navArgument("listId") { type = androidx.navigation.NavType.LongType })
      ) { ListDetailScreen(navController = navController) }

      composable(Screen.DosageCalculator.route) { DosageCalculatorScreen(navController = navController) }
      composable(Screen.BMICalculator.route) { BMICalculatorScreen(navController = navController) }
      composable(Screen.MortgageCalculator.route) { MortgageCalculatorScreen(navController = navController) }
      composable(Screen.AgeCalculator.route) { AgeCalculatorScreen(navController = navController) }
      composable(Screen.ConsumptionCalculator.route) { ConsumptionCalculatorScreen(navController = navController) }
      composable(Screen.SavingsCalculator.route) { SavingsCalculatorScreen(navController = navController) }
      composable(Screen.VehicleManager.route) { VehicleManagementScreen(navController = navController) }
      composable(Screen.Inventory.route) { StockScreen(navController = navController) }
      composable(Screen.Expenses.route) { ExpenseScreen(navController = navController) }
      composable(Screen.PhotoToPdf.route) { PhotoToPdfScreen(navController = navController) }
      composable(Screen.WifiQR.route) { WifiQRScreen(navController = navController) }
      composable(Screen.CocinaConverter.route) { CocinaConverterScreen(navController = navController) }
      composable(Screen.FeedingCalculator.route) { FeedingCalculatorScreen(navController = navController) }
    }
  }
}
