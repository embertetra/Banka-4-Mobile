package rs.raf.banka4mobile.data.remote.dto

import rs.raf.banka4mobile.domain.model.transfer.Transfer
import rs.raf.banka4mobile.domain.model.transfer.TransferHistory

fun TransferResponseDto.toDomain(): Transfer {
    return Transfer(
        transferId = transferId,
        transactionId = transactionId,
        fromAccountNumber = fromAccountNumber,
        toAccountNumber = toAccountNumber,
        initialAmount = initialAmount,
        finalAmount = finalAmount,
        commission = commission,
        exchangeRate = exchangeRate,
        createdAt = createdAt
    )
}

fun TransfersResponseDto.toDomain(): TransferHistory {
    return TransferHistory(
        transfers = data.map { it.toDomain() },
        page = page,
        pageSize = pageSize,
        total = total,
        totalPages = totalPages
    )
}