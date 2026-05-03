package rs.raf.banka4mobile.presentation.transfers

interface TransferContract {

    data class UiState(
        val isLoading: Boolean = false,
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null,

        val accounts: List<AccountItem> = emptyList(),
        val selectedFromAccountIndex: Int = 0,

        val toAccountInput: String = "",
        val amountInput: String = "",

        val transfers: List<TransferItem> = emptyList()
    ) {
        val selectedFromAccount: AccountItem?
            get() = accounts.getOrNull(selectedFromAccountIndex)
    }

    data class AccountItem(
        val accountNumber: String,
        val name: String,
        val balance: Double,
        val currency: String
    )

    data class TransferItem(
        val id: String,
        val fromAccount: String,
        val toAccount: String,
        val initialAmount: Double,
        val finalAmount: Double,
        val commission: Double,
        val createdAt: String
    )

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()
        data object PreviousAccountClicked : UiEvent()
        data object NextAccountClicked : UiEvent()
        data class ToAccountChanged(val value: String) : UiEvent()
        data class AmountChanged(val value: String) : UiEvent()
        data object SubmitTransferClicked : UiEvent()
        data object ClearMessages : UiEvent()
    }
}