package rs.raf.banka4mobile.presentation.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.domain.model.home.BankLoan
import rs.raf.banka4mobile.domain.repository.HomeRepository
import rs.raf.banka4mobile.presentation.loan.LoanContract.UiEvent
import rs.raf.banka4mobile.presentation.loan.LoanContract.UiState
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
	private val homeRepository: HomeRepository
) : ViewModel() {

	private val _state = MutableStateFlow(UiState())
	val state = _state.asStateFlow()

	private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

	fun onEvent(event: UiEvent) {
		when (event) {
			UiEvent.ScreenOpened -> fetchData()
		}
	}

	private fun fetchData() {
		if (state.value.isLoading || state.value.loans.isNotEmpty()) return

		viewModelScope.launch {
			setState { copy(isLoading = true, errorMessage = null) }

			homeRepository.getLoans()
				.onSuccess { loans ->
					println("Uspesno ucitani krediti: ${loans.size}")
					println("Lista kredita: $loans")

					if (loans.isEmpty()) {
						setState {
							copy(
								isLoading = false,
								errorMessage = null,
								loans = emptyList()
							)
						}
						return@launch
					}

					setState {
						copy(
							isLoading = false,
							errorMessage = null,
							loans = loans.map { it.toUiItem() }
						)
					}
				}
				.onFailure { throwable ->
					setState {
						copy(
							isLoading = false,
							errorMessage = throwable.message ?: "Greska pri ucitavanju rata."
						)
					}
				}
		}
	}

	private fun BankLoan.toUiItem(): LoanContract.LoanItem {
		return LoanContract.LoanItem(
			id = id,
			amount = amount,
			currency = currency,
			loanType = loanType,
			monthlyInstallment = monthlyInstallment,
			status = status
		)
	}
}