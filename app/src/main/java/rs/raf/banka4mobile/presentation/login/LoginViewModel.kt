package rs.raf.banka4mobile.presentation.login

import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.domain.repository.AuthRepository
import rs.raf.banka4mobile.presentation.login.LoginContract.LoginUiState
import rs.raf.banka4mobile.presentation.login.LoginContract.LoginUiEvent
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val LOG_TAG = "LoginViewModel"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<LoginUiEvent>(capacity = Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        onScreenOpened()
    }

    fun onScreenOpened() {
        viewModelScope.launch {
            val savedEmail = authRepository.getLastLoginEmail().orEmpty()
            _uiState.update {
                it.copy(
                    email = savedEmail,
                    password = "",
                    isLoading = false
                )
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(email = email)
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(password = password)
        }
    }

    fun login() {
        val email = uiState.value.email.trim()
        val password = uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            emitEffect(LoginUiEvent.ShowMessage("Unesite email i lozinku."))
            return
        }

        viewModelScope.launch {
            authRepository.saveLastLoginEmail(email)

            _uiState.update {
                it.copy(isLoading = true)
            }

            authRepository.login(email, password)
                .onSuccess { session ->
                    authRepository.saveSession(session)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            password = ""
                        )
                    }
                    emitEffect(LoginUiEvent.NavigateToHome)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    emitEffect(LoginUiEvent.ShowMessage(error.message ?: "Greška pri prijavi."))
                }
        }
    }

    fun onBiometricAvailabilityChecked(status: Int) {
        val canUseBiometric = status == BiometricManager.BIOMETRIC_SUCCESS
        val infoMessage = biometricAvailabilityMessage(status)

        if (canUseBiometric) {
            Timber.tag(LOG_TAG).i("Biometrija je dostupna za brzu prijavu.")
        } else {
            Timber.tag(LOG_TAG).w(
                "Biometrija nije dostupna: status=$status, razlog=${infoMessage ?: "nepoznat"}"
            )
        }

        _uiState.update {
            it.copy(
                showBiometricLogin = canUseBiometric,
                biometricAvailabilityStatus = status
            )
        }
    }

    fun onBiometricLoginClick() {
        val state = _uiState.value
        if (!state.showBiometricLogin) {
            val status = state.biometricAvailabilityStatus
            val message = biometricAvailabilityMessage(status)
                ?: "Biometrija nije dostupna na ovom uređaju."

            Timber.tag(LOG_TAG).w(
                "Klik na biometrijsku prijavu, ali biometrija nije dostupna. status=${status ?: "nepoznat"}"
            )
            emitEffect(LoginUiEvent.ShowMessage(message))
            return
        }

        emitEffect(LoginUiEvent.ShowBiometricPrompt)
    }

    fun onBiometricAuthenticated() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            val quickSession = authRepository.getQuickLoginSession()
            if (quickSession != null) {
                authRepository.saveSession(quickSession)
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
                emitEffect(LoginUiEvent.NavigateToHome)
            } else {
                _uiState.update {
                    it.copy(isLoading = false)
                }
                emitEffect(LoginUiEvent.ShowMessage("Nema sačuvanih podataka za brzu prijavu. Prijavite se email-om i lozinkom."))
            }
        }
    }

    fun onBiometricAuthError(message: String?) {
        val normalizedMessage = message?.takeIf { it.isNotBlank() }
        Timber.tag(LOG_TAG).w(
            "Biometrijska prijava neuspešna: ${normalizedMessage ?: "Nema dodatne poruke"}"
        )
        emitEffect(
            LoginUiEvent.ShowMessage(
                normalizedMessage ?: "Biometrijska prijava nije uspela."
            )
        )
    }

    private fun emitEffect(effect: LoginUiEvent) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    private fun biometricAvailabilityMessage(status: Int?): String? {
        return when (status) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                "Biometrija nije podešena na telefonu."
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                "Uredjaj ne podržava biometrijsku prijavu."
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                "Biometrijski senzor je trenutno nedostupan."
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                "Potrebno je bezbednosno ažuriranje uredjaja za biometriju."
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                "Ovaj tip biometrijske prijave nije podržan na uredjaju."
            }

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                "Nije moguce proveriti biometriju trenutno."
            }

            else -> null
        }
    }
}