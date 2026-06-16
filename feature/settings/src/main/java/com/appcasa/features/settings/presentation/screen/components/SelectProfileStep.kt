package com.appcasa.features.settings.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.R as CoreR
import com.appcasa.feature.settings.R

@Composable
fun SelectProfileStep(
    existingHousehold: Household?,
    members: List<FamilyMember>,
    isLoading: Boolean,
    onMemberClick: (FamilyMember) -> Unit,
    onAddProfileClick: () -> Unit,
    onSwitchHouseClick: (() -> Unit)?,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmReset by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.setup_profile_who_are_you),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.setup_select_profile_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        AppCasaCard(
            useGlassmorphism = true,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = existingHousehold?.nombre ?: "...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.setup_selected_household_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onSwitchHouseClick != null) {
                    IconButton(
                        onClick = onSwitchHouseClick,
                        enabled = !isLoading,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.SyncAlt, null)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    val people = members.filter { it.tipo == TipoMiembro.PERSONA }
                    items(people) { member ->
                        ProfileAvatar(member) {
                            if (!isLoading) onMemberClick(member)
                        }
                    }

                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(enabled = !isLoading) { onAddProfileClick() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.setup_btn_new_profile),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        if (members.isEmpty() && !isLoading) {
            Text(
                text = stringResource(R.string.setup_no_members_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(48.dp))

        if (showConfirmReset) {
            AlertDialog(
                onDismissRequest = { showConfirmReset = false },
                title = { Text(stringResource(R.string.setup_reset_title)) },
                text = { Text(stringResource(R.string.setup_reset_desc)) },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetAll()
                            showConfirmReset = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.setup_btn_reset_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmReset = false }) {
                        Text(stringResource(CoreR.string.common_cancel))
                    }
                }
            )
        }

        TextButton(onClick = { showConfirmReset = true }) {
            Icon(Icons.Default.RestartAlt, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.setup_btn_reset_all))
        }
    }
}

@Composable
private fun ProfileAvatar(member: FamilyMember, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val imageModel = member.fotoUri ?: member.urlNube
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = member.nombre,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
