package rs.raf.banka4mobile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.UiState())
    val state = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<ProfileContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    fun onEvent(event: ProfileContract.UiEvent) {
        when (event) {
            ProfileContract.UiEvent.ScreenOpened -> loadProfile()
            ProfileContract.UiEvent.LogoutClicked -> logout()
        }
    }

    private fun loadProfile() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val session = authRepository.getSession()

                if (session == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Nema aktivne sesije."
                        )
                    }
                    return@launch
                }

                val user = session.user

                _state.update {
                    it.copy(
                        isLoading = false,
                        profile = ProfileUiModel(
                            fullName = "${user.firstName} ${user.lastName}",
                            email = user.email,
                            username = user.username,
                            identityType = user.identityType
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Greška pri učitavanju profila."
                    )
                }
            }
        }
    }

    private fun logout() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                authRepository.logout()
                _state.update { it.copy(isLoading = false) }
                _sideEffects.emit(ProfileContract.SideEffect.NavigateToLogin)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Greška pri odjavi."
                    )
                }
            }
        }
    }
}