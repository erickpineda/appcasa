package com.appcasa.features.settings.presentation.screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appcasa.core.domain.model.Household
import com.appcasa.core.ui.R as CoreR
import com.appcasa.feature.settings.R

@Composable
fun JoinStep(
    codeValue: TextFieldValue,
    isLoading: Boolean,
    discoveredHouse: Household?,
    onCodeChange: (TextFieldValue) -> Unit,
    onScanClick: () -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.setup_join_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.setup_join_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = codeValue,
            onValueChange = onCodeChange,
            label = { Text(stringResource(R.string.setup_label_code)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrect = false
            ),
            placeholder = { Text(stringResource(R.string.setup_placeholder_code)) },
            textStyle = MaterialTheme.typography.titleLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold
            ),
            trailingIcon = {
                IconButton(onClick = onScanClick, enabled = !isLoading) {
                    Icon(Icons.Default.QrCodeScanner, null)
                }
            }
        )

        if (discoveredHouse != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.setup_household_found_label, discoveredHouse.nombre),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onConfirm,
            enabled = codeValue.text.length >= Household.CODE_TOTAL_LENGTH && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.setup_btn_continue))
            }
        }
        TextButton(onClick = onBack, enabled = !isLoading) {
            Text(stringResource(CoreR.string.common_cancel))
        }
    }
}
