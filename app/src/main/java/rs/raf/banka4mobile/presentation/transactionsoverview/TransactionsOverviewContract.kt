package rs.raf.banka4mobile.presentation.transactionsoverview

interface TransactionsOverviewContract {

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val accounts: List<AccountItem> = emptyList(),
        val selectedAccountIndex: Int = 0,
        val transactions: List<TransactionItem> = emptyList(),
        val typeFilter: TypeFilter = TypeFilter.ALL,
        val amountSortOrder: AmountSortOrder = AmountSortOrder.DESCENDING,
        val dateSortOrder: DateSortOrder = DateSortOrder.DESCENDING,
        val isFiltersVisible: Boolean = false,
        val expandedTransactionIds: Set<String> = emptySet()
    ) {
        val selectedAccount: AccountItem?
            get() = accounts.getOrNull(selectedAccountIndex)

        val filteredTransactions: List<TransactionItem>
            get() {
                val byType = when (typeFilter) {
                    TypeFilter.ALL -> transactions
                    TypeFilter.RECEIVED -> transactions.filter { it.type == TransactionType.RECEIVED }
                    TypeFilter.SENT -> transactions.filter { it.type == TransactionType.SENT }
                }

                return byType.sortedWith { first, second ->
                    val firstSignedAmount = if (first.type == TransactionType.SENT) {
                        -kotlin.math.abs(first.amount)
                    } else {
                        kotlin.math.abs(first.amount)
                    }
                    val secondSignedAmount = if (second.type == TransactionType.SENT) {
                        -kotlin.math.abs(second.amount)
                    } else {
                        kotlin.math.abs(second.amount)
                    }

                    val amountCompare = when (amountSortOrder) {
                        AmountSortOrder.ASCENDING -> firstSignedAmount.compareTo(secondSignedAmount)
                        AmountSortOrder.DESCENDING -> secondSignedAmount.compareTo(firstSignedAmount)
                    }

                    if (amountCompare != 0) {
                        amountCompare
                    } else {
                        when (dateSortOrder) {
                            DateSortOrder.ASCENDING -> {
                                first.createdAtEpochMillis.compareTo(second.createdAtEpochMillis)
                            }
                            DateSortOrder.DESCENDING -> {
                                second.createdAtEpochMillis.compareTo(first.createdAtEpochMillis)
                            }
                        }
                    }
                }
            }
    }

    data class AccountItem(
        val id: String,
        val name: String,
        val accountNumber: String
    )

    enum class TypeFilter {
        ALL,
        RECEIVED,
        SENT
    }

    enum class AmountSortOrder {
        ASCENDING,
        DESCENDING
    }

    enum class DateSortOrder {
        ASCENDING,
        DESCENDING
    }

    enum class TransactionType {
        SENT,
        RECEIVED
    }

    data class TransactionItem(
        val id: String,
        val name: String,
        val amount: Double,
        val currency: String,
        val type: TransactionType,
        val createdAtEpochMillis: Long,
        val createdAt: String,
        val status: String,
        val purpose: String,
        val paymentCode: String,
        val recipientAccount: String,
        val payerAccount: String
    )

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()
        data object PreviousAccountClicked : UiEvent()
        data object NextAccountClicked : UiEvent()
        data object ToggleFiltersClicked : UiEvent()
        data class TypeFilterChanged(val filter: TypeFilter) : UiEvent()
        data class AmountSortOrderChanged(val sortOrder: AmountSortOrder) : UiEvent()
        data class DateSortOrderChanged(val sortOrder: DateSortOrder) : UiEvent()
        data class ToggleTransactionExpanded(val id: String) : UiEvent()
        data object BackClicked : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateBack : SideEffect()
    }

}

