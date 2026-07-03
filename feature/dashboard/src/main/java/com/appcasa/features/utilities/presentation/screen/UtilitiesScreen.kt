package com.appcasa.features.utilities.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.domain.model.Utility
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.PullToRefreshWrapper
import com.appcasa.feature.dashboard.R
import com.appcasa.features.utilities.presentation.viewmodel.UtilitiesViewModel
import com.appcasa.navigation.Screen

@Composable
fun UtilitiesScreen(
  navController: NavController,
  viewModel: UtilitiesViewModel = hiltViewModel()
) {
  val utilities by viewModel.utilities.collectAsStateWithLifecycle()

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
  var expandedCategories by rememberSaveable { mutableStateOf<Set<String>?>(null) }
  
  LaunchedEffect(groupedUtilities.keys) {
      if (expandedCategories == null && groupedUtilities.isNotEmpty()) {
          expandedCategories = groupedUtilities.keys.toSet()
      }
  }
  
  val currentExpanded = expandedCategories ?: groupedUtilities.keys.toSet()
  val allExpanded = groupedUtilities.isNotEmpty() && currentExpanded.size == groupedUtilities.size

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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
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
            val isExpanded = currentExpanded.contains(categoria)
            item(key = categoria) {
              CategoryHeader(
                title = categoria,
                isExpanded = isExpanded,
                onToggle = {
                  expandedCategories = if (isExpanded) currentExpanded - categoria else currentExpanded + categoria
                }
              )
            }
            
            if (isExpanded) {
              // Renderizamos las utilidades en filas de 2 manualmente para evitar el FlowRow bug
              val chunks = utils.chunked(2)
              items(
                count = chunks.size,
                key = { index -> "${categoria}_$index" }
              ) { index ->
                val rowUtils = chunks[index]
                Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  rowUtils.forEach { utility ->
                    UtilityCard(
                      utility = utility,
                      onClick = { onUtilityClick(utility) },
                      modifier = Modifier.weight(1f)
                    )
                  }
                  if (rowUtils.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
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
fun CategoryHeader(title: String, isExpanded: Boolean, onToggle: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() }
      .padding(vertical = 12.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )
    Icon(
      imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp)
    )
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

  AppCasaCard(
    useGlassmorphism = true,
    modifier = modifier.height(130.dp).alpha(0.9f),
    onClick = onClick
  ) {
    Column(
      modifier = Modifier.padding(12.dp).fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(28.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = utility.nombre,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1
      )
      Text(
        text = utility.descripcion ?: "",
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        lineHeight = 12.sp
      )
    }
  }
}
