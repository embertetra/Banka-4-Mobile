package rs.raf.banka4mobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.domain.model.home.BankAccountDetails
import rs.raf.banka4mobile.domain.model.home.BankAccountSummary
import rs.raf.banka4mobile.domain.model.home.BankPayment
import rs.raf.banka4mobile.domain.repository.HomeRepository
import rs.raf.banka4mobile.presentation.home.HomeContract.SideEffect
import rs.raf.banka4mobile.presentation.home.HomeContract.UiEvent
import rs.raf.banka4mobile.presentation.home.HomeContract.UiState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
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
            UiEvent.OpenInfoClicked -> setState { copy(isInfoDialogVisible = true) }
            UiEvent.DismissInfoClicked -> setState { copy(isInfoDialogVisible = false) }
        }
    }

    private fun openCards() {
        if (state.value.isLoading) return

        val accountNumber = state.value.selectedAccount?.accountNumber ?: return

        viewModelScope.launch {
            _sideEffects.emit(SideEffect.NavigateToCards(accountNumber))
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
            val accountsResult = homeRepository.getAccounts()

            accountsResult
                .onSuccess { accounts ->
                    if (accounts.isEmpty()) {
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = "Nema dostupnih racuna za prikaz.",
                                accounts = emptyList(),
                                transactions = emptyList(),
                                accountDetails = null
                            )
                        }
                        return@launch
                    }

                    val selectedAccount = accounts.first()
                    val payments = homeRepository
                        .getPayments(accountNumber = selectedAccount.accountNumber)
                        .getOrDefault(emptyList())
                    val details = homeRepository
                        .getAccountDetails(accountNumber = selectedAccount.accountNumber)
                        .getOrNull()

                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = null,
                            accounts = accounts.map { it.toUiAccount() },
                            selectedAccountIndex = 0,
                            transactions = payments.toUiTransactions(selectedAccount.accountNumber),
                            accountDetails = details?.toUiDetails()
                        )
                    }
                }
                .onFailure { throwable ->
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Greska pri ucitavanju podataka."
                        )
                    }
                }
        }
    }

    private fun loadTransactionsForSelectedAccount() {
        val selectedAccount = state.value.selectedAccount ?: return

        viewModelScope.launch {
            homeRepository.getPayments(accountNumber = selectedAccount.accountNumber)
                .onSuccess { payments ->
                    setState {
                        copy(
                            transactions = payments.toUiTransactions(selectedAccount.accountNumber)
                        )
                    }
                }
                .onFailure {
                    setState { copy(transactions = emptyList()) }
                }
        }
    }

    private fun loadDetailsForSelectedAccount() {
        val selectedAccount = state.value.selectedAccount ?: return

        viewModelScope.launch {
            homeRepository.getAccountDetails(accountNumber = selectedAccount.accountNumber)
                .onSuccess { details ->
                    setState { copy(accountDetails = details.toUiDetails()) }
                }
                .onFailure {
                    setState { copy(accountDetails = null) }
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
                selectedAccountIndex = nextIndex,
                isInfoDialogVisible = false
            )
        }

        loadTransactionsForSelectedAccount()
        loadDetailsForSelectedAccount()
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
                selectedAccountIndex = nextIndex,
                isInfoDialogVisible = false
            )
        }

        loadTransactionsForSelectedAccount()
        loadDetailsForSelectedAccount()
    }

    private fun BankAccountSummary.toUiAccount(): HomeContract.AccountItem {
        return HomeContract.AccountItem(
            id = accountNumber,
            name = name,
            accountType = accountType,
            accountKind = accountKind,
            accountNumber = accountNumber,
            balance = balance,
            availableBalance = availableBalance,
            reservedFunds = reservedFunds,
            currency = currency
        )
    }

    private fun BankAccountDetails.toUiDetails(): HomeContract.AccountDetailsItem {
        return HomeContract.AccountDetailsItem(
            dailyLimit = dailyLimit,
            monthlyLimit = monthlyLimit,
            dailySpending = dailySpending,
            monthlySpending = monthlySpending,
            reservedFunds = reservedFunds,
            accountType = accountType,
            accountKind = accountKind
        )
    }

    private fun List<BankPayment>.toUiTransactions(
        selectedAccountNumber: String
    ): List<HomeContract.TransactionItem> {
        return map { payment ->
            val isSent = payment.payerAccount == selectedAccountNumber
            val transactionType = if (isSent) {
                HomeContract.TransactionType.SENT
            } else {
                HomeContract.TransactionType.RECEIVED
            }

            val displayName = when {
                payment.recipientName.isNotBlank() -> payment.recipientName
                payment.purpose.isNotBlank() -> payment.purpose
                isSent -> "Odliv"
                else -> "Priliv"
            }

            HomeContract.TransactionItem(
                id = payment.id.toString(),
                name = displayName,
                amount = payment.amount,
                currency = payment.currency,
                type = transactionType
            )
        }
    }

}