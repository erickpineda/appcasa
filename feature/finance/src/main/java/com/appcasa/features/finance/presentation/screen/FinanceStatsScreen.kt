package com.appcasa.features.finance.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.feature.finance.R
import com.appcasa.features.finance.presentation.viewmodel.FinanceViewModel
import com.appcasa.core.ui.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceStatsScreen(
    navController: NavController,
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val context = LocalContext.current

    fun android.content.Context.findActivity(): FragmentActivity? {
        var context = this
        while (context is android.content.ContextWrapper) {
            if (context is FragmentActivity) return context
            context = context.baseContext
        }
        return null
    }

    LaunchedEffect(Unit) {
        if (!isUnlocked) {
            kotlinx.coroutines.delay(300)
            context.findActivity()?.let { viewModel.authenticate(it) }
        }
    }

    if (!isUnlocked) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                Text(stringResource(CoreR.string.lock_finance_stats_title), style = MaterialTheme.typography.titleLarge)
                androidx.compose.material3.TextButton(onClick = { 
                    context.findActivity()?.let { viewModel.authenticate(it) }
                }) {
                    Text(stringResource(CoreR.string.lock_btn_unlock))
                }
            }
        }
        return
    }

    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val monthlyEvolution by viewModel.monthlyEvolution.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.finance_stats_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(stringResource(R.string.finance_stats_by_category), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CategoryPieChart(data = expensesByCategory, currency = currency)

            Text(stringResource(R.string.finance_stats_monthly), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            MonthlyBarChart(data = monthlyEvolution)
        }
    }
}

@Composable
fun CategoryPieChart(data: Map<String, Double>, currency: String) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF9C27B0)
    )

    AppCasaCard(useGlassmorphism = true) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(150.dp)) {
                val total = data.values.sum().coerceAtLeast(1.0)
                var startAngle = 0f
                data.entries.forEachIndexed { index, entry ->
                    val sweepAngle = (entry.value / total * 360f).toFloat()
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }
            
            Column(modifier = Modifier.padding(start = 16.dp)) {
                data.entries.forEachIndexed { index, entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(MaterialTheme.shapes.extraSmall).background(colors[index % colors.size]))
                        Text(
                            text = stringResource(R.string.finance_stats_category_label, entry.key, entry.value, currency),
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyBarChart(data: Map<String, Double>) {
    val barColor = MaterialTheme.colorScheme.primary
    AppCasaCard(useGlassmorphism = true) {
        Box(modifier = Modifier.padding(16.dp).height(200.dp).fillMaxWidth()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxVal = (data.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0).toFloat()
                val barWidth = size.width / (data.size * 2f).coerceAtLeast(1f)
                
                data.entries.forEachIndexed { index, entry ->
                    val barHeight = (entry.value.toFloat() / maxVal) * size.height
                    drawRect(
                        color = barColor,
                        topLeft = Offset(index * barWidth * 2f + barWidth / 2f, size.height - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
        }
    }
}
