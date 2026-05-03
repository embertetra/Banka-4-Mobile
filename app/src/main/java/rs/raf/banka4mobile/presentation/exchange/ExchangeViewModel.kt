package rs.raf.banka4mobile.presentation.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.domain.repository.ExchangeRepository
import rs.raf.banka4mobile.presentation.exchange.ExchangeContract.UiEvent
import rs.raf.banka4mobile.presentation.exchange.ExchangeContract.UiState
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val exchangeRepository: ExchangeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.ScreenOpened -> loadRates()

            is UiEvent.AmountChanged -> {
                onAmountChanged(event.value)
            }

            is UiEvent.FromCurrencyChanged -> {
                _state.update {
                    it.copy(
                        fromCurrency = event.value,
                        errorMessage = null
                    )
                }
                recalculate()
            }

            is UiEvent.ToCurrencyChanged -> {
                _state.update {
                    it.copy(
                        toCurrency = event.value,
                        errorMessage = null
                    )
                }
                recalculate()
            }

            UiEvent.SwapCurrenciesClicked -> swapCurrencies()

            is UiEvent.BuyAmountChanged -> {
                onBuyAmountChanged(event.value)
            }

            is UiEvent.BuyFromCurrencyChanged -> {
                _state.update {
                    it.copy(
                        buyFromCurrency = event.value,
                        errorMessage = null,
                        successMessage = null
                    )
                }
                previewPurchase()
            }

            is UiEvent.BuyToCurrencyChanged -> {
                _state.update {
                    it.copy(
                        buyToCurrency = event.value,
                        errorMessage = null,
                        successMessage = null
                    )
                }
                previewPurchase()
            }

            UiEvent.PreviewPurchaseClicked -> previewPurchase()

            UiEvent.ConfirmPurchaseClicked -> confirmPurchase()

            UiEvent.ClearSuccessMessage -> {
                _state.update {
                    it.copy(successMessage = null)
                }
            }
        }
    }

    private fun loadRates() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            exchangeRepository.getExchangeRates()
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            rates = result.rates.map { rate ->
                                ExchangeRateUiModel(
                                    currencyCode = rate.currencyCode,
                                    currencyName = rate.currencyName,
                                    buyRate = rate.buyRate,
                                    middleRate = rate.middleRate,
                                    sellRate = rate.sellRate
                                )
                            },
                            updatedAtText = result.updatedAt,
                            nextUpdateText = result.nextUpdateAt,
                            isLoading = false,
                            errorMessage = null
                        )
                    }

                    recalculate()
                    previewPurchase()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Greška pri učitavanju kursne liste."
                        )
                    }
                }
        }
    }

    private fun onAmountChanged(value: String) {
        val normalized = value.replace(',', '.')
        if (normalized.count { it == '.' } > 1) return
        if (normalized.isNotEmpty() && normalized.any { !it.isDigit() && it != '.' }) return

        _state.update {
            it.copy(
                amountInput = value,
                errorMessage = null
            )
        }

        recalculate()
    }

    private fun onBuyAmountChanged(value: String) {
        val normalized = value.replace(',', '.')
        if (normalized.count { it == '.' } > 1) return
        if (normalized.isNotEmpty() && normalized.any { !it.isDigit() && it != '.' }) return

        _state.update {
            it.copy(
                buyAmountInput = value,
                errorMessage = null,
                successMessage = null
            )
        }

        previewPurchase()
    }

    private fun swapCurrencies() {
        val current = _state.value

        _state.update {
            it.copy(
                fromCurrency = current.toCurrency,
                toCurrency = current.fromCurrency,
                errorMessage = null
            )
        }

        recalculate()
    }

    private fun recalculate() {
        val current = _state.value
        val amount = current.amountInput.replace(',', '.').toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _state.update {
                it.copy(
                    convertedAmountText = "0.00 ${current.toCurrency}",
                    helperText = ""
                )
            }
            return
        }

        viewModelScope.launch {
            exchangeRepository.calculateExchange(
                amount = amount,
                fromCurrency = current.fromCurrency,
                toCurrency = current.toCurrency
            ).onSuccess { result ->
                _state.update {
                    it.copy(
                        convertedAmountText = "${formatNumber(result.total)} ${result.toCurrency}",
                        helperText = "${formatNumber(result.amount)} ${result.fromCurrency}",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        convertedAmountText = "0.00 ${current.toCurrency}",
                        helperText = "",
                        errorMessage = error.message ?: "Greška pri preračunu valute."
                    )
                }
            }
        }
    }

    private fun previewPurchase() {
        val current = _state.value
        val amount = current.buyAmountInput.replace(',', '.').toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _state.update {
                it.copy(
                    buyPreviewText = "0.00 ${current.buyToCurrency}"
                )
            }
            return
        }

        if (current.buyFromCurrency == current.buyToCurrency) {
            _state.update {
                it.copy(
                    buyPreviewText = "0.00 ${current.buyToCurrency}",
                    errorMessage = "Valute ne mogu biti iste."
                )
            }
            return
        }

        viewModelScope.launch {
            exchangeRepository.calculateExchange(
                amount = amount,
                fromCurrency = current.buyFromCurrency,
                toCurrency = current.buyToCurrency
            ).onSuccess { result ->
                _state.update {
                    it.copy(
                        buyPreviewText = "${formatNumber(result.total)} ${result.toCurrency}",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        buyPreviewText = "0.00 ${current.buyToCurrency}",
                        errorMessage = error.message ?: "Greška pri prikazu kupovine."
                    )
                }
            }
        }
    }

    private fun confirmPurchase() {
        val current = _state.value
        val amount = current.buyAmountInput.replace(',', '.').toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _state.update {
                it.copy(errorMessage = "Unesite validan iznos za kupovinu.")
            }
            return
        }

        if (current.buyFromCurrency == current.buyToCurrency) {
            _state.update {
                it.copy(errorMessage = "Izvorna i ciljna valuta ne mogu biti iste.")
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isBuying = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            exchangeRepository.purchaseCurrency(
                amount = amount,
                fromCurrency = current.buyFromCurrency,
                toCurrency = current.buyToCurrency
            ).onSuccess { result ->
                _state.update {
                    it.copy(
                        isBuying = false,
                        successMessage = "${result.message} Potrošeno: ${formatNumber(result.spentAmount)} ${result.spentCurrency}, dobijeno: ${formatNumber(result.receivedAmount)} ${result.receivedCurrency}.",
                        buyPreviewText = "${formatNumber(result.receivedAmount)} ${result.receivedCurrency}",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isBuying = false,
                        successMessage = null,
                        errorMessage = error.message ?: "Kupovina valute nije uspela."
                    )
                }
            }
        }
    }

    private fun formatNumber(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }
}