package rs.raf.banka4mobile.presentation.transactionsoverview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.domain.model.home.BankAccountSummary
import rs.raf.banka4mobile.domain.model.home.BankPayment
import rs.raf.banka4mobile.domain.repository.HomeRepository
import rs.raf.banka4mobile.navigation.Screen
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.SideEffect
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.UiEvent
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.UiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class TransactionsOverviewViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountNumber: String? =
        savedStateHandle.get<String>(Screen.TransactionsOverview.ACCOUNT_NUMBER_ARG)

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.ScreenOpened -> loadInitialData()
            UiEvent.PreviousAccountClicked -> previousAccount()
            UiEvent.NextAccountClicked -> nextAccount()
            is UiEvent.TypeFilterChanged -> setState { copy(typeFilter = event.filter) }
            is UiEvent.DateSortOrderChanged -> setState { copy(dateSortOrder = event.sortOrder) }
            UiEvent.BackClicked -> navigateBack()
        }
    }

    private fun loadInitialData() {
        if (state.value.isLoading || state.value.accounts.isNotEmpty()) return

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            homeRepository.getAccounts()
                .onSuccess { accounts ->
                    if (accounts.isEmpty()) {
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = "Nema dostupnih racuna.",
                                accounts = emptyList(),
                                transactions = emptyList()
                            )
                        }
                        return@launch
                    }

                    val selectedIndex = accounts.indexOfFirst {
                        it.accountNumber == accountNumber
                    }.takeIf { it >= 0 } ?: 0

                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = null,
                            accounts = accounts.map { it.toUiAccount() },
                            selectedAccountIndex = selectedIndex,
                            transactions = emptyList()
                        )
                    }

                    loadTransactionsForSelectedAccount()
                }
                .onFailure { throwable ->
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Greska pri ucitavanju racuna.",
                            accounts = emptyList(),
                            transactions = emptyList()
                        )
                    }
                }
        }
    }

    private fun loadTransactionsForSelectedAccount() {
        val selectedAccount = state.value.selectedAccount ?: return
        val selectedAccountNumber = selectedAccount.accountNumber

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null, transactions = emptyList()) }

            homeRepository.getPayments(selectedAccountNumber)
                .onSuccess { payments ->
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = null,
                            transactions = payments.toUiTransactions(selectedAccountNumber)
                        )
                    }
                }
                .onFailure { throwable ->
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Greska pri ucitavanju transakcija."
                        )
                    }
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

        setState { copy(selectedAccountIndex = nextIndex) }
        loadTransactionsForSelectedAccount()
    }

    private fun nextAccount() {
        val current = state.value
        if (current.isLoading || current.accounts.isEmpty()) return

        val nextIndex = if (current.selectedAccountIndex == current.accounts.lastIndex) {
            0
        } else {
            current.selectedAccountIndex + 1
        }

        setState { copy(selectedAccountIndex = nextIndex) }
        loadTransactionsForSelectedAccount()
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _sideEffects.emit(SideEffect.NavigateBack)
        }
    }

    private fun List<BankPayment>.toUiTransactions(
        selectedAccountNumber: String
    ): List<TransactionsOverviewContract.TransactionItem> {
        return map { payment ->
            val isSent = payment.payerAccount == selectedAccountNumber
            val transactionType = if (isSent) {
                TransactionsOverviewContract.TransactionType.SENT
            } else {
                TransactionsOverviewContract.TransactionType.RECEIVED
            }

            val displayName = when {
                payment.recipientName.isNotBlank() -> payment.recipientName
                payment.purpose.isNotBlank() -> payment.purpose
                isSent -> "Odliv"
                else -> "Priliv"
            }

            TransactionsOverviewContract.TransactionItem(
                id = payment.id.toString(),
                name = displayName,
                amount = payment.amount,
                currency = payment.currency,
                type = transactionType,
                createdAtEpochMillis = parseCreatedAt(payment.createdAt)
            )
        }
    }

    private fun parseCreatedAt(createdAt: String): Long {
        return runCatching { Instant.parse(createdAt).toEpochMilli() }
            .recoverCatching { OffsetDateTime.parse(createdAt).toInstant().toEpochMilli() }
            .recoverCatching {
                LocalDateTime.parse(createdAt).toInstant(ZoneOffset.UTC).toEpochMilli()
            }
            .getOrDefault(0L)
    }

    private fun BankAccountSummary.toUiAccount(): TransactionsOverviewContract.AccountItem {
        return TransactionsOverviewContract.AccountItem(
            id = accountNumber,
            name = name,
            accountNumber = accountNumber
        )
    }
}

