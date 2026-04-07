package rs.raf.banka4mobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.presentation.home.HomeContract.SideEffect
import rs.raf.banka4mobile.presentation.home.HomeContract.UiEvent
import rs.raf.banka4mobile.presentation.home.HomeContract.UiState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(

) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.ScreenOpened -> fetchData()
            UiEvent.PreviousAccountClicked -> previousAccount()
            UiEvent.NextAccountClicked -> nextAccount()
            UiEvent.OpenCardsClicked -> openCards()
            UiEvent.CreditInstallmentClicked -> onCreditInstallmentClicked()
        }
    }

    private fun openCards() {
        if (state.value.isLoading) return

        viewModelScope.launch {
            _sideEffects.emit(SideEffect.NavigateToCards)
        }
    }

    private fun onCreditInstallmentClicked() {
        if (state.value.isLoading) return

        viewModelScope.launch {
            _sideEffects.emit(SideEffect.ShowToast("Detalji rate za kredit uskoro."))
        }
    }

    private fun fetchData() {
        if (state.value.isLoading || state.value.accounts.isNotEmpty()) return

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            try {
                // Mock podaci dok ne povezes prave API rute.
                delay(200)
                setState {
                    copy(
                        accounts = mockAccounts(),
                        transactions = mockTransactions(),
                        selectedAccountIndex = 0,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                setState { copy(errorMessage = "Greska pri ucitavanju podataka: ${e.message}") }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun previousAccount() {
        val current = state.value
        if (current.isLoading || current.accounts.isEmpty()) return

        val nextIndex = if (current.selectedAccountIndex == 0) {
            current.accounts.lastIndex
        } else {
            current.selectedAccountIndex - 1
        }

        setState {
            copy(
                selectedAccountIndex = nextIndex
            )
        }
    }

    private fun nextAccount() {
        val current = state.value
        if (current.isLoading || current.accounts.isEmpty()) return

        val nextIndex = if (current.selectedAccountIndex == current.accounts.lastIndex) {
            0
        } else {
            current.selectedAccountIndex + 1
        }

        setState {
            copy(
                selectedAccountIndex = nextIndex
            )
        }
    }

    private fun mockAccounts(): List<HomeContract.AccountItem> {
        return listOf(
            HomeContract.AccountItem(
                id = "tekuci",
                accountType = "Tekuci racun",
                accountNumber = "265-000000000001-11",
                balance = 125430.52,
                currency = "RSD"
            ),
            HomeContract.AccountItem(
                id = "devizni",
                accountType = "Devizni racun",
                accountNumber = "265-000000000002-22",
                balance = 2350.30,
                currency = "EUR"
            )
        )
    }

    private fun mockTransactions(): List<HomeContract.TransactionItem> {
        return listOf(
            HomeContract.TransactionItem(
                id = "tx-1",
                name = "Plata",
                amount = 1450.00,
                currency = "EUR",
                type = HomeContract.TransactionType.RECEIVED
            ),
            HomeContract.TransactionItem(
                id = "tx-2",
                name = "Racun za struju",
                amount = 92.50,
                currency = "EUR",
                type = HomeContract.TransactionType.SENT
            ),
            HomeContract.TransactionItem(
                id = "tx-3",
                name = "Transfer od Jelene",
                amount = 120.00,
                currency = "EUR",
                type = HomeContract.TransactionType.RECEIVED
            ),
            HomeContract.TransactionItem(
                id = "tx-4",
                name = "Kupovina - Market",
                amount = 48.20,
                currency = "EUR",
                type = HomeContract.TransactionType.SENT
            ),
            HomeContract.TransactionItem(
                id = "tx-5",
                name = "Internet",
                amount = 29.99,
                currency = "EUR",
                type = HomeContract.TransactionType.SENT
            )
        )
    }

}