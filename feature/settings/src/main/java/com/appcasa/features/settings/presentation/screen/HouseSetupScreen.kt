package com.appcasa.features.settings.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.appcasa.core.data.utils.FileUtils
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.utils.Constants
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.feature.settings.R
import com.appcasa.features.settings.presentation.viewmodel.HouseSetupViewModel
import com.appcasa.navigation.Screen
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.appcasa.core.ui.R as CoreR
import com.google.mlkit.vision.common.InputImage
import com.appcasa.core.ui.utils.UiText
import java.util.concurrent.Executors
import androidx.camera.core.Preview as CameraPreview

@Composable
fun HouseSetupScreen(
  navController: NavController,
  viewModel: HouseSetupViewModel = hiltViewModel()
) {
  val existingHousehold by viewModel.existingHousehold.collectAsState()
  val allHouseholds by viewModel.allHouseholds.collectAsState()
  val householdMembers by viewModel.householdMembers.collectAsState()
  val isCheckingDb by viewModel.isCheckingDb.collectAsState()
  val discoveredHouse by viewModel.discoveredHousehold.collectAsState()
  val isLoggedIn by viewModel.isLoggedIn.collectAsState()

  var step by remember { mutableStateOf<SetupStep?>(null) }
  var errorMessage by remember { mutableStateOf<UiText?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  // Reacción automática si se encuentran hogares tras login o discovery
  LaunchedEffect(isCheckingDb, existingHousehold, allHouseholds) {
    if (!isCheckingDb) {
      if (existingHousehold != null) {
        step = SetupStep.SELECT_PROFILE
      } else if (allHouseholds.size == 1 && isLoggedIn) {
        // Solo auto-entramos si hay exactamente una casa Y estamos logueados.
        viewModel.switchHousehold(allHouseholds.first().id)
        step = SetupStep.SELECT_PROFILE
      } else if (allHouseholds.size > 1 && isLoggedIn) {
        step = SetupStep.SWITCH_HOUSEHOLD
      } else if (step == null) {
        step = SetupStep.WELCOME
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.navEvent.collect { newStep ->
      step = when (newStep) {
        HouseSetupViewModel.SetupStep.WELCOME -> SetupStep.WELCOME
        HouseSetupViewModel.SetupStep.CREATE -> SetupStep.CREATE
        HouseSetupViewModel.SetupStep.JOIN -> SetupStep.JOIN
        HouseSetupViewModel.SetupStep.SELECT_PROFILE -> SetupStep.SELECT_PROFILE
        HouseSetupViewModel.SetupStep.ADD_PROFILE -> SetupStep.ADD_PROFILE
        HouseSetupViewModel.SetupStep.SWITCH_HOUSEHOLD -> SetupStep.SWITCH_HOUSEHOLD
      }
    }
  }

  // Al loguearse (o volver de Auth logueado), intentamos completar acciones
  LaunchedEffect(isLoggedIn) {
    if (isLoggedIn) {
      val pendingStep = viewModel.getPendingStep()
      if (pendingStep != null) {
        step = when (pendingStep) {
          HouseSetupViewModel.STEP_CREATE -> SetupStep.CREATE
          HouseSetupViewModel.STEP_JOIN -> SetupStep.JOIN
          else -> step
        }
        viewModel.clearPendingStep()
      } else {
        viewModel.tryCompletePendingActions()
        // Si no había nada pendiente, forzamos recuperación silenciosa
        viewModel.silentRecoverHouseholds()
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.setupEvent.collect { result ->
      isLoading = false
      when (result) {
        is HouseSetupViewModel.SetupResult.Success -> {
          navController.navigate(Screen.Dashboard) {
            popUpTo(Screen.HouseSetup) { inclusive = true }
          }
        }
        is HouseSetupViewModel.SetupResult.Error -> {
          errorMessage = result.message
        }
      }
    }
  }

  BackHandler(enabled = step != null) {
    if (isLoading) return@BackHandler
    when (step) {
      SetupStep.CREATE, SetupStep.JOIN, SetupStep.ADD_PROFILE -> {
        step = when {
          existingHousehold != null -> SetupStep.SELECT_PROFILE
          allHouseholds.isNotEmpty() -> SetupStep.SWITCH_HOUSEHOLD
          else -> SetupStep.WELCOME
        }
      }
      SetupStep.SELECT_PROFILE, SetupStep.SWITCH_HOUSEHOLD -> {
        step = SetupStep.WELCOME
      }
      SetupStep.WELCOME -> {
        navController.popBackStack()
      }
      null -> {}
    }
  }

  AppCasaMeshBackground {
    if (step == null) {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    } else {
      Box(Modifier.fillMaxSize()) {
        HouseSetupContent(
          step = step!!,
          isLoading = isLoading,
          discoveredHouse = discoveredHouse,
          onStepChange = { step = it },
          existingHousehold = existingHousehold,
          allHouseholds = allHouseholds,
          householdMembers = householdMembers,
          onSearchHouse = viewModel::searchHousehold,
          onDiscoverHouse = viewModel::discoverAndJoin,
          onCreateClick = {
            if (viewModel.isUserLoggedIn()) {
              step = SetupStep.CREATE
            } else {
              viewModel.setPendingCreateData("", "", null)
              navController.navigate(Screen.Auth)
            }
          },
          onJoinClick = { step = SetupStep.JOIN },
          onRecoverClick = {
            val email = viewModel.getCurrentUserEmail()
            if (email != null) viewModel.recoverHouseholdsManual(email)
          },
          onCreateHousehold = { h, u, p ->
            isLoading = true
            viewModel.createHousehold(h, u, p)
          },
          onJoinHousehold = { c, u, p ->
            isLoading = true
            viewModel.joinHousehold(c, u, p)
          },
          onSelectMember = {
            isLoading = true
            viewModel.selectMember(it)
          },
          onSwitchHousehold = viewModel::switchHousehold,
          onLoginClick = {
            if (existingHousehold != null) step = SetupStep.SELECT_PROFILE
            else if (allHouseholds.isNotEmpty()) step = SetupStep.SWITCH_HOUSEHOLD
            else navController.navigate(Screen.Auth)
          },
          onResetAll = {
            viewModel.resetHousehold()
            step = SetupStep.WELCOME
          },
          isLoggedIn = isLoggedIn
        )

        if (isLoading || isCheckingDb) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surface.copy(alpha = Constants.UI.LOADING_OVERLAY_ALPHA)),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator()
          }
        }

        if (errorMessage != null) {
          AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text(stringResource(CoreR.string.common_oops)) },
            text = { Text(errorMessage!!.asString()) },
            confirmButton = {
              TextButton(onClick = { errorMessage = null }) { Text(stringResource(CoreR.string.common_ok)) }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun HouseSetupContent(
  step: SetupStep,
  isLoading: Boolean,
  discoveredHouse: Household?,
  onStepChange: (SetupStep) -> Unit,
  existingHousehold: Household?,
  allHouseholds: List<Household>,
  householdMembers: List<FamilyMember>,
  onSearchHouse: (String) -> Unit,
  onDiscoverHouse: (String) -> Unit,
  onCreateClick: () -> Unit,
  onJoinClick: () -> Unit,
  onRecoverClick: () -> Unit,
  onCreateHousehold: (String, String, String?) -> Unit,
  onJoinHousehold: (String, String, String?) -> Unit,
  onSelectMember: (FamilyMember) -> Unit,
  onSwitchHousehold: (Long) -> Unit,
  onLoginClick: () -> Unit,
  onResetAll: () -> Unit,
  isLoggedIn: Boolean
) {
  var inputName by remember { mutableStateOf("") }
  var inputUserName by remember { mutableStateOf("") }
  var inputCodeValue by remember { mutableStateOf(TextFieldValue("")) }
  var userPhotoUri by remember { mutableStateOf<String?>(null) }
  var showScanner by remember { mutableStateOf(false) }

  // Inicializar el prefijo una sola vez al entrar en JOIN
  var lastStep by remember { mutableStateOf<SetupStep?>(null) }
  LaunchedEffect(step) {
    if (step == SetupStep.JOIN && lastStep != SetupStep.JOIN) {
      inputCodeValue = TextFieldValue(Household.CODE_PREFIX, TextRange(Household.CODE_PREFIX_LENGTH))
    }
    lastStep = step
  }

  val context = LocalContext.current

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let {
      userPhotoUri = FileUtils.saveImageLocally(context, it.toString())
    }
  }

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
          onCreateClick = onCreateClick,
          onJoinClick = onJoinClick,
          onLoginClick = onLoginClick,
          onRecoverClick = onRecoverClick,
          isLoggedIn = isLoggedIn,
          householdName = existingHousehold?.nombre
        )
        SetupStep.CREATE -> CreateStep(
          name = inputName,
          userName = inputUserName,
          photoUri = userPhotoUri,
          isLoading = isLoading,
          onNameChange = { inputName = it },
          onUserNameChange = { inputUserName = it },
          onPhotoClick = { imagePickerLauncher.launch(Constants.Media.MIME_TYPE_IMAGE) },
          onBack = { onStepChange(SetupStep.WELCOME) },
          onConfirm = { onCreateHousehold(inputName, inputUserName, userPhotoUri) }
        )
        SetupStep.JOIN -> JoinStep(
          codeValue = inputCodeValue,
          isLoading = isLoading,
          discoveredHouse = discoveredHouse,
          onCodeChange = { newValue ->
            val input = newValue.text.uppercase()

            // Impedir borrar el prefijo "CASA-"
            if (input.length < Household.CODE_PREFIX_LENGTH && inputCodeValue.text.startsWith(Household.CODE_PREFIX)) {
              inputCodeValue = inputCodeValue.copy(selection = TextRange(Household.CODE_PREFIX_LENGTH))
              return@JoinStep
            }

            val suffix = if (input.startsWith(Household.CODE_PREFIX)) {
              input.substring(Household.CODE_PREFIX_LENGTH)
            } else {
              input
            }.replace(Regex("[^A-Z0-9]"), "").take(Household.CODE_SUFFIX_LENGTH)

            val newFullText = "${Household.CODE_PREFIX}$suffix"

            inputCodeValue = TextFieldValue(
              text = newFullText,
              selection = TextRange(newFullText.length)
            )
            if (suffix.length == Household.CODE_SUFFIX_LENGTH) {
              onSearchHouse(newFullText)
            }
          },
          onScanClick = { showScanner = true },
          onBack = { onStepChange(SetupStep.WELCOME) },
          onConfirm = { onDiscoverHouse(inputCodeValue.text) }
        )
        SetupStep.SELECT_PROFILE -> SelectProfileStep(
          existingHousehold = existingHousehold,
          members = householdMembers,
          isLoading = isLoading,
          onMemberClick = onSelectMember,
          onAddProfileClick = { onStepChange(SetupStep.ADD_PROFILE) },
          onSwitchHouseClick = if (allHouseholds.size > 1) { { onStepChange(SetupStep.SWITCH_HOUSEHOLD) } } else null,
          onResetAll = {
            onResetAll()
            onStepChange(SetupStep.WELCOME)
          }
        )
        SetupStep.SWITCH_HOUSEHOLD -> SwitchHouseholdStep(
          households = allHouseholds,
          onHouseholdClick = {
            onSwitchHousehold(it.id)
            onStepChange(SetupStep.SELECT_PROFILE)
          },
          onCreateNewClick = { onStepChange(SetupStep.CREATE) },
          onJoinNewClick = { onStepChange(SetupStep.JOIN) }
        )
        SetupStep.ADD_PROFILE -> AddProfileStep(
          userName = inputUserName,
          photoUri = userPhotoUri,
          isLoading = isLoading,
          onUserNameChange = { inputUserName = it },
          onPhotoClick = { imagePickerLauncher.launch(Constants.Media.MIME_TYPE_IMAGE) },
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
          val clean = it.replace("-", "").uppercase()
          val formatted = if (clean.length > Household.CODE_SUFFIX_LENGTH) {
            clean.substring(0, clean.length - Household.CODE_SUFFIX_LENGTH) + "-" + clean.substring(clean.length - Household.CODE_SUFFIX_LENGTH)
          } else {
            clean
          }
          inputCodeValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
          onSearchHouse(formatted)
          showScanner = false
        },
        onDismiss = { showScanner = false }
      )
    }
  }
}

@Composable
fun QRScannerDialog(onCodeScanned: (String) -> Unit, onDismiss: () -> Unit) {
  val context = LocalContext.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

  DisposableEffect(Unit) {
    onDispose {
      cameraExecutor.shutdown()
    }
  }

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
    title = { Text(stringResource(R.string.setup_qr_scan_title)) },
    text = {
      if (hasPermission) {
        Box(modifier = Modifier
          .size(280.dp)
          .clip(RoundedCornerShape(16.dp))) {
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

                imageAnalysis.setAnalyzer(cameraExecutor, object : ImageAnalysis.Analyzer {
                  @ExperimentalGetImage
                  override fun analyze(imageProxy: ImageProxy) {
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                      val visionImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                      scanner.process(visionImage)
                        .addOnSuccessListener { barcodes ->
                          for (barcode in barcodes) {
                            barcode.rawValue?.let { code ->
                              if (code.startsWith(Household.CODE_PREFIX)) {
                                onCodeScanned(code)
                              }
                            }
                          }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                    } else {
                      imageProxy.close()
                    }
                  }
                })

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
        Text(stringResource(R.string.setup_qr_permission_error))
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(CoreR.string.common_cancel)) } }
  )
}

@Composable
private fun WelcomeStep(
  onCreateClick: () -> Unit,
  onJoinClick: () -> Unit,
  onLoginClick: () -> Unit,
  onRecoverClick: () -> Unit,
  isLoggedIn: Boolean,
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
      if (householdName != null) stringResource(R.string.setup_household_detected, householdName) else stringResource(R.string.setup_welcome_desc),
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

    Spacer(Modifier.height(16.dp))
    Button(
      onClick = if (isLoggedIn && householdName == null) onRecoverClick else onLoginClick,
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary
      )
    ) {
      Icon(if (isLoggedIn && householdName == null) Icons.Default.CloudDownload else Icons.Default.Person, null)
      Spacer(Modifier.width(8.dp))
      Text(
        if (isLoggedIn && householdName == null) stringResource(R.string.setup_btn_recover_house)
        else stringResource(R.string.setup_btn_enter_my_house)
      )
    }
  }
}

