package com.appcasa.features.settings.presentation.screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.appcasa.core.ui.components.AppCasaMeshBackground
import com.appcasa.core.ui.components.GoogleIcon
import com.appcasa.feature.settings.R
import com.appcasa.features.settings.presentation.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token != null) {
                viewModel.onGoogleSignInResult(token)
            } else {
                Toast.makeText(context, context.getString(R.string.auth_error_google_no_token), Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.auth_error_google_api, e.statusCode), Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.authEvent.collect { event ->
            when (event) {
                is AuthViewModel.AuthEvent.Success -> {
                    navController.popBackStack()
                }
                is AuthViewModel.AuthEvent.Message -> {
                    Toast.makeText(context, event.text.asString(context), Toast.LENGTH_LONG).show()
                    if (authMode == AuthMode.FORGOT_PASSWORD) {
                        authMode = AuthMode.LOGIN
                    }
                }
            }
        }
    }

    AppCasaMeshBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (authMode) {
                    AuthMode.REGISTER -> stringResource(R.string.auth_create_account)
                    AuthMode.FORGOT_PASSWORD -> stringResource(R.string.auth_forgot_password_title)
                    AuthMode.LOGIN -> stringResource(R.string.auth_welcome)
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (authMode) {
                    AuthMode.REGISTER -> stringResource(R.string.auth_subtitle_register)
                    AuthMode.FORGOT_PASSWORD -> stringResource(R.string.auth_forgot_password_subtitle)
                    AuthMode.LOGIN -> stringResource(R.string.auth_subtitle_login)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.auth_label_email)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )

            if (authMode != AuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.auth_label_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (uiState is AuthViewModel.AuthUiState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as AuthViewModel.AuthUiState.Error).message.asString(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState is AuthViewModel.AuthUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        when (authMode) {
                            AuthMode.REGISTER -> viewModel.register(email, password)
                            AuthMode.LOGIN -> viewModel.login(email, password)
                            AuthMode.FORGOT_PASSWORD -> viewModel.sendResetPassword(email)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (authMode) {
                            AuthMode.REGISTER -> stringResource(R.string.auth_btn_register)
                            AuthMode.LOGIN -> stringResource(R.string.auth_btn_login)
                            AuthMode.FORGOT_PASSWORD -> stringResource(R.string.auth_btn_send_link)
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (authMode != AuthMode.FORGOT_PASSWORD) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign In Button Decorado
                    OutlinedButton(
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleIcon()
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.auth_btn_google),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { 
                        authMode = if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN 
                    }) {
                        Text(if (authMode == AuthMode.REGISTER) stringResource(R.string.auth_btn_goto_login) else stringResource(R.string.auth_btn_goto_register))
                    }
                    
                    if (authMode != AuthMode.FORGOT_PASSWORD) {
                        TextButton(onClick = { authMode = AuthMode.FORGOT_PASSWORD }) {
                            Text(stringResource(R.string.auth_btn_forgot_password), color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        TextButton(onClick = { authMode = AuthMode.LOGIN }) {
                            Text(stringResource(R.string.auth_btn_back_to_login), color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

private enum class AuthMode { LOGIN, REGISTER, FORGOT_PASSWORD }

