package rs.raf.banka4mobile.presentation.exchange

data class ExchangeRateUiModel(
    val currencyCode: String,
    val currencyName: String,
    val buyRate: Double,
    val middleRate: Double,
    val sellRate: Double
)

interface ExchangeContract {

    data class UiState(
        val rates: List<ExchangeRateUiModel> = emptyList(),
        val amountInput: String = "1",
        val fromCurrency: String = "EUR",
        val toCurrency: String = "RSD",
        val convertedAmountText: String = "0.00 RSD",
        val helperText: String = "",
        val buyAmountInput: String = "1000",
        val buyFromCurrency: String = "RSD",
        val buyToCurrency: String = "EUR",
        val buyPreviewText: String = "0.00 EUR",
        val successMessage: String? = null,
        val updatedAtText: String = "",
        val nextUpdateText: String = "",
        val isLoading: Boolean = false,
        val isBuying: Boolean = false,
        val errorMessage: String? = null
    )

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()

        data class AmountChanged(val value: String) : UiEvent()
        data class FromCurrencyChanged(val value: String) : UiEvent()
        data class ToCurrencyChanged(val value: String) : UiEvent()
        data object SwapCurrenciesClicked : UiEvent()

        data class BuyAmountChanged(val value: String) : UiEvent()
        data class BuyFromCurrencyChanged(val value: String) : UiEvent()
        data class BuyToCurrencyChanged(val value: String) : UiEvent()
        data object PreviewPurchaseClicked : UiEvent()
        data object ConfirmPurchaseClicked : UiEvent()

        data object ClearSuccessMessage : UiEvent()
    }
}