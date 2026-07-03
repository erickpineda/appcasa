package com.appcasa.navigation
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import com.appcasa.core.ui.utils.LocalSyncAction
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigation.navDeepLink
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigation(
  globalViewModel: GlobalViewModel = hiltViewModel()
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination
  
  val isHouseholdSetup by globalViewModel.isHouseholdSetup.collectAsStateWithLifecycle()
  val isKeyboardVisible = WindowInsets.isImeVisible

  // Manejo de Seguridad (FLAG_SECURE) basado en la ruta
  LaunchedEffect(currentDestination) {
      val isSensitiveScreen = currentDestination?.hasRoute<Screen.SmartSafe>() == true ||
                              currentDestination?.hasRoute<Screen.Expenses>() == true ||
                              currentDestination?.hasRoute<Screen.FinanceStats>() == true ||
                              currentDestination?.hasRoute<Screen.MortgageCalculator>() == true ||
                              currentDestination?.hasRoute<Screen.SavingsCalculator>() == true ||
                              currentDestination?.hasRoute<Screen.Archive>() == true ||
                              currentDestination?.hasRoute<Screen.Auth>() == true
      globalViewModel.setSecureMode(isSensitiveScreen)
  }

  LaunchedEffect(isHouseholdSetup) {
      if (isHouseholdSetup == false) {
          navController.navigate(Screen.HouseSetup()) {
              popUpTo(0) { inclusive = true }
          }
      } else if (isHouseholdSetup == true && currentDestination?.hasRoute<Screen.HouseSetup>() == true) {
          navController.navigate(Screen.Dashboard) {
              popUpTo(0) { inclusive = true }
          }
      }
  }

  if (isHouseholdSetup == null) {
      Box(
          modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), 
          contentAlignment = Alignment.Center
      ) {
          CircularProgressIndicator()
      }
      return
  }

  val showBottomBar = isHouseholdSetup == true && 
                      currentDestination?.hasRoute<Screen.HouseSetup>() != true && 
                      currentDestination?.hasRoute<Screen.Auth>() != true

  CompositionLocalProvider(
    LocalSyncAction provides {
        globalViewModel.triggerManualSync()
        delay(1000)
    }
  ) {
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
        startDestination = if (isHouseholdSetup == false) Screen.HouseSetup() else Screen.Dashboard,
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = if (isKeyboardVisible || !showBottomBar) 0.dp else innerPadding.calculateBottomPadding())
          .padding(top = innerPadding.calculateTopPadding()),
        enterTransition = { 
          if (targetState.destination.hasRoute<Screen.Dashboard>() || 
              targetState.destination.hasRoute<Screen.Management>() ||
              targetState.destination.hasRoute<Screen.FamilyHub>() ||
              targetState.destination.hasRoute<Screen.Utilities>()) {
            fadeIn(animationSpec = tween(400))
          } else {
            fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)) 
          }
        },
        exitTransition = { fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f, animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 1.05f, animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200)) }
      ) {
        composable<Screen.HouseSetup>(
          deepLinks = listOf(
            navDeepLink {
              uriPattern = "appcasa://join/{code}"
            }
          )
        ) { backStackEntry ->
          val route = backStackEntry.toRoute<Screen.HouseSetup>()
          HouseSetupScreen(
            navController = navController,
            initialCode = route.code
          )
        }
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
          Screen.Dashboard -> currentDestination?.hierarchy?.any { it.hasRoute<Screen.Dashboard>() } == true
          Screen.Management -> currentDestination?.hierarchy?.any { it.hasRoute<Screen.ManagementGraph>() || it.hasRoute<Screen.Management>() || Screen.managementTabRoutes.any { route -> it.hasRoute(route) } } == true
          Screen.FamilyHub -> currentDestination?.hierarchy?.any { it.hasRoute<Screen.FamilyGraph>() || it.hasRoute<Screen.FamilyHub>() || Screen.familyTabRoutes.any { route -> it.hasRoute(route) } } == true
          Screen.Utilities -> currentDestination?.hierarchy?.any { it.hasRoute<Screen.UtilitiesGraph>() || it.hasRoute<Screen.Utilities>() || Screen.utilitiesTabRoutes.any { route -> it.hasRoute(route) } } == true
          else -> false
        }
        
        val label = stringResource(item.labelRes)

        NavigationBarItem(
          selected = selected,
          onClick = {
            val isAlreadyOnSelectedTab = when (item.screen) {
              Screen.Dashboard -> currentDestination?.hasRoute<Screen.Dashboard>() == true
              Screen.Management -> currentDestination?.hasRoute<Screen.Management>() == true
              Screen.FamilyHub -> currentDestination?.hasRoute<Screen.FamilyHub>() == true
              Screen.Utilities -> currentDestination?.hasRoute<Screen.Utilities>() == true
              else -> false
            }

            if (!isAlreadyOnSelectedTab) {
              navController.navigate(item.screen) {
                popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
                }
                launchSingleTop = true
                restoreState = true
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
