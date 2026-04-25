package rs.raf.banka4mobile.presentation.cards

interface CardContract {

	data class UiState(
		val isLoading: Boolean = false,
		val isCardsLoading: Boolean = false,
		val errorMessage: String? = null,
		val accounts: List<AccountItem> = emptyList(),
		val selectedAccountIndex: Int = 0,
		val cards: List<CardItem> = emptyList()
	) {
		val selectedAccount: AccountItem?
			get() = accounts.getOrNull(selectedAccountIndex)
	}

	data class AccountItem(
		val id: String,
		val name: String,
		val accountNumber: String,
		val currency: String
	)

	data class CardItem(
		val id: String,
		val holderName: String,
		val cardBrand: String,
		val cardType: String,
		val cardNumberMasked: String,
		val expiresAt: String,
		val limit: Double,
		val status: String
	)

	sealed class UiEvent {
		data object ScreenOpened : UiEvent()
		data object PreviousAccountClicked : UiEvent()
		data object NextAccountClicked : UiEvent()
		data object RetryClicked : UiEvent()
	}
}