@Composable
private fun JoinStep(
  codeValue: TextFieldValue,
  isLoading: Boolean,
  discoveredHouse: Household?,
  onCodeChange: (TextFieldValue) -> Unit,
  onScanClick: () -> Unit,
  onBack: () -> Unit,
  onConfirm: () -> Unit
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(stringResource(R.string.setup_join_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.setup_join_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
      textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold),
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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
          Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
          Spacer(Modifier.width(12.dp))
          Text(stringResource(R.string.setup_household_found_label, discoveredHouse.nombre), style = MaterialTheme.typography.labelLarge)
        }
      }
    }

    Spacer(Modifier.height(48.dp))

    Button(
      onClick = onConfirm,
      enabled = codeValue.text.length >= Household.CODE_TOTAL_LENGTH && !isLoading,
      modifier = Modifier.fillMaxWidth()
    ) {
      if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
      else Text(stringResource(R.string.setup_btn_continue))
    }
    TextButton(onClick = onBack, enabled = !isLoading) { Text(stringResource(CoreR.string.common_cancel)) }
  }
}

@Composable
private fun CreateStep(
  name: String,
  userName: String,
  photoUri: String?,
  isLoading: Boolean,
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
        .clickable(enabled = !isLoading) { onPhotoClick() },
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
          Text(stringResource(R.string.setup_profile_your_photo), style = MaterialTheme.typography.labelSmall)
        }
      }
    }

    Spacer(Modifier.height(24.dp))

    OutlinedTextField(
      value = name,
      onValueChange = onNameChange,
      label = { Text(stringResource(R.string.setup_label_house_name)) },
      modifier = Modifier.fillMaxWidth(),
      enabled = !isLoading,
      keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
    )

    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
      value = userName,
      onValueChange = onUserNameChange,
      label = { Text(stringResource(R.string.settings_user_name_title)) },
      modifier = Modifier.fillMaxWidth(),
      enabled = !isLoading,
      keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
    )

    Spacer(Modifier.height(32.dp))

    Button(
      onClick = onConfirm,
      enabled = name.isNotBlank() && userName.isNotBlank() && !isLoading,
      modifier = Modifier.fillMaxWidth()
    ) {
      if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
      else Text(stringResource(R.string.setup_btn_finish))
    }
    TextButton(onClick = onBack, enabled = !isLoading) { Text(stringResource(CoreR.string.common_cancel)) }
  }
}

