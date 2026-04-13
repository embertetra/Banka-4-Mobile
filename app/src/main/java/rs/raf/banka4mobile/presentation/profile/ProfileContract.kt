package rs.raf.banka4mobile.presentation.profile

data class ProfileUiModel(
    val fullName: String = "",
    val email: String = "",
    val username: String = "",
    val identityType: String = ""
)

interface ProfileContract {

    data class UiState(
        val profile: ProfileUiModel? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()
        data object LogoutClicked : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateToLogin : SideEffect()
    }
}