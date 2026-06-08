package com.appcasa.features.utilities.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.Utility
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.feature.dashboard.R
import com.appcasa.features.utilities.presentation.viewmodel.UtilitiesViewModel
import com.appcasa.navigation.Screen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UtilitiesScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val utilities by viewModel.utilities.collectAsState()

  AppCasaMeshBackground {
    PullToRefreshWrapper {
      UtilitiesContent(
        utilities = utilities,
        onInitialize = { viewModel.initializeUtilities() },
        onUtilityClick = { utility ->
          when (utility.codigo) {
            "CALC_DOSIS" -> navController.navigate(Screen.DosageCalculator)
            "CALC_IMC" -> navController.navigate(Screen.BMICalculator)
            "CALC_HIPOTECA" -> navController.navigate(Screen.MortgageCalculator)
            "CALC_EDAD" -> navController.navigate(Screen.AgeCalculator)
            "CALC_CONSUMO" -> navController.navigate(Screen.ConsumptionCalculator)
            "CALC_AHORRO" -> navController.navigate(Screen.SavingsCalculator)
            "FIN_GASTOS" -> navController.navigate(Screen.Expenses)
            "VEH_MGR" -> navController.navigate(Screen.VehicleManager)
            "UTIL_PDF" -> navController.navigate(Screen.PhotoToPdf)
            "UTIL_WIFI" -> navController.navigate(Screen.WifiQR)
            "UTIL_COCINA" -> navController.navigate(Screen.CocinaConverter)
            "UTIL_PIENSO" -> navController.navigate(Screen.FeedingCalculator)
            "UTIL_SAFE" -> navController.navigate(Screen.SmartSafe)
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UtilitiesContent(
  utilities: List<Utility>,
  onInitialize: () -> Unit,
  onUtilityClick: (Utility) -> Unit
) {
  val groupedUtilities = utilities.groupBy { it.categoria }
  var expandedCategories by rememberSaveable { mutableStateOf(groupedUtilities.keys.toSet()) }
  val allExpanded = expandedCategories.size == groupedUtilities.size

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.utilities_title)) },
        actions = {
          if (groupedUtilities.isNotEmpty()) {
            IconButton(
              onClick = {
                expandedCategories = if (allExpanded) emptySet() else groupedUtilities.keys.toSet()
              }
            ) {
              Icon(
                imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                contentDescription = if (allExpanded) stringResource(R.string.utilities_collapse_all) else stringResource(R.string.utilities_expand_all),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    containerColor = Color.Transparent
  ) { scaffoldPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(scaffoldPadding)
    ) {
      if (utilities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.utilities_empty))
            Button(onClick = onInitialize, modifier = Modifier.padding(top = 16.dp)) {
              Text(stringResource(R.string.utilities_btn_initialize))
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          groupedUtilities.forEach { (categoria, utils) ->
            val isExpanded = expandedCategories.contains(categoria)
            item(key = categoria) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    expandedCategories = if (isExpanded) {
                      expandedCategories - categoria
                    } else {
                      expandedCategories + categoria
                    }
                  }
                  .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = categoria,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Icon(
                  imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                  contentDescription = if (isExpanded) stringResource(R.string.utilities_collapse) else stringResource(R.string.utilities_expand),
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            
            item(key = "${categoria}_grid") {
              AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
              ) {
                FlowRow(
                  modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp),
                  maxItemsInEachRow = 2
                ) {
                  utils.forEach { utility ->
                    UtilityCard(
                      utility = utility,
                      onClick = { onUtilityClick(utility) },
                      modifier = Modifier.weight(1f).fillMaxWidth(0.45f)
                    )
                  }
                  if (utils.size % 2 != 0) {
                      Spacer(modifier = Modifier.weight(1f).fillMaxWidth(0.45f))
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun UtilityCard(utility: Utility, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val icon = when (utility.icono) {
    "medication" -> Icons.Default.Medication
    "monitor_weight" -> Icons.Default.MonitorWeight
    "home" -> Icons.Default.Home
    "cake" -> Icons.Default.Cake
    "bolt" -> Icons.Default.Bolt
    "savings" -> Icons.Default.Savings
    "straighten" -> Icons.Default.Straighten
    "payments" -> Icons.Default.Payments
    "directions_car" -> Icons.Default.DirectionsCar
    "picture_as_pdf" -> Icons.Default.PictureAsPdf
    "qr_code" -> Icons.Default.QrCode
    "restaurant" -> Icons.Default.Restaurant
    "lock" -> Icons.Default.Lock
    "pets" -> Icons.Default.Pets
    else -> Icons.Default.Apps
  }

  AppCasaCard(useGlassmorphism = true,
    modifier = modifier
      .height(140.dp)
      .alpha(0.8f),
    onClick = onClick
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = utility.nombre,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )
      Text(
        text = utility.descripcion ?: "",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