@Composable
private fun SwitchHouseholdStep(
  households: List<Household>,
  onHouseholdClick: (Household) -> Unit,
  onCreateNewClick: () -> Unit,
  onJoinNewClick: () -> Unit
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
    Text(stringResource(R.string.setup_switch_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.setup_switch_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(Modifier.height(32.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
      items(households) { household ->
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onHouseholdClick(household) }) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Home, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
          }
          Spacer(Modifier.height(8.dp))
          Text(household.nombre, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(Modifier.height(48.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      OutlinedButton(onClick = onCreateNewClick) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.setup_btn_create_new))
      }
      OutlinedButton(onClick = onJoinNewClick) {
        Icon(Icons.Default.VpnKey, null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.setup_btn_join_another))
      }
    }
  }
}

@Composable
private fun SelectProfileStep(
  existingHousehold: Household?,
  members: List<FamilyMember>,
  isLoading: Boolean,
  onMemberClick: (FamilyMember) -> Unit,
  onAddProfileClick: () -> Unit,
  onSwitchHouseClick: (() -> Unit)? = null,
  onResetAll: () -> Unit
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(stringResource(R.string.setup_profile_who_are_you), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.setup_select_profile_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(Modifier.height(16.dp))

    // Info de hogar y botón de cambiar más visible
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

    Box(modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 120.dp), contentAlignment = Alignment.Center) {
      if (isLoading) {
        CircularProgressIndicator()
      } else {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
          val people = members.filter { it.tipo == TipoMiembro.PERSONA }
          items(people) { member ->
            ProfileAvatar(member) { if (!isLoading) onMemberClick(member) }
          }

          item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(enabled = !isLoading) { onAddProfileClick() }) {
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
              Text(stringResource(R.string.setup_btn_new_profile), style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    if (members.isEmpty() && !isLoading) {
      Text(
        stringResource(R.string.setup_no_members_error),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
      )
    }

    Spacer(Modifier.height(48.dp))

    var showConfirmReset by remember { mutableStateOf(false) }

    if (showConfirmReset) {
      AlertDialog(
        onDismissRequest = { showConfirmReset = false },
        title = { Text(stringResource(R.string.setup_reset_title)) },
        text = { Text(stringResource(R.string.setup_reset_desc)) },
        confirmButton = {
          Button(onClick = { onResetAll(); showConfirmReset = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text(stringResource(R.string.setup_btn_reset_confirm))
          }
        },
        dismissButton = {
          TextButton(onClick = { showConfirmReset = false }) { Text(stringResource(CoreR.string.common_cancel)) }
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
private fun AddProfileStep(
  userName: String,
  photoUri: String?,
  isLoading: Boolean,
  onUserNameChange: (String) -> Unit,
  onPhotoClick: () -> Unit,
  onBack: () -> Unit,
  onConfirm: () -> Unit
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(stringResource(R.string.setup_profile_who_are_you), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(stringResource(R.string.setup_profile_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(24.dp))

    Box(
      modifier = Modifier
        .size(100.dp)
        .clip(CircleShape)
        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        .clickable(enabled = !isLoading) { onPhotoClick() },
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
          Text(stringResource(R.string.setup_profile_your_photo), style = MaterialTheme.typography.labelSmall)
        }
      }
    }

    Spacer(Modifier.height(24.dp))

    OutlinedTextField(
      value = userName,
      onValueChange = onUserNameChange,
      label = { Text(stringResource(R.string.settings_user_name_title)) },
      modifier = Modifier.fillMaxWidth(),
      enabled = !isLoading,
      keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
    )

    Spacer(Modifier.height(32.dp))

    Button(
      onClick = onConfirm,
      enabled = userName.isNotBlank() && !isLoading,
      modifier = Modifier.fillMaxWidth()
    ) {
      if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
      else Text(stringResource(R.string.setup_btn_enter))
    }
    TextButton(onClick = onBack, enabled = !isLoading) { Text(stringResource(CoreR.string.common_cancel)) }
  }
}

@Composable
private fun ProfileAvatar(member: FamilyMember, onClick: () -> Unit) {
  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
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
        Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
      }
    }
    Spacer(Modifier.height(8.dp))
    Text(member.nombre, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
  }
}

private enum class SetupStep {
  WELCOME, CREATE, JOIN, SELECT_PROFILE, ADD_PROFILE, SWITCH_HOUSEHOLD
}

@Preview(showBackground = true)
@Composable
fun HouseSetupPreview_Welcome() {
  AppCasaTheme {
    HouseSetupContent(
      step = SetupStep.WELCOME,
      isLoading = false,
      discoveredHouse = null,
      onStepChange = {},
      existingHousehold = null,
      allHouseholds = emptyList(),
      householdMembers = emptyList(),
      onSearchHouse = {},
      onDiscoverHouse = {},
      onCreateClick = {},
      onJoinClick = {},
      onRecoverClick = {},
      onCreateHousehold = { _, _, _ -> },
      onJoinHousehold = { _, _, _ -> },
      onSelectMember = {},
      onSwitchHousehold = {},
      onLoginClick = {},
      onResetAll = {},
      isLoggedIn = false
    )
  }
}

@Preview(showBackground = true)
@Composable
fun HouseSetupPreview_SelectProfile() {
  AppCasaTheme {
    HouseSetupContent(
      step = SetupStep.SELECT_PROFILE,
      isLoading = false,
      discoveredHouse = null,
      onStepChange = {},
      existingHousehold = Household(id = 1, nombre = "Mi Casa", codigoHogar = "CASA-1234"),
      allHouseholds = listOf(Household(id = 1, nombre = "Mi Casa")),
      householdMembers = listOf(
        FamilyMember(id = 1, hogarId = 1, nombre = "Juan", tipo = TipoMiembro.PERSONA),
        FamilyMember(id = 2, hogarId = 1, nombre = "Maria", tipo = TipoMiembro.PERSONA)
      ),
      onSearchHouse = {},
      onDiscoverHouse = {},
      onCreateClick = {},
      onJoinClick = {},
      onRecoverClick = {},
      onCreateHousehold = { _, _, _ -> },
      onJoinHousehold = { _, _, _ -> },
      onSelectMember = {},
      onSwitchHousehold = {},
      onLoginClick = {},
      onResetAll = {},
      isLoggedIn = true
    )
  }
}
