package rs.raf.banka4mobile.presentation.profile

import rs.raf.banka4mobile.data.local.settings.AppThemeOption

data class ProfileUiModel(
    val fullName: String = "",
    val email: String = "",
    val username: String = "",
    val identityType: String = ""
)

data class MonthlyBalanceItem(
    val monthLabel: String,
    val income: Double,
    val outcome: Double
) {
    val balanceChange: Double
        get() = income - outcome
}

data class DailySpendingItem(
    val day: Int,
    val amount: Double
)

interface ProfileContract {

    data class UiState(
        val profile: ProfileUiModel? = null,
        val monthlyBalance: List<MonthlyBalanceItem> = emptyList(),
        val dailySpending: List<DailySpendingItem> = emptyList(),
        val selectedTheme: AppThemeOption = AppThemeOption.SYSTEM,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()
        data object LogoutClicked : UiEvent()
        data class ThemeChanged(val theme: AppThemeOption) : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateToLogin : SideEffect()
    }
}