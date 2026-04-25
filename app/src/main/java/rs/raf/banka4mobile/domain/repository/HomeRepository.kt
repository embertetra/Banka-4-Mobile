package rs.raf.banka4mobile.domain.repository

import rs.raf.banka4mobile.domain.model.home.BankAccountDetails
import rs.raf.banka4mobile.domain.model.home.BankAccountSummary
import rs.raf.banka4mobile.domain.model.home.BankCard
import rs.raf.banka4mobile.domain.model.home.BankPayment

interface HomeRepository {

    suspend fun getAccounts(): Result<List<BankAccountSummary>>

    suspend fun getAccountDetails(accountNumber: String): Result<BankAccountDetails>

    suspend fun getCards(accountNumber: String): Result<List<BankCard>>

    suspend fun getPayments(accountNumber: String): Result<List<BankPayment>>
}