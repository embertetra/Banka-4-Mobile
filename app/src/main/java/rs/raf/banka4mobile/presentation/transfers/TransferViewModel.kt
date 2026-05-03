package rs.raf.banka4mobile.presentation.transfers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.domain.model.home.BankAccountSummary
import rs.raf.banka4mobile.domain.model.transfer.Transfer
import rs.raf.banka4mobile.domain.repository.HomeRepository
import rs.raf.banka4mobile.domain.repository.TransferRepository
import rs.raf.banka4mobile.presentation.transfers.TransferContract.UiEvent
import rs.raf.banka4mobile.presentation.transfers.TransferContract.UiState
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val transferRepository: TransferRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.ScreenOpened -> loadData()
            UiEvent.PreviousAccountClicked -> previousAccount()
            UiEvent.NextAccountClicked -> nextAccount()
            is UiEvent.ToAccountChanged -> {
                _state.update {
                    it.copy(
                        toAccountInput = event.value,
                        errorMessage = null,
                        successMessage = null
                    )
                }
            }
            is UiEvent.AmountChanged -> onAmountChanged(event.value)
            UiEvent.SubmitTransferClicked -> submitTransfer()
            UiEvent.ClearMessages -> {
                _state.update {
                    it.copy(
                        errorMessage = null,
                        successMessage = null
                    )
                }
            }
        }
    }

    private fun loadData() {
        if (_state.value.isLoading || _state.value.accounts.isNotEmpty()) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            val accountsResult = homeRepository.getAccounts()
            val transfersResult = transferRepository.getTransfers(page = 1, pageSize = 10)

            val accounts = accountsResult.getOrElse { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Greška pri učitavanju računa."
                    )
                }
                return@launch
            }

            val transfers = transfersResult.getOrNull()?.transfers.orEmpty()

            _state.update {
                it.copy(
                    isLoading = false,
                    accounts = accounts.map { account -> account.toUiAccount() },
                    transfers = transfers.map { transfer -> transfer.toUiTransfer() },
                    errorMessage = null
                )
            }
        }
    }

    private fun previousAccount() {
        val current = _state.value
        if (current.accounts.isEmpty() || current.isSubmitting) return

        val nextIndex = if (current.selectedFromAccountIndex == 0) {
            current.accounts.lastIndex
        } else {
            current.selectedFromAccountIndex - 1
        }

        _state.update {
            it.copy(
                selectedFromAccountIndex = nextIndex,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun nextAccount() {
        val current = _state.value
        if (current.accounts.isEmpty() || current.isSubmitting) return

        val nextIndex = if (current.selectedFromAccountIndex == current.accounts.lastIndex) {
            0
        } else {
            current.selectedFromAccountIndex + 1
        }

        _state.update {
            it.copy(
                selectedFromAccountIndex = nextIndex,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun onAmountChanged(value: String) {
        val normalized = value.replace(',', '.')
        if (normalized.count { it == '.' } > 1) return
        if (normalized.isNotEmpty() && normalized.any { !it.isDigit() && it != '.' }) return

        _state.update {
            it.copy(
                amountInput = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun submitTransfer() {
        val current = _state.value
        val fromAccount = current.selectedFromAccount?.accountNumber
        val toAccount = current.toAccountInput.trim()
        val amount = current.amountInput.replace(',', '.').toDoubleOrNull()

        when {
            fromAccount.isNullOrBlank() -> {
                _state.update { it.copy(errorMessage = "Izaberite račun sa kog šaljete novac.") }
                return
            }

            toAccount.isBlank() -> {
                _state.update { it.copy(errorMessage = "Unesite račun primaoca.") }
                return
            }

            fromAccount == toAccount -> {
                _state.update { it.copy(errorMessage = "Račun pošiljaoca i primaoca ne mogu biti isti.") }
                return
            }

            amount == null || amount <= 0.0 -> {
                _state.update { it.copy(errorMessage = "Unesite ispravan iznos.") }
                return
            }
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            transferRepository.createTransfer(
                amount = amount,
                fromAccount = fromAccount,
                toAccount = toAccount
            ).onSuccess { transfer ->
                val refreshedTransfers = transferRepository
                    .getTransfers(page = 1, pageSize = 10)
                    .getOrNull()
                    ?.transfers
                    .orEmpty()

                _state.update {
                    it.copy(
                        isSubmitting = false,
                        amountInput = "",
                        toAccountInput = "",
                        successMessage = "Transfer je uspešno izvršen.",
                        transfers = if (refreshedTransfers.isNotEmpty()) {
                            refreshedTransfers.map { item -> item.toUiTransfer() }
                        } else {
                            listOf(transfer.toUiTransfer()) + it.transfers
                        }
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Greška pri izvršavanju transfera."
                    )
                }
            }
        }
    }

    private fun BankAccountSummary.toUiAccount(): TransferContract.AccountItem {
        return TransferContract.AccountItem(
            accountNumber = accountNumber,
            name = name,
            balance = balance,
            currency = currency
        )
    }

    private fun Transfer.toUiTransfer(): TransferContract.TransferItem {
        return TransferContract.TransferItem(
            id = transferId.toString(),
            fromAccount = fromAccountNumber,
            toAccount = toAccountNumber,
            initialAmount = initialAmount,
            finalAmount = finalAmount,
            commission = commission,
            createdAt = createdAt
        )
    }
}

fun formatTransferAmount(value: Double): String {
    return String.format(Locale.US, "%,.2f", value)
}