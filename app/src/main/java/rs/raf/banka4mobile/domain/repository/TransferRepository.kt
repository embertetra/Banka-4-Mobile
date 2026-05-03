package rs.raf.banka4mobile.domain.repository

import rs.raf.banka4mobile.domain.model.transfer.Transfer
import rs.raf.banka4mobile.domain.model.transfer.TransferHistory

interface TransferRepository {

    suspend fun getTransfers(
        page: Int = 1,
        pageSize: Int = 10
    ): Result<TransferHistory>

    suspend fun createTransfer(
        amount: Double,
        fromAccount: String,
        toAccount: String
    ): Result<Transfer>
}