package rs.raf.banka4mobile.presentation.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.domain.model.home.BankAccountSummary
import rs.raf.banka4mobile.domain.model.home.BankCard
import rs.raf.banka4mobile.domain.repository.HomeRepository
import rs.raf.banka4mobile.navigation.Screen
import rs.raf.banka4mobile.presentation.cards.CardContract.UiEvent
import rs.raf.banka4mobile.presentation.cards.CardContract.UiState
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialAccountNumber: String? =
        savedStateHandle.get<String>(Screen.Cards.ACCOUNT_NUMBER_ARG)

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.ScreenOpened -> fetchAccountsAndCards()
            UiEvent.PreviousAccountClicked -> previousAccount()
            UiEvent.NextAccountClicked -> nextAccount()
            UiEvent.RetryClicked -> retry()
        }
    }

    private fun fetchAccountsAndCards(forceReload: Boolean = false) {
        val current = state.value
        if (current.isLoading || (!forceReload && current.accounts.isNotEmpty())) return

        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                    isCardsLoading = false,
                    errorMessage = null,
                    cards = if (forceReload) emptyList() else cards
                )
            }

            homeRepository.getAccounts()
                .onSuccess { accounts ->
                    if (accounts.isEmpty()) {
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = "Nema dostupnih racuna.",
                                accounts = emptyList(),
                                cards = emptyList()
                            )
                        }
                        return@launch
                    }

                    val selectedIndex = accounts.indexOfFirst {
                        it.accountNumber == initialAccountNumber
                    }.takeIf { it >= 0 } ?: 0

                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = null,
                            accounts = accounts.map { it.toUiAccount() },
                            selectedAccountIndex = selectedIndex,
                            cards = emptyList()
                        )
                    }

                    loadCardsForSelectedAccount()
                }
                .onFailure { throwable ->
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Greska pri ucitavanju kartica.",
                            accounts = emptyList(),
                            cards = emptyList()
                        )
                    }
                }
        }
    }

    private fun loadCardsForSelectedAccount() {
        val selectedAccount = state.value.selectedAccount ?: return

        viewModelScope.launch {
            setState {
                copy(
                    isCardsLoading = true,
                    errorMessage = null
                )
            }

            homeRepository.getCards(accountNumber = selectedAccount.accountNumber)
                .onSuccess { cards ->
                    setState {
                        copy(
                            isCardsLoading = false,
                            errorMessage = null,
                            cards = cards.map { it.toUiCard() }
                        )
                    }
                }
                .onFailure { throwable ->
                    setState {
                        copy(
                            isCardsLoading = false,
                            errorMessage = throwable.message ?: "Greska pri ucitavanju kartica.",
                            cards = emptyList()
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
        loadCardsForSelectedAccount()
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
        loadCardsForSelectedAccount()
    }

    private fun retry() {
        val current = state.value
        if (current.accounts.isEmpty()) {
            fetchAccountsAndCards(forceReload = true)
        } else {
            loadCardsForSelectedAccount()
        }
    }

    private fun BankAccountSummary.toUiAccount(): CardContract.AccountItem {
        return CardContract.AccountItem(
            id = accountNumber,
            name = name,
            accountNumber = accountNumber,
            currency = currency
        )
    }

    private fun BankCard.toUiCard(): CardContract.CardItem {
        return CardContract.CardItem(
            id = id.toString(),
            holderName = name,
            cardBrand = cardBrand,
            cardType = cardType,
            cardNumberMasked = maskCardNumber(cardNumber),
            expiresAt = expiresAt,
            limit = limit,
            status = status
        )
    }

    private fun maskCardNumber(cardNumber: String): String {
        val visiblePart = cardNumber.takeLast(4)
        return "**** **** **** $visiblePart"
    }
}