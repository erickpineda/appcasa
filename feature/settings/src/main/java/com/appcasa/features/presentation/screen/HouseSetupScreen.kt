package com.appcasa.features.settings.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.data.utils.FileUtils
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.feature.settings.R
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.settings.data.local.HogarEntity
import com.appcasa.features.settings.presentation.viewmodel.HouseSetupViewModel
import com.appcasa.navigation.Screen
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.camera.core.Preview as CameraPreview

@Composable
fun HouseSetupScreen(
    navController: NavController,
    viewModel: HouseSetupViewModel = hiltViewModel()
) {
    val existingHousehold by viewModel.existingHousehold.collectAsState()
    val householdMembers by viewModel.householdMembers.collectAsState()

    // Determinamos el paso inicial de forma instantánea para evitar parpadeos
    val initialStep = remember(existingHousehold) {
        if (existingHousehold != null) SetupStep.SELECT_PROFILE else SetupStep.WELCOME
    }
    var step by remember { mutableStateOf(initialStep) }
    
    // Sincronizar el paso si el hogar se carga tarde
    LaunchedEffect(existingHousehold) {
        if (existingHousehold != null && step == SetupStep.WELCOME) {
            step = SetupStep.SELECT_PROFILE
        }
    }
    
    // Manejo inteligente del botón Atrás del sistema
    BackHandler(enabled = true) {
        when (step) {
            SetupStep.CREATE, SetupStep.JOIN, SetupStep.ADD_PROFILE -> {
                step = if (existingHousehold != null) SetupStep.SELECT_PROFILE else SetupStep.WELCOME
            }
            SetupStep.SELECT_PROFILE -> {
                step = SetupStep.WELCOME
            }
            SetupStep.WELCOME -> {
                // Desde la pantalla raíz siempre salimos de la app
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setupEvent.collect { result ->
            if (result is HouseSetupViewModel.SetupResult.Success) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.HouseSetup.route) { inclusive = true }
                }
            }
        }
    }

    HouseSetupContent(
        step = step,
        onStepChange = { step = it },
        existingHousehold = existingHousehold,
        householdMembers = householdMembers,
        onCreateHousehold = viewModel::createHousehold,
        onJoinHousehold = viewModel::joinHousehold,
        onSelectMember = viewModel::selectMember,
        onResetAll = { 
            viewModel.resetHousehold()
            step = SetupStep.WELCOME 
        }
    )
}

@Composable
private fun HouseSetupContent(
    step: SetupStep,
    onStepChange: (SetupStep) -> Unit,
    existingHousehold: HogarEntity?,
    householdMembers: List<MiembroEntity>,
    onCreateHousehold: (String, String, String?) -> Unit,
    onJoinHousehold: (String, String, String?) -> Unit,
    onSelectMember: (MiembroEntity) -> Unit,
    onResetAll: () -> Unit
) {
    var inputName by remember { mutableStateOf("") }
    var inputUserName by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var userPhotoUri by remember { mutableStateOf<String?>(null) }
    var showScanner by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            userPhotoUri = FileUtils.saveImageLocally(context, it.toString())
        }
    }

    AppCasaMeshBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "setup_steps"
            ) { targetStep ->
                when (targetStep) {
                    SetupStep.WELCOME -> WelcomeStep(
                        onCreateClick = { onStepChange(SetupStep.CREATE) },
                        onJoinClick = { onStepChange(SetupStep.JOIN) },
                        onLoginClick = { onStepChange(SetupStep.SELECT_PROFILE) },
                        householdName = existingHousehold?.nombre
                    )
                    SetupStep.CREATE -> CreateStep(
                        name = inputName,
                        userName = inputUserName,
                        photoUri = userPhotoUri,
                        onNameChange = { inputName = it },
                        onUserNameChange = { inputUserName = it },
                        onPhotoClick = { imagePickerLauncher.launch("image/*") },
                        onBack = { 
                            onStepChange(if (existingHousehold != null) SetupStep.SELECT_PROFILE else SetupStep.WELCOME) 
                        },
                        onConfirm = { onCreateHousehold(inputName, inputUserName, userPhotoUri) }
                    )
                    SetupStep.JOIN -> JoinStep(
                        code = inputCode,
                        userName = inputUserName,
                        photoUri = userPhotoUri,
                        onCodeChange = { inputCode = it },
                        onUserNameChange = { inputUserName = it },
                        onPhotoClick = { imagePickerLauncher.launch("image/*") },
                        onScanClick = { showScanner = true },
                        onBack = { 
                            onStepChange(if (existingHousehold != null) SetupStep.SELECT_PROFILE else SetupStep.WELCOME) 
                        },
                        onConfirm = { onJoinHousehold(inputCode, inputUserName, userPhotoUri) }
                    )
                    SetupStep.SELECT_PROFILE -> SelectProfileStep(
                        existingHousehold = existingHousehold,
                        members = householdMembers,
                        onMemberClick = onSelectMember,
                        onAddProfileClick = { onStepChange(SetupStep.ADD_PROFILE) },
                        onResetAll = onResetAll
                    )
                    SetupStep.ADD_PROFILE -> AddProfileStep(
                        userName = inputUserName,
                        photoUri = userPhotoUri,
                        onUserNameChange = { inputUserName = it },
                        onPhotoClick = { imagePickerLauncher.launch("image/*") },
                        onBack = { onStepChange(SetupStep.SELECT_PROFILE) },
                        onConfirm = { 
                            existingHousehold?.let { 
                                onJoinHousehold(it.codigoHogar ?: "", inputUserName, userPhotoUri) 
                            }
                        }
                    )
                }
            }
            
            if (showScanner) {
                QRScannerDialog(
                    onCodeScanned = { 
                        inputCode = it
                        showScanner = false 
                    },
                    onDismiss = { showScanner = false }
                )
            }
        }
    }
}

