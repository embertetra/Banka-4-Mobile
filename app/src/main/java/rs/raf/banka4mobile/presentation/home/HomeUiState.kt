package rs.raf.banka4mobile.presentation.home

interface HomeContract {

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()
        data object OpenVerificationClicked : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateToVerification : SideEffect()
    }

}