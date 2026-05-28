package rs.raf.banka4mobile.presentation.login

import android.os.Build
import android.os.Build.VERSION_CODES.R
import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rs.raf.banka4mobile.presentation.login.LoginContract.LoginUiEvent
import kotlinx.coroutines.flow.collectLatest

private tailrec fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}

private fun biometricAuthenticators(): Int {
    return if (Build.VERSION.SDK_INT >= R) {
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    } else {
        BiometricManager.Authenticators.BIOMETRIC_WEAK
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val fieldShape = RoundedCornerShape(10.dp)
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    val isKeyboardOpen = imeBottom > 0
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val currentActivity by rememberUpdatedState(activity)
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)
    val authenticators = biometricAuthenticators()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.onScreenOpened()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is LoginUiEvent.NavigateToHome -> currentOnLoginSuccess()
                is LoginUiEvent.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is LoginUiEvent.ShowBiometricPrompt -> {
                    val safeActivity = currentActivity
                    if (safeActivity == null) {
                        viewModel.onBiometricAuthError("Biometrija nije dostupna u ovom kontekstu.")
                    } else {
                        val executor = ContextCompat.getMainExecutor(safeActivity)
                        val biometricPrompt = BiometricPrompt(
                            safeActivity,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    viewModel.onBiometricAuthenticated()
                                }

                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence
                                ) {
                                    super.onAuthenticationError(errorCode, errString)
                                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                        errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                        errorCode != BiometricPrompt.ERROR_CANCELED
                                    ) {
                                        viewModel.onBiometricAuthError(errString.toString())
                                    }
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                    viewModel.onBiometricAuthError("Biometrijska autentikacija nije uspela. Pokušajte ponovo.")
                                }
                            }
                        )

                        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Brza prijava")
                            .setSubtitle("Potvrdite identitet za ulazak u aplikaciju")

                        if (Build.VERSION.SDK_INT >= R) {
                            promptInfoBuilder.setAllowedAuthenticators(authenticators)
                        } else {
                            promptInfoBuilder
                                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                                .setNegativeButtonText("Odustani")
                        }

                        biometricPrompt.authenticate(promptInfoBuilder.build())
                    }
                }
            }
        }
    }

    LaunchedEffect(activity) {
        if (activity == null) {
            viewModel.onBiometricAvailabilityChecked(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
            return@LaunchedEffect
        }

        val status = BiometricManager.from(activity).canAuthenticate(authenticators)
        viewModel.onBiometricAvailabilityChecked(status)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .imePadding()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = if (isKeyboardOpen) Arrangement.Bottom else Arrangement.Center
            ) {
                Text(
                    text = "Banka 4",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "prijavite se na svoj nalog",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(if (isKeyboardOpen) 20.dp else 30.dp))

                TextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, fieldShape),
                    label = { Text("Email") },
                    singleLine = true,
                    shape = fieldShape,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, fieldShape),
                    label = { Text("Lozinka") },
                    singleLine = true,
                    shape = fieldShape,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                    ),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (passwordVisible) "Sakrij lozinku" else "Prikaži lozinku"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::login,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    enabled = !uiState.isLoading,
                    shape = fieldShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                ) {
                    Text(
                        text = "Prijavi se",
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        letterSpacing = 1.25.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "ili",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = viewModel::onBiometricLoginClick,
                        enabled = uiState.showBiometricLogin && !uiState.isLoading,
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(2.dp, CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = "Prijava otiskom",
                            tint = if (uiState.showBiometricLogin) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                }

            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(vertical = 8.dp),
            snackbar = { snackbarData ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(14.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = snackbarData.visuals.message,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        )
    }
}