@Composable
fun QRScannerDialog(onCodeScanned: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var hasPermission by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) 
    }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escanear Código de Casa") },
        text = {
            if (hasPermission) {
                Box(modifier = Modifier.size(280.dp).clip(RoundedCornerShape(16.dp))) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = CameraPreview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                
                                val scanner = BarcodeScanning.getClient()
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                
                                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    @OptIn(ExperimentalGetImage::class)
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                        scanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                for (barcode in barcodes) {
                                                    barcode.rawValue?.let { code ->
                                                        if (code.startsWith("CASA-")) {
                                                            onCodeScanned(code)
                                                        }
                                                    }
                                                }
                                            }
                                            .addOnCompleteListener { imageProxy.close() }
                                    }
                                }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text("Se requiere permiso de cámara para escanear el QR.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AddProfileStep(
    userName: String, 
    photoUri: String?,
    onUserNameChange: (String) -> Unit,
    onPhotoClick: () -> Unit,
    onBack: () -> Unit, 
    onConfirm: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¿Quién eres?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Introduce tu nombre para entrar al hogar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        
        // Avatar Selection
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onPhotoClick() },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Tu Foto", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text(stringResource(R.string.settings_user_name_title)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Button(onClick = onConfirm, enabled = userName.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Entrar")
        }
        TextButton(onClick = onBack) { Text(stringResource(R.string.settings_btn_cancel)) }
    }
}

@Composable
private fun WelcomeStep(
    onCreateClick: () -> Unit, 
    onJoinClick: () -> Unit,
    onLoginClick: (() -> Unit)? = null,
    householdName: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.setup_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (householdName != null) "Hogar detectado: $householdName" else stringResource(R.string.setup_welcome_desc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.Groups, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.setup_btn_create_house))
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.VpnKey, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.setup_btn_join_house))
        }

        if (onLoginClick != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Person, null)
                Spacer(Modifier.width(8.dp))
                Text("Entrar en mi Hogar")
            }
        }
    }
}

@Composable
private fun CreateStep(
    name: String, 
    userName: String, 
    photoUri: String?,
    onNameChange: (String) -> Unit, 
    onUserNameChange: (String) -> Unit,
    onPhotoClick: () -> Unit,
    onBack: () -> Unit, 
    onConfirm: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.setup_create_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.setup_create_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        
        // Avatar Selection
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onPhotoClick() },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Tu Foto", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.setup_label_house_name)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text(stringResource(R.string.settings_user_name_title)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Button(onClick = onConfirm, enabled = name.isNotBlank() && userName.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.setup_btn_finish))
        }
        TextButton(onClick = onBack) { Text(stringResource(R.string.settings_btn_cancel)) }
    }
}

@Composable
private fun JoinStep(
    code: String, 
    userName: String,
    photoUri: String?,
    onCodeChange: (String) -> Unit, 
    onUserNameChange: (String) -> Unit,
    onPhotoClick: () -> Unit,
    onScanClick: () -> Unit,
    onBack: () -> Unit, 
    onConfirm: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.setup_join_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.setup_join_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Selection
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { onPhotoClick() },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Tu Foto", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Spacer(Modifier.width(24.dp))
            
            // QR Scanner Placeholder Button
            OutlinedButton(
                onClick = onScanClick,
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = code.uppercase(),
            onValueChange = { if (it.length <= 10) onCodeChange(it) },
            label = { Text(stringResource(R.string.setup_label_code)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("CASA-XXXX") },
            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text(stringResource(R.string.settings_user_name_title)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Button(onClick = onConfirm, enabled = code.length >= 6 && userName.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.setup_btn_join))
        }
        TextButton(onClick = onBack) { Text(stringResource(R.string.settings_btn_cancel)) }
    }
}

@Composable
private fun SelectProfileStep(
    existingHousehold: HogarEntity?,
    members: List<MiembroEntity>,
    onMemberClick: (MiembroEntity) -> Unit,
    onAddProfileClick: () -> Unit,
    onResetAll: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¿Quién eres?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Selecciona tu perfil para entrar", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        // Debug info
        Text("Hogar: ${existingHousehold?.nombre ?: "None"} (ID: ${existingHousehold?.id ?: "N/A"})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

        Spacer(Modifier.height(32.dp))
        
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), contentAlignment = Alignment.Center) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                val people = members.filter { it.tipo.uppercase() == TipoMiembro.PERSONA.name }
                items(people) { member ->
                    ProfileAvatar(member) { onMemberClick(member) }
                }
                
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onAddProfileClick() }) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Nuevo", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (members.isEmpty()) {
            Text(
                "No hay miembros registrados todavía.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        var showConfirmReset by remember { mutableStateOf(false) }
        
        if (showConfirmReset) {
            AlertDialog(
                onDismissRequest = { showConfirmReset = false },
                title = { Text("¿Borrar todo?") },
                text = { Text("Esta acción eliminará permanentemente este hogar y todos sus datos (tareas, gastos, etc.) de este dispositivo.") },
                confirmButton = {
                    Button(onClick = { onResetAll(); showConfirmReset = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Borrar permanentemente")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmReset = false }) { Text("Cancelar") }
                }
            )
        }

        TextButton(onClick = { showConfirmReset = true }) {
            Icon(Icons.Default.RestartAlt, null)
            Spacer(Modifier.width(8.dp))
            Text("Empezar de cero (Borrar hogar)")
        }
    }
}

@Composable
private fun ProfileAvatar(member: MiembroEntity, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (member.fotoUri != null) {
                AsyncImage(
                    model = member.fotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(member.nombre, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

private enum class SetupStep {
    WELCOME, CREATE, JOIN, SELECT_PROFILE, ADD_PROFILE
}

@Preview(showBackground = true)
@Composable
fun HouseSetupPreview_Welcome() {
    AppCasaTheme {
        HouseSetupContent(
            step = SetupStep.WELCOME,
            onStepChange = {},
            existingHousehold = null,
            householdMembers = emptyList(),
            onCreateHousehold = { _, _, _ -> },
            onJoinHousehold = { _, _, _ -> },
            onSelectMember = {},
            onResetAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HouseSetupPreview_SelectProfile() {
    AppCasaTheme {
        HouseSetupContent(
            step = SetupStep.SELECT_PROFILE,
            onStepChange = {},
            existingHousehold = HogarEntity(id = 1, nombre = "Mi Casa"),
            householdMembers = listOf(
                MiembroEntity(id = 1, hogarId = 1, nombre = "Juan"),
                MiembroEntity(id = 2, hogarId = 1, nombre = "Maria")
            ),
            onCreateHousehold = { _, _, _ -> },
            onJoinHousehold = { _, _, _ -> },
            onSelectMember = {},
            onResetAll = {}
        )
    }
}
