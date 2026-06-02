package com.appcasa.features.tasks.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaEmptyState
import com.appcasa.feature.tasks.R
import com.appcasa.features.tasks.data.local.RecompensaEntity
import com.appcasa.features.tasks.presentation.viewmodel.RewardStoreViewModel
import com.appcasa.core.ui.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardStoreScreen(
    navController: NavController,
    viewModel: RewardStoreViewModel = hiltViewModel()
) {
    val rewards by viewModel.recompensas.collectAsState()
    val members by viewModel.members.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(members) {
        if (selectedMemberId == null && members.isNotEmpty()) {
            selectedMemberId = members.first().id
        }
    }

    if (showAddDialog) {
        AddRewardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { t, p, d ->
                viewModel.addRecompensa(t, p, d)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rewards_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.rewards_new_reward))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (members.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = members.indexOfFirst { it.id == selectedMemberId }.coerceAtLeast(0),
                    containerColor = Color.Transparent,
                    edgePadding = 16.dp
                ) {
                    members.forEach { member ->
                        Tab(
                            selected = member.id == selectedMemberId,
                            onClick = { selectedMemberId = member.id },
                            text = { 
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(member.nombre)
                                    Text("${member.puntos} XP", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (rewards.isEmpty()) {
                    item {
                        AppCasaEmptyState(
                            title = stringResource(R.string.rewards_empty_title),
                            description = stringResource(R.string.rewards_empty_desc),
                            icon = Icons.Default.CardGiftcard,
                            actionText = stringResource(R.string.rewards_btn_create_first),
                            onActionClick = { showAddDialog = true }
                        )
                    }
                } else {
                    items(rewards) { reward ->
                        val currentMember = members.find { it.id == selectedMemberId }
                        val canAfford = (currentMember?.puntos ?: 0) >= reward.costePuntos
                        
                        RewardCard(
                            reward = reward,
                            canAfford = canAfford,
                            onRedeem = { selectedMemberId?.let { viewModel.redeemReward(it, reward) } },
                            onDelete = { viewModel.deleteReward(reward) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardCard(
    reward: RecompensaEntity,
    canAfford: Boolean,
    onRedeem: () -> Unit,
    onDelete: () -> Unit
) {
    AppCasaCard(useGlassmorphism = true) {
        ListItem(
            headlineContent = { Text(reward.titulo, fontWeight = FontWeight.Bold) },
            supportingContent = { reward.descripcion?.let { Text(it) } },
            leadingContent = { Icon(Icons.Default.CardGiftcard, null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${reward.costePuntos} XP", color = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
                    Button(
                        onClick = onRedeem,
                        enabled = canAfford,
                        modifier = Modifier.padding(top = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.rewards_btn_redeem), style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        )
    }
}

@Composable
fun AddRewardDialog(onDismiss: () -> Unit, onConfirm: (String, Int, String?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rewards_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.rewards_label_title)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = points, onValueChange = { points = it }, label = { Text(stringResource(R.string.rewards_label_puntos)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text(stringResource(R.string.rewards_label_desc)) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, points.toIntOrNull() ?: 0, desc) }) {
                Text(stringResource(CoreR.string.common_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(CoreR.string.common_cancel)) } }
    )
}
