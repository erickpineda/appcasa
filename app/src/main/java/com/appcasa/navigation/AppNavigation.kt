package com.appcasa.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.appcasa.features.calendar.presentation.screen.CalendarScreen
import com.appcasa.features.family.presentation.screen.AddMemberScreen
import com.appcasa.features.family.presentation.screen.EditMemberScreen
import com.appcasa.features.family.presentation.screen.FamilyScreen
import com.appcasa.features.family.presentation.screen.MemberDetailScreen
import com.appcasa.features.finance.presentation.screen.ExpenseScreen
import com.appcasa.features.finance.presentation.screen.FinanceStatsScreen
import com.appcasa.features.inventory.presentation.screen.StockScreen
import com.appcasa.features.lists.presentation.screen.ListDetailScreen
import com.appcasa.features.lists.presentation.screen.ListsScreen
import com.appcasa.features.pets.presentation.screen.PetDetailScreen
import com.appcasa.features.presentation.screen.ArchiveScreen
import com.appcasa.features.presentation.screen.DashboardScreen
import com.appcasa.features.family.presentation.screen.FamilyHubScreen
import com.appcasa.features.presentation.screen.HomeMaintenanceScreen
import com.appcasa.features.presentation.screen.MaintenanceDetailScreen
import com.appcasa.features.presentation.screen.ManagementHubScreen
import com.appcasa.features.presentation.screen.SettingsScreen
import com.appcasa.features.settings.presentation.screen.HouseSetupScreen
import com.appcasa.features.tasks.presentation.screen.AddTaskScreen
import com.appcasa.features.tasks.presentation.screen.RewardStoreScreen
import com.appcasa.features.tasks.presentation.screen.TaskDetailScreen
import com.appcasa.features.tasks.presentation.screen.TasksScreen
import com.appcasa.features.utilities.presentation.screen.AgeCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.BMICalculatorScreen
import com.appcasa.features.utilities.presentation.screen.CocinaConverterScreen
import com.appcasa.features.utilities.presentation.screen.ConsumptionCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.DosageCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.FeedingCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.MortgageCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.PhotoToPdfScreen
import com.appcasa.features.utilities.presentation.screen.SavingsCalculatorScreen
import com.appcasa.features.utilities.presentation.screen.SmartSafeScreen
import com.appcasa.features.utilities.presentation.screen.UtilitiesScreen
import com.appcasa.features.utilities.presentation.screen.VehicleManagementScreen
import com.appcasa.features.utilities.presentation.screen.WifiQRScreen
import com.appcasa.presentation.viewmodel.GlobalViewModel

@Composable
fun AppNavigation(
  globalViewModel: GlobalViewModel = hiltViewModel()
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination
  
  val isHouseholdSetup by globalViewModel.isHouseholdSetup.collectAsState()
  val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

  if (isHouseholdSetup == null) {
      Box(
          modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), 
          contentAlignment = Alignment.Center
      ) {
          CircularProgressIndicator()
      }
      return
  }

  val showBottomBar = currentDestination?.route !in listOf(Screen.HouseSetup.route, Screen.Auth.route) && isHouseholdSetup == true

  Scaffold(
    bottomBar = {
      AppBottomBar(
        navController = navController,
        currentDestination = currentDestination,
        showBottomBar = showBottomBar,
        isKeyboardVisible = isKeyboardVisible
      )
    }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = if (isHouseholdSetup == false) Screen.HouseSetup.route else Screen.Dashboard.route,
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = if (isKeyboardVisible || !showBottomBar) 0.dp else innerPadding.calculateBottomPadding())
        .padding(top = innerPadding.calculateTopPadding()),
      enterTransition = { 
        if (targetState.destination.route == Screen.Dashboard.route || 
            targetState.destination.route == Screen.Management.route ||
            targetState.destination.route == Screen.FamilyHub.route ||
            targetState.destination.route == Screen.Utilities.route) {
          fadeIn(animationSpec = tween(400))
        } else {
          fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)) 
        }
      },
      exitTransition = { fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f, animationSpec = tween(200)) },
      popEnterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 1.05f, animationSpec = tween(300)) },
      popExitTransition = { fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200)) }
    ) {
      composable(Screen.HouseSetup.route) { HouseSetupScreen(navController = navController) }
      composable(Screen.Auth.route) { /* TODO */ }

      composable(Screen.Dashboard.route) { DashboardScreen(navController = navController) }

      // --- Gestión (Management) ---
      navigation(
        startDestination = Screen.Management.route,
        route = "management_graph"
      ) {
        composable(Screen.Management.route) { ManagementHubScreen(navController = navController) }
        composable(Screen.Tasks.route) { TasksScreen(navController = navController) }
        composable(Screen.Lists.route) { ListsScreen(navController = navController) }
        composable(Screen.Inventory.route) { StockScreen(navController = navController) }
        composable(Screen.AddTask.route) { AddTaskScreen(navController = navController) }
        composable(Screen.HomeMaintenance.route) { HomeMaintenanceScreen(navController = navController) }
        
        composable(
          route = Screen.TaskDetail.route,
          arguments = listOf(androidx.navigation.navArgument("taskId") { type = androidx.navigation.NavType.LongType })
        ) { TaskDetailScreen(navController = navController) }

        composable(
          route = Screen.ListDetail.route,
          arguments = listOf(androidx.navigation.navArgument("listId") { type = androidx.navigation.NavType.LongType })
        ) { ListDetailScreen(navController = navController) }

        composable(
          route = Screen.MaintenanceDetail.route,
          arguments = listOf(androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.LongType })
        ) { backStackEntry ->
          val id = backStackEntry.arguments?.getLong("id") ?: 0L
          MaintenanceDetailScreen(id = id, navController = navController)
        }
      }

      // --- Familia (Family) ---
      navigation(
        startDestination = Screen.FamilyHub.route,
        route = "family_graph"
      ) {
        composable(Screen.FamilyHub.route) { FamilyHubScreen(navController = navController) }
        composable(Screen.Family.route) { FamilyScreen(navController = navController) }
        composable(Screen.Calendar.route) { CalendarScreen(navController = navController) }
        composable(Screen.AddMember.route) { AddMemberScreen(navController = navController) }
        
        composable(
          route = Screen.PetDetail.route,
          arguments = listOf(androidx.navigation.navArgument("petId") { type = androidx.navigation.NavType.LongType })
        ) { PetDetailScreen(navController = navController) }

        composable(
          route = Screen.MemberDetail.route,
          arguments = listOf(androidx.navigation.navArgument("memberId") { type = androidx.navigation.NavType.LongType })
        ) { backStackEntry ->
          val memberId = backStackEntry.arguments?.getLong("memberId") ?: 0L
          MemberDetailScreen(navController = navController, memberId = memberId)
        }

        composable(
          route = Screen.EditMember.route,
          arguments = listOf(androidx.navigation.navArgument("memberId") { type = androidx.navigation.NavType.LongType })
        ) { EditMemberScreen(navController = navController) }
      }

      // --- Utilidades (Utilities) ---
      navigation(
        startDestination = Screen.Utilities.route,
        route = "utilities_graph"
      ) {
        composable(Screen.Utilities.route) { UtilitiesScreen(navController = navController) }
        composable(Screen.DosageCalculator.route) { DosageCalculatorScreen(navController = navController) }
        composable(Screen.BMICalculator.route) { BMICalculatorScreen(navController = navController) }
        composable(Screen.MortgageCalculator.route) { MortgageCalculatorScreen(navController = navController) }
        composable(Screen.AgeCalculator.route) { AgeCalculatorScreen(navController = navController) }
        composable(Screen.ConsumptionCalculator.route) { ConsumptionCalculatorScreen(navController = navController) }
        composable(Screen.SavingsCalculator.route) { SavingsCalculatorScreen(navController = navController) }
        composable(Screen.VehicleManager.route) { VehicleManagementScreen(navController = navController) }
        composable(Screen.Expenses.route) { ExpenseScreen(navController = navController) }
        composable(Screen.PhotoToPdf.route) { PhotoToPdfScreen(navController = navController) }
        composable(Screen.WifiQR.route) { WifiQRScreen(navController = navController) }
        composable(Screen.CocinaConverter.route) { CocinaConverterScreen(navController = navController) }
        composable(Screen.FeedingCalculator.route) { FeedingCalculatorScreen(navController = navController) }
        composable(Screen.SmartSafe.route) { SmartSafeScreen(navController = navController) }
        composable(Screen.FinanceStats.route) { FinanceStatsScreen(navController = navController) }
      }

      composable(Screen.Settings.route) { SettingsScreen(navController = navController, innerPadding = innerPadding) }
      composable(Screen.RewardStore.route) { RewardStoreScreen(navController = navController) }
      composable(Screen.Archive.route) { ArchiveScreen(navController = navController) }
    }
  }
}

@Composable
private fun AppBottomBar(
  navController: androidx.navigation.NavHostController,
  currentDestination: androidx.navigation.NavDestination?,
  showBottomBar: Boolean,
  isKeyboardVisible: Boolean
) {
  AnimatedVisibility(
    visible = !isKeyboardVisible && showBottomBar,
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
  ) {
    NavigationBar(
      containerColor = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp
    ) {
      bottomNavItems.forEach { item ->
        val selected = when (item.screen) {
          Screen.Dashboard -> currentDestination?.route == Screen.Dashboard.route
          Screen.Management -> currentDestination?.route in Screen.managementTabRoutes
          Screen.FamilyHub -> currentDestination?.route in Screen.familyTabRoutes
          Screen.Utilities -> currentDestination?.route in Screen.utilitiesTabRoutes
          else -> false
        }
        
        val label = stringResource(item.labelRes)

        NavigationBarItem(
          selected = selected,
          onClick = {
            val isAtHub = currentDestination?.route == item.screen.route
            if (!isAtHub) {
              if (selected) {
                val popped = navController.popBackStack(item.screen.route, inclusive = false)
                if (!popped) {
                  navController.navigate(item.screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                    launchSingleTop = true
                  }
                }
              } else {
                navController.navigate(item.screen.route) {
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = false
                  }
                  launchSingleTop = true
                  restoreState = false
                }
              }
            }
          },
          icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(item.icon, contentDescription = label)
              AnimatedVisibility(visible = selected) {
                Box(
                  modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                )
              }
            }
          },
          label = { if (selected) Text(label) },
          alwaysShowLabel = false
        )
      }
    }
  }
}
