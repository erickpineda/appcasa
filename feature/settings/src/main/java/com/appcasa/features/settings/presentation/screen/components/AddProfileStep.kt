package com.appcasa.features.settings.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.R as CoreR
import com.appcasa.feature.settings.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileStep(
  userName: String,
  photoUri: String?,
  isLoading: Boolean,
  onUserNameChange: (String) -> Unit,
  onPhotoClick: () -> Unit,
  onBack: () -> Unit,
  onConfirm: (TipoMiembro, String?, Long?) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedType by remember { mutableStateOf(TipoMiembro.PERSONA) }
  var breed by remember { mutableStateOf("") }
  var birthDate by remember { mutableStateOf<Long?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }

  val datePickerState = rememberDatePickerState()

  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          birthDate = datePickerState.selectedDateMillis
          showDatePicker = false
        }) { Text(stringResource(CoreR.string.common_ok)) }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  AppCasaCard(
    useGlassmorphism = true,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = stringResource(R.string.setup_profile_who_are_you),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = stringResource(R.string.setup_profile_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(Modifier.height(24.dp))

      // Member Type Selector
      MemberTypeSelector(
        selectedType = selectedType,
        onTypeSelected = { selectedType = it },
        enabled = !isLoading
      )

      Spacer(Modifier.height(24.dp))

      // Avatar Selection
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
          .size(100.dp)
          .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
          .clickable(enabled = !isLoading) { onPhotoClick() }
      ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
          if (photoUri != null) {
            AsyncImage(
              model = photoUri,
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = when(selectedType) {
                  TipoMiembro.PERSONA -> Icons.Default.AddAPhoto
                  else -> Icons.Default.Pets
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(Modifier.height(4.dp))
              Text(
                text = stringResource(R.string.setup_profile_your_photo),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }
        }
      }

      Spacer(Modifier.height(24.dp))

      OutlinedTextField(
        value = userName,
        onValueChange = onUserNameChange,
        label = { 
          Text(
            if (selectedType == TipoMiembro.PERSONA) stringResource(R.string.settings_user_name_title)
            else stringResource(R.string.setup_label_pet_name)
          ) 
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
      )

      if (selectedType != TipoMiembro.PERSONA) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
          value = breed,
          onValueChange = { breed = it },
          label = { Text(stringResource(CoreR.string.family_label_breed)) },
          modifier = Modifier.fillMaxWidth(),
          enabled = !isLoading,
          shape = RoundedCornerShape(16.dp),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
      }

      Spacer(Modifier.height(12.dp))
      
      OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        enabled = !isLoading
      ) {
        val label = if (birthDate == null) {
          stringResource(if (selectedType == TipoMiembro.PERSONA) CoreR.string.family_label_birth_date else CoreR.string.family_label_birth_date_pet)
        } else {
          SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(birthDate!!))
        }
        Icon(Icons.Default.Cake, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label)
      }

      Spacer(Modifier.height(32.dp))

      Button(
        onClick = { onConfirm(selectedType, breed.ifBlank { null }, birthDate) },
        enabled = userName.isNotBlank() && !isLoading,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Text(stringResource(R.string.setup_btn_enter), fontWeight = FontWeight.Bold)
        }
      }
      Spacer(Modifier.height(16.dp))
      TextButton(onClick = onBack, enabled = !isLoading) {
        Text(stringResource(CoreR.string.common_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
private fun MemberTypeSelector(
  selectedType: TipoMiembro,
  onTypeSelected: (TipoMiembro) -> Unit,
  enabled: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceEvenly
  ) {
    TypeItem(
      type = TipoMiembro.PERSONA,
      icon = Icons.Default.Person,
      isSelected = selectedType == TipoMiembro.PERSONA,
      enabled = enabled,
      onClick = { onTypeSelected(TipoMiembro.PERSONA) }
    )
    TypeItem(
      type = TipoMiembro.PERRO,
      icon = Icons.Default.Pets,
      isSelected = selectedType == TipoMiembro.PERRO,
      enabled = enabled,
      onClick = { onTypeSelected(TipoMiembro.PERRO) }
    )
    TypeItem(
      type = TipoMiembro.GATO,
      icon = Icons.Default.Pets,
      isSelected = selectedType == TipoMiembro.GATO,
      enabled = enabled,
      onClick = { onTypeSelected(TipoMiembro.GATO) }
    )
    TypeItem(
      type = TipoMiembro.OTRO,
      icon = Icons.Default.QuestionMark,
      isSelected = selectedType == TipoMiembro.OTRO,
      enabled = enabled,
      onClick = { onTypeSelected(TipoMiembro.OTRO) }
    )
  }
}

@Composable
private fun TypeItem(
  type: TipoMiembro,
  icon: ImageVector,
  isSelected: Boolean,
  enabled: Boolean,
  onClick: () -> Unit
) {
  val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
  val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(enabled = enabled) { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(color),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, null, tint = contentColor, modifier = Modifier.size(24.dp))
    }
    Spacer(Modifier.height(4.dp))
    Text(
      text = type.name.lowercase().replaceFirstChar { it.uppercase() },
      style = MaterialTheme.typography.labelSmall,
      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
