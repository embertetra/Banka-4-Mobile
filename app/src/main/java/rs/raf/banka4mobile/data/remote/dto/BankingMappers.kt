package rs.raf.banka4mobile.data.remote.dto

import rs.raf.banka4mobile.domain.model.home.BankAccountDetails
import rs.raf.banka4mobile.domain.model.home.BankAccountSummary
import rs.raf.banka4mobile.domain.model.home.BankCard
import rs.raf.banka4mobile.domain.model.home.BankPayment

fun AccountSummaryDto.toDomain(): BankAccountSummary {
    return BankAccountSummary(
        accountNumber = accountNumber,
        name = name,
        accountType = accountType,
        accountKind = accountKind,
        currency = currency,
        balance = balance,
        availableBalance = availableBalance,
        reservedFunds = reservedFunds
    )
}

fun AccountDetailsDto.toDomain(): BankAccountDetails {
    return BankAccountDetails(
        accountNumber = accountNumber,
        name = name,
        clientId = clientId,
        companyId = companyId,
        employeeId = employeeId,
        balance = balance,
        availableBalance = availableBalance,
        currency = currency,
        status = status,
        accountType = accountType,
        accountKind = accountKind,
        dailyLimit = dailyLimit,
        monthlyLimit = monthlyLimit,
        dailySpending = dailySpending,
        monthlySpending = monthlySpending,
        reservedFunds = reservedFunds
    )
}

fun CardDto.toDomain(): BankCard {
    return BankCard(
        id = id,
        accountName = accountName,
        accountNumber = accountNumber,
        authorizedPersonId = authorizedPersonId,
        cardBrand = cardBrand,
        cardNumber = cardNumber,
        cardType = cardType,
        expiresAt = expiresAt,
        limit = limit,
        name = name,
        status = status
    )
}

fun PaymentDto.toDomain(): BankPayment {
    return BankPayment(
        id = id,
        amount = amount,
        createdAt = createdAt,
        currency = currency,
        payerAccount = payerAccount,
        paymentCode = paymentCode,
        purpose = purpose,
        recipientAccount = recipientAccount,
        recipientName = recipientName,
        status = status
    )
}

