package com.appcasa.features.settings.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.data.utils.FileUtils
import com.appcasa.core.domain.model.Household
import com.appcasa.core.ui.components.AppCasaCard
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.utils.Constants
import com.appcasa.feature.settings.R
import com.appcasa.core.ui.R as CoreR
import com.appcasa.features.settings.presentation.screen.components.*
import com.appcasa.features.settings.presentation.viewmodel.HouseSetupViewModel
import com.appcasa.features.settings.presentation.viewmodel.SetupIntent
import com.appcasa.features.settings.presentation.viewmodel.SetupUiEffect
import com.appcasa.navigation.Screen
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.appcasa.core.ui.utils.UiText
import java.util.concurrent.Executors
import androidx.camera.core.Preview as CameraPreview

@Composable
fun HouseSetupScreen(
  navController: NavController,
  initialCode: String? = null,
  viewModel: HouseSetupViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current

  var step by remember {
    mutableStateOf(
      if (viewModel.isOnboardingCompleted()) SetupStep.WELCOME else SetupStep.ONBOARDING
    )
  }
  var errorMessage by remember { mutableStateOf<UiText?>(null) }

  var inputName by remember { mutableStateOf("") }
  var inputUserName by remember { mutableStateOf("") }
  var inputCodeValue by remember { mutableStateOf(TextFieldValue("")) }
  var userPhotoUri by remember { mutableStateOf<String?>(null) }
  var showScanner by remember { mutableStateOf(false) }

  // Reacción al deep link inicial
  LaunchedEffect(initialCode) {
    if (!initialCode.isNullOrEmpty()) {
      val formatted = if (initialCode.startsWith(Household.CODE_PREFIX)) {
        initialCode
      } else {
        "${Household.CODE_PREFIX}$initialCode"
      }
      inputCodeValue = TextFieldValue(formatted, TextRange(formatted.length))
      viewModel.handleIntent(SetupIntent.SearchHousehold(formatted))
      step = SetupStep.JOIN
    }
  }

  // Inicializar el prefijo una sola vez al entrar en JOIN manualmente
  var lastStep by remember { mutableStateOf<SetupStep?>(null) }
  LaunchedEffect(step) {
    if (step == SetupStep.JOIN && lastStep != SetupStep.JOIN && initialCode == null) {
      inputCodeValue = TextFieldValue(Household.CODE_PREFIX, TextRange(Household.CODE_PREFIX_LENGTH))
    }
    lastStep = step
  }

  // Navegación/decisión inicial automática tras comprobar DB (si no estamos en onboarding)
  LaunchedEffect(uiState.isCheckingDb, uiState.existingHousehold, uiState.allHouseholds, uiState.isLoggedIn) {
    if (step == SetupStep.ONBOARDING) return@LaunchedEffect
    
    if (!uiState.isCheckingDb) {
      if (!uiState.isLoggedIn) {
        step = SetupStep.WELCOME
      } else if (uiState.existingHousehold != null) {
        // Si ya hay un hogar activo, vamos a elegir perfil
        step = SetupStep.SELECT_PROFILE
      } else if (uiState.allHouseholds.isNotEmpty()) {
        if (uiState.allHouseholds.size == 1) {
          viewModel.handleIntent(SetupIntent.SwitchHousehold(uiState.allHouseholds.first().id))
          step = SetupStep.SELECT_PROFILE
        } else {
          step = SetupStep.SWITCH_HOUSEHOLD
        }
      } else {
        step = SetupStep.WELCOME
      }
    }
  }

  // Manejo de Intents pendientes tras iniciar sesión
  LaunchedEffect(uiState.isLoggedIn) {
    if (uiState.isLoggedIn) {
      val pendingStep = viewModel.getPendingStep()
      if (pendingStep != null) {
        step = when (pendingStep) {
          HouseSetupViewModel.STEP_CREATE -> SetupStep.CREATE
          HouseSetupViewModel.STEP_JOIN -> SetupStep.JOIN
          else -> step
        }
        viewModel.handleIntent(SetupIntent.ClearPendingStep)
      } else {
        viewModel.handleIntent(SetupIntent.TryCompletePendingActions)
        viewModel.handleIntent(SetupIntent.SilentRecoverHouseholds)
      }
    }
  }

  // Suscribirse a los efectos del ViewModel
  LaunchedEffect(Unit) {
    viewModel.uiEffect.collect { effect ->
      when (effect) {
        is SetupUiEffect.NavigateToDashboard -> {
          navController.navigate(Screen.Dashboard) {
            popUpTo(Screen.HouseSetup()) { inclusive = true }
          }
        }
        is SetupUiEffect.NavigateToStep -> {
          step = when (effect.step) {
            HouseSetupViewModel.SetupStep.ONBOARDING -> SetupStep.ONBOARDING
            HouseSetupViewModel.SetupStep.WELCOME -> SetupStep.WELCOME
            HouseSetupViewModel.SetupStep.CREATE -> SetupStep.CREATE
            HouseSetupViewModel.SetupStep.JOIN -> SetupStep.JOIN
            HouseSetupViewModel.SetupStep.SELECT_PROFILE -> SetupStep.SELECT_PROFILE
            HouseSetupViewModel.SetupStep.ADD_PROFILE -> SetupStep.ADD_PROFILE
            HouseSetupViewModel.SetupStep.SWITCH_HOUSEHOLD -> SetupStep.SWITCH_HOUSEHOLD
            HouseSetupViewModel.SetupStep.BIOMETRIC_PROMPT -> SetupStep.BIOMETRIC_PROMPT
          }
        }
        is SetupUiEffect.ShowError -> {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
          errorMessage = effect.message
        }
      }
    }
  }

  // BackHandler para navegación natural de Compose
  BackHandler(enabled = step != SetupStep.ONBOARDING) {
    if (uiState.isLoading) return@BackHandler
    when (step) {
      SetupStep.CREATE, SetupStep.JOIN, SetupStep.ADD_PROFILE -> {
        step = when {
          !uiState.isLoggedIn -> SetupStep.WELCOME
          uiState.existingHousehold != null -> SetupStep.SELECT_PROFILE
          uiState.allHouseholds.isNotEmpty() -> SetupStep.SWITCH_HOUSEHOLD
          else -> SetupStep.WELCOME
        }
      }
      SetupStep.SELECT_PROFILE, SetupStep.SWITCH_HOUSEHOLD -> {
        step = SetupStep.WELCOME
      }
      SetupStep.WELCOME -> {
        navController.popBackStack()
      }
      SetupStep.BIOMETRIC_PROMPT -> {
        viewModel.handleIntent(SetupIntent.SetupBiometrics(false))
      }
      else -> {}
    }
  }

  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    uri?.let {
      userPhotoUri = FileUtils.saveImageLocally(context, it.toString())
    }
  }

  AppCasaMeshBackground {
    Box(Modifier.fillMaxSize()) {
      AnimatedContent(
        targetState = step,
        transitionSpec = {
          (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn()).togetherWith(
            slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut()
          )
        },
        label = "setup_steps"
      ) { targetStep ->
        when (targetStep) {
          SetupStep.ONBOARDING -> {
            OnboardingPager(
              onFinished = {
                viewModel.handleIntent(SetupIntent.SaveOnboardingCompleted)
              }
            )
          }
          SetupStep.WELCOME -> {
            WelcomeStep(
              onCreateClick = {
                if (viewModel.isUserLoggedIn()) {
                  step = SetupStep.CREATE
                } else {
                  viewModel.handleIntent(SetupIntent.SetPendingCreate("", "", null))
                  navController.navigate(Screen.Auth)
                }
              },
              onJoinClick = { step = SetupStep.JOIN },
              onRestoreClick = {
                val email = viewModel.getCurrentUserEmail()
                if (email != null) {
                  viewModel.handleIntent(SetupIntent.RecoverHouseholdsManual(email))
                } else {
                  navController.navigate(Screen.Auth)
                }
              },
              isLoggedIn = uiState.isLoggedIn,
              householdName = uiState.existingHousehold?.nombre
            )
          }
          SetupStep.CREATE -> {
            CreateStep(
              name = inputName,
              userName = inputUserName,
              photoUri = userPhotoUri,
              isLoading = uiState.isLoading,
              onNameChange = { inputName = it },
              onUserNameChange = { inputUserName = it },
              onPhotoClick = { imagePickerLauncher.launch(Constants.Media.MIME_TYPE_IMAGE) },
              onBack = { step = SetupStep.WELCOME },
              onConfirm = {
                viewModel.handleIntent(SetupIntent.CreateHousehold(inputName, inputUserName, userPhotoUri))
              }
            )
          }
          SetupStep.JOIN -> {
            JoinStep(
              codeValue = inputCodeValue,
              isLoading = uiState.isLoading,
              discoveredHouse = uiState.discoveredHousehold,
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
                  viewModel.handleIntent(SetupIntent.SearchHousehold(newFullText))
                }
              },
              onScanClick = { showScanner = true },
              onBack = { step = SetupStep.WELCOME },
              onConfirm = {
                viewModel.handleIntent(SetupIntent.DiscoverAndJoin(inputCodeValue.text))
              }
            )
          }
          SetupStep.SELECT_PROFILE -> {
            SelectProfileStep(
              existingHousehold = uiState.existingHousehold,
              members = uiState.householdMembers,
              isLoading = uiState.isLoading,
              onMemberClick = { member ->
                viewModel.handleIntent(SetupIntent.SelectMember(member))
              },
              onAddProfileClick = { step = SetupStep.ADD_PROFILE },
              onSwitchHouseClick = if (uiState.allHouseholds.size > 1) {
                { step = SetupStep.SWITCH_HOUSEHOLD }
              } else null,
              onLogout = {
                viewModel.handleIntent(SetupIntent.Logout)
                step = SetupStep.WELCOME
              }
            )
          }
          SetupStep.SWITCH_HOUSEHOLD -> {
            SwitchHouseholdStep(
              households = uiState.allHouseholds,
              onHouseholdClick = { household ->
                viewModel.handleIntent(SetupIntent.SwitchHousehold(household.id))
                step = SetupStep.SELECT_PROFILE
              },
              onCreateNewClick = { step = SetupStep.CREATE },
              onJoinNewClick = { step = SetupStep.JOIN }
            )
          }
          SetupStep.ADD_PROFILE -> {
            AddProfileStep(
              userName = inputUserName,
              photoUri = userPhotoUri,
              isLoading = uiState.isLoading,
              onUserNameChange = { inputUserName = it },
              onPhotoClick = { imagePickerLauncher.launch(Constants.Media.MIME_TYPE_IMAGE) },
              onBack = { step = SetupStep.SELECT_PROFILE },
              onConfirm = { tipo, raza, birthDate ->
                uiState.existingHousehold?.let {
                  viewModel.handleIntent(SetupIntent.JoinHousehold(it.codigoHogar ?: "", inputUserName, userPhotoUri, tipo, raza, birthDate))
                }
              }
            )
          }
          SetupStep.BIOMETRIC_PROMPT -> {
            BiometricPromptStep(
              onEnable = { enable ->
                viewModel.handleIntent(SetupIntent.SetupBiometrics(enable))
              }
            )
          }
        }
      }

      AnimatedVisibility(
        visible = uiState.isLoading || uiState.isCheckingDb,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
          contentAlignment = Alignment.Center
        ) {
          AppCasaCard(
            useGlassmorphism = true,
            modifier = Modifier.padding(32.dp)
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
              Spacer(Modifier.height(24.dp))
              val loadMsg = uiState.loadingMessage?.asString() ?: stringResource(R.string.setup_loading_checking_db)
              Text(
                text = loadMsg,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      if (errorMessage != null) {
        AlertDialog(
          onDismissRequest = { errorMessage = null },
          title = { Text(stringResource(CoreR.string.common_oops)) },
          text = { Text(errorMessage!!.asString()) },
          confirmButton = {
            TextButton(onClick = { errorMessage = null }) {
              Text(stringResource(CoreR.string.common_ok))
            }
          }
        )
      }

      if (showScanner) {
        QRScannerDialog(
          onCodeScanned = { barcode ->
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            val clean = barcode.replace("-", "").uppercase()
            val formatted = if (clean.length > Household.CODE_SUFFIX_LENGTH) {
              clean.substring(0, clean.length - Household.CODE_SUFFIX_LENGTH) + "-" + clean.substring(clean.length - Household.CODE_SUFFIX_LENGTH)
            } else {
              clean
            }
            inputCodeValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
            viewModel.handleIntent(SetupIntent.SearchHousehold(formatted))
            showScanner = false
          },
          onDismiss = { showScanner = false }
        )
      }
    }
  }
}

private enum class SetupStep {
  ONBOARDING, WELCOME, CREATE, JOIN, SELECT_PROFILE, ADD_PROFILE, SWITCH_HOUSEHOLD, BIOMETRIC_PROMPT
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

