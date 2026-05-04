package rs.raf.banka4mobile.presentation.loan

interface LoanContract {

	data class UiState(
		val isLoading: Boolean = false,
		val errorMessage: String? = null,
		val loans: List<LoanItem> = emptyList()
	)

	data class LoanItem(
		val id: Long,
		val amount: Double,
		val currency: String,
		val loanType: String,
		val monthlyInstallment: Double,
		val status: String
	)

	sealed class UiEvent {
		data object ScreenOpened : UiEvent()
	}
}