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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import com.appcasa.features.dashboard.presentation.screen.ArchiveScreen
import com.appcasa.features.dashboard.presentation.screen.DashboardScreen
import com.appcasa.features.family.presentation.screen.FamilyHubScreen
import com.appcasa.features.dashboard.presentation.screen.HomeMaintenanceScreen
import com.appcasa.features.dashboard.presentation.screen.MaintenanceDetailScreen
import com.appcasa.features.dashboard.presentation.screen.ManagementHubScreen
import com.appcasa.features.settings.presentation.screen.AuthScreen
import com.appcasa.features.settings.presentation.screen.SettingsScreen
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

  val showBottomBar = currentDestination?.hasRoute(Screen.HouseSetup::class) == false && 
                      currentDestination.hasRoute(Screen.Auth::class) == false && 
                      isHouseholdSetup == true

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
      startDestination = if (isHouseholdSetup == false) Screen.HouseSetup else Screen.Dashboard,
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = if (isKeyboardVisible || !showBottomBar) 0.dp else innerPadding.calculateBottomPadding())
        .padding(top = innerPadding.calculateTopPadding()),
      enterTransition = { 
        if (targetState.destination.hasRoute(Screen.Dashboard::class) || 
            targetState.destination.hasRoute(Screen.Management::class) ||
            targetState.destination.hasRoute(Screen.FamilyHub::class) ||
            targetState.destination.hasRoute(Screen.Utilities::class)) {
          fadeIn(animationSpec = tween(400))
        } else {
          fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)) 
        }
      },
      exitTransition = { fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f, animationSpec = tween(200)) },
      popEnterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 1.05f, animationSpec = tween(300)) },
      popExitTransition = { fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200)) }
    ) {
      composable<Screen.HouseSetup> { HouseSetupScreen(navController = navController) }
      composable<Screen.Auth> { AuthScreen(navController = navController) }

      composable<Screen.Dashboard> { DashboardScreen(navController = navController) }

      managementGraph(navController)
      familyGraph(navController)
      utilitiesGraph(navController)

      composable<Screen.Settings> { SettingsScreen(navController = navController, innerPadding = innerPadding) }
      composable<Screen.RewardStore> { RewardStoreScreen(navController = navController) }
      composable<Screen.Archive> { ArchiveScreen(navController = navController) }
    }
  }
}

private fun androidx.navigation.NavGraphBuilder.managementGraph(navController: androidx.navigation.NavController) {
  navigation<Screen.ManagementGraph>(
    startDestination = Screen.Management
  ) {
    composable<Screen.Management> { ManagementHubScreen(navController = navController) }
    composable<Screen.Tasks> { TasksScreen(navController = navController) }
    composable<Screen.Lists> { ListsScreen(navController = navController) }
    composable<Screen.Inventory> { StockScreen(navController = navController) }
    composable<Screen.AddTask> { AddTaskScreen(navController = navController) }
    composable<Screen.HomeMaintenance> { HomeMaintenanceScreen(navController = navController) }
    
    composable<Screen.TaskDetail> { TaskDetailScreen(navController = navController) }
    composable<Screen.ListDetail> { ListDetailScreen(navController = navController) }

    composable<Screen.MaintenanceDetail> { backStackEntry ->
      val detail = backStackEntry.toRoute<Screen.MaintenanceDetail>()
      MaintenanceDetailScreen(id = detail.id, navController = navController)
    }
  }
}

private fun androidx.navigation.NavGraphBuilder.familyGraph(navController: androidx.navigation.NavController) {
  navigation<Screen.FamilyGraph>(
    startDestination = Screen.FamilyHub
  ) {
    composable<Screen.FamilyHub> { FamilyHubScreen(navController = navController) }
    composable<Screen.Family> { FamilyScreen(navController = navController) }
    composable<Screen.Calendar> { CalendarScreen(navController = navController) }
    composable<Screen.AddMember> { AddMemberScreen(navController = navController) }
    
    composable<Screen.PetDetail> { PetDetailScreen(navController = navController) }

    composable<Screen.MemberDetail> { backStackEntry ->
      val detail = backStackEntry.toRoute<Screen.MemberDetail>()
      MemberDetailScreen(navController = navController, memberId = detail.memberId)
    }

    composable<Screen.EditMember> { EditMemberScreen(navController = navController) }
  }
}

private fun androidx.navigation.NavGraphBuilder.utilitiesGraph(navController: androidx.navigation.NavController) {
  navigation<Screen.UtilitiesGraph>(
    startDestination = Screen.Utilities
  ) {
    composable<Screen.Utilities> { UtilitiesScreen(navController = navController) }
    composable<Screen.DosageCalculator> { DosageCalculatorScreen(navController = navController) }
    composable<Screen.BMICalculator> { BMICalculatorScreen(navController = navController) }
    composable<Screen.MortgageCalculator> { MortgageCalculatorScreen(navController = navController) }
    composable<Screen.AgeCalculator> { AgeCalculatorScreen(navController = navController) }
    composable<Screen.ConsumptionCalculator> { ConsumptionCalculatorScreen(navController = navController) }
    composable<Screen.SavingsCalculator> { SavingsCalculatorScreen(navController = navController) }
    composable<Screen.VehicleManager> { VehicleManagementScreen(navController = navController) }
    composable<Screen.Expenses> { ExpenseScreen(navController = navController) }
    composable<Screen.PhotoToPdf> { PhotoToPdfScreen(navController = navController) }
    composable<Screen.WifiQR> { WifiQRScreen(navController = navController) }
    composable<Screen.CocinaConverter> { CocinaConverterScreen(navController = navController) }
    composable<Screen.FeedingCalculator> { FeedingCalculatorScreen(navController = navController) }
    composable<Screen.SmartSafe> { SmartSafeScreen(navController = navController) }
    composable<Screen.FinanceStats> { FinanceStatsScreen(navController = navController) }
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
          Screen.Dashboard -> currentDestination?.hasRoute(Screen.Dashboard::class) == true
          Screen.Management -> currentDestination?.hasRoute(Screen.ManagementGraph::class) == true || currentDestination?.hasRoute(Screen.Management::class) == true || Screen.managementTabRoutes.any { currentDestination?.hasRoute(it) == true }
          Screen.FamilyHub -> currentDestination?.hasRoute(Screen.FamilyGraph::class) == true || currentDestination?.hasRoute(Screen.FamilyHub::class) == true || Screen.familyTabRoutes.any { currentDestination?.hasRoute(it) == true }
          Screen.Utilities -> currentDestination?.hasRoute(Screen.UtilitiesGraph::class) == true || currentDestination?.hasRoute(Screen.Utilities::class) == true || Screen.utilitiesTabRoutes.any { currentDestination?.hasRoute(it) == true }
          else -> false
        }
        
        val label = stringResource(item.labelRes)

        NavigationBarItem(
          selected = selected,
          onClick = {
            val isAtHub = currentDestination?.hasRoute(item.screen::class) == true
            if (!isAtHub) {
              if (selected) {
                val popped = navController.popBackStack(item.screen, inclusive = false)
                if (!popped) {
                  navController.navigate(item.screen) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                    launchSingleTop = true
                  }
                }
              } else {
                navController.navigate(item.screen) {
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
