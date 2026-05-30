package com.appcasa.features.utilities.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.features.utilities.data.local.UtilidadEntity
import com.appcasa.features.utilities.presentation.viewmodel.UtilitiesViewModel
import com.appcasa.navigation.Screen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UtilitiesScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val utilities by viewModel.utilities.collectAsState()

  PullToRefreshWrapper {
    UtilitiesContent(
      utilities = utilities,
      onInitialize = { viewModel.initializeUtilities() },
      onUtilityClick = { utility ->
        when (utility.codigo) {
          "CALC_DOSIS" -> navController.navigate(Screen.DosageCalculator.route)
          "CALC_IMC" -> navController.navigate(Screen.BMICalculator.route)
          "CALC_HIPOTECA" -> navController.navigate(Screen.MortgageCalculator.route)
          "CALC_EDAD" -> navController.navigate(Screen.AgeCalculator.route)
          "CALC_CONSUMO" -> navController.navigate(Screen.ConsumptionCalculator.route)
          "CALC_AHORRO" -> navController.navigate(Screen.SavingsCalculator.route)
          "FIN_GASTOS" -> navController.navigate(Screen.Expenses.route)
          "VEH_MGR" -> navController.navigate(Screen.VehicleManager.route)
          "UTIL_PDF" -> navController.navigate(Screen.PhotoToPdf.route)
          "UTIL_WIFI" -> navController.navigate(Screen.WifiQR.route)
          "UTIL_COCINA" -> navController.navigate(Screen.CocinaConverter.route)
          "UTIL_PIENSO" -> navController.navigate(Screen.FeedingCalculator.route)
          "UTIL_SAFE" -> navController.navigate(Screen.SmartSafe.route)
        }
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UtilitiesContent(
  utilities: List<UtilidadEntity>,
  onInitialize: () -> Unit,
  onUtilityClick: (UtilidadEntity) -> Unit
) {
  val groupedUtilities = utilities.groupBy { it.categoria }
  var expandedCategories by rememberSaveable { mutableStateOf(groupedUtilities.keys.toSet()) }
  val allExpanded = expandedCategories.size == groupedUtilities.size

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Utilidades") },
        actions = {
          if (groupedUtilities.isNotEmpty()) {
            IconButton(
              onClick = {
                expandedCategories = if (allExpanded) emptySet() else groupedUtilities.keys.toSet()
              }
            ) {
              Icon(
                imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                contentDescription = if (allExpanded) "Contraer todo" else "Expandir todo",
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
    }
  ) { scaffoldPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(scaffoldPadding)
    ) {
      if (utilities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No hay utilidades configuradas")
            Button(onClick = onInitialize, modifier = Modifier.padding(top = 16.dp)) {
              Text("Inicializar Utilidades")
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
                  contentDescription = if (isExpanded) "Contraer" else "Expandir",
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
                  // Si hay un número impar de utilidades, añadimos un espaciador para el layout de FlowRow
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
fun UtilityCard(utility: UtilidadEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
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

  com.appcasa.core.ui.components.AppCasaCard(useGlassmorphism = true,
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
