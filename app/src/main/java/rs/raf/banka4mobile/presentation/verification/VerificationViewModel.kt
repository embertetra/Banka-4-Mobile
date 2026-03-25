package rs.raf.banka4mobile.presentation.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import javax.inject.Inject
import rs.raf.banka4mobile.domain.repository.AuthRepository
import rs.raf.banka4mobile.feature.verification.TOTPGenerator
import rs.raf.banka4mobile.presentation.verification.VerificationContract.SideEffect
import rs.raf.banka4mobile.presentation.verification.VerificationContract.UiState

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    private val codeValiditySeconds = 30L
    private var totpGenerator: TOTPGenerator? = null
    private var lastTimeStep: Long? = null
    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            loadSecretAndStartTicker()
        }
    }

    private suspend fun loadSecretAndStartTicker() {
        setState { copy(isLoading = true, error = null) }

        authRepository.getSecretMobile()
            .onSuccess { secret ->
                runCatching {
                    TOTPGenerator(
                        secretBase32 = secret,
                        timeStepSeconds = codeValiditySeconds
                    )
                }.onSuccess { generator ->
                    totpGenerator = generator
                    setState { copy(isLoading = false, error = null) }
                    startTicker()
                }.onFailure { error ->
                    setState {
                        copy(
                            isLoading = false,
                            error = error,
                            totp = ""
                        )
                    }
                }
            }
            .onFailure { error ->
                setState {
                    copy(
                        isLoading = false,
                        error = error,
                        totp = ""
                    )
                }
            }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val epochSeconds = System.currentTimeMillis() / 1000
                val currentTimeStep = epochSeconds / codeValiditySeconds
                val secondsLeft = (codeValiditySeconds - (epochSeconds % codeValiditySeconds)).toInt()

                if (lastTimeStep == null || lastTimeStep != currentTimeStep) {
                    val isRefresh = lastTimeStep != null
                    lastTimeStep = currentTimeStep
                    generateNewCode()
                    if (isRefresh) {
                        _sideEffects.emit(SideEffect.ShowToast("Kod je ponovo kreiran"))
                    }
                }

                setState { copy(secondsLeft = secondsLeft) }
                delay(1000)
            }
        }
    }

    private fun generateNewCode() {
        val generator = totpGenerator ?: return
        val totp = generator.generate()
        setState { copy(totp = totp) }
    }
}