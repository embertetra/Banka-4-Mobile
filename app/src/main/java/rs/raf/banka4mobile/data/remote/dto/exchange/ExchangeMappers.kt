package rs.raf.banka4mobile.data.remote.dto.exchange

import rs.raf.banka4mobile.domain.model.exchange.ExchangeCalculation
import rs.raf.banka4mobile.domain.model.exchange.ExchangeRate
import rs.raf.banka4mobile.domain.model.exchange.ExchangeRates

private fun mapCurrencyName(code: String): String {
    return when (code.uppercase()) {
        "EUR" -> "Evro"
        "USD" -> "Američki dolar"
        "CHF" -> "Švajcarski franak"
        "GBP" -> "Britanska funta"
        "JPY" -> "Japanski jen"
        "CAD" -> "Kanadski dolar"
        "AUD" -> "Australijski dolar"
        "RSD" -> "Srpski dinar"
        else -> code
    }
}

fun ExchangeRateDto.toDomain(): ExchangeRate {
    return ExchangeRate(
        currencyCode = currency,
        currencyName = mapCurrencyName(currency),
        buyRate = buyRate,
        middleRate = middleRate,
        sellRate = sellRate
    )
}

fun ExchangeRatesResponseDto.toDomain(): ExchangeRates {
    return ExchangeRates(
        baseCurrency = baseCurrency,
        updatedAt = updatedAt,
        nextUpdateAt = nextUpdateAt,
        rates = rates.map { it.toDomain() }
    )
}

fun ExchangeCalculateResponseDto.toDomain(): ExchangeCalculation {
    return ExchangeCalculation(
        amount = amount,
        fromCurrency = fromCurrency,
        toCurrency = toCurrency,
        total = total
    )
}