package rs.raf.banka4mobile.presentation.transactionsoverview

interface TransactionsOverviewContract {

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val accounts: List<AccountItem> = emptyList(),
        val selectedAccountIndex: Int = 0,
        val transactions: List<TransactionItem> = emptyList(),
        val typeFilter: TypeFilter = TypeFilter.ALL,
        val dateSortOrder: DateSortOrder = DateSortOrder.DESCENDING
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

                return when (dateSortOrder) {
                    DateSortOrder.ASCENDING -> byType.sortedBy {
                        val normalized = kotlin.math.abs(it.amount)
                        if (it.type == TransactionType.SENT) -normalized else normalized
                    }
                    DateSortOrder.DESCENDING -> byType.sortedByDescending {
                        val normalized = kotlin.math.abs(it.amount)
                        if (it.type == TransactionType.SENT) -normalized else normalized
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
        val createdAtEpochMillis: Long
    )

    sealed class UiEvent {
        data object ScreenOpened : UiEvent()
        data object PreviousAccountClicked : UiEvent()
        data object NextAccountClicked : UiEvent()
        data class TypeFilterChanged(val filter: TypeFilter) : UiEvent()
        data class DateSortOrderChanged(val sortOrder: DateSortOrder) : UiEvent()
        data object BackClicked : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateBack : SideEffect()
    }

}

