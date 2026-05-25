package rs.raf.banka4mobile.presentation.login

interface LoginContract {
    data class LoginUiState(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val showBiometricLogin: Boolean = false
    )

    sealed class LoginUiEvent {
        data object NavigateToHome : LoginUiEvent()
        data class ShowMessage(val message: String) : LoginUiEvent()
        data object ShowBiometricPrompt : LoginUiEvent()
    }
}