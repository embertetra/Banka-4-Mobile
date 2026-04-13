package rs.raf.banka4mobile.presentation.home

interface HomeContract {

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val accounts: List<AccountItem> = emptyList(),
        val selectedAccountIndex: Int = 0,
        val transactions: List<TransactionItem> = emptyList(),
        val isInfoDialogVisible: Boolean = false,
        val accountDetails: AccountDetailsItem? = null
    ) {
        val selectedAccount: AccountItem?
            get() = accounts.getOrNull(selectedAccountIndex)
    }

    data class AccountItem(
        val id: String,
        val name: String,
        val accountType: String,
        val accountKind: String,
        val accountNumber: String,
        val balance: Double,
        val availableBalance: Double,
        val reservedFunds: Double,
        val currency: String
    )

    data class AccountDetailsItem(
        val dailyLimit: Double,
        val monthlyLimit: Double,
        val dailySpending: Double,
        val monthlySpending: Double,
        val reservedFunds: Double,
        val accountType: String,
        val accountKind: String
    )

    enum class TransactionType {
        SENT,
        RECEIVED
    }

    data class TransactionItem(
        val id: String,
        val name: String,
        val amount: Double,
        val currency: String,
        val type: TransactionType
    )

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()
        data object PreviousAccountClicked : UiEvent()
        data object NextAccountClicked : UiEvent()
        data object OpenCardsClicked : UiEvent()
        data object CreditInstallmentClicked : UiEvent()
        data object OpenInfoClicked : UiEvent()
        data object DismissInfoClicked : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToCards(val accountNumber: String) : SideEffect()
        data class ShowToast(val message: String) : SideEffect()
    }

}