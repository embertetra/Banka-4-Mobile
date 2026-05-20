package rs.raf.banka4mobile.presentation.transactionsoverview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rs.raf.banka4mobile.presentation.components.AccountSwitcherHeader
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.DateSortOrder
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.SideEffect
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.TypeFilter
import java.util.Locale

@Composable
fun TransactionsOverviewScreen(
    onBack: () -> Unit,
    viewModel: TransactionsOverviewViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.onEvent(TransactionsOverviewContract.UiEvent.ScreenOpened)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                SideEffect.NavigateBack -> onBack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        AccountSwitcherHeader(
            accountName = state.selectedAccount?.name ?: "Racun",
            accountNumber = state.selectedAccount?.accountNumber ?: "",
            onPrevious = {
                viewModel.onEvent(TransactionsOverviewContract.UiEvent.PreviousAccountClicked)
            },
            onNext = {
                viewModel.onEvent(TransactionsOverviewContract.UiEvent.NextAccountClicked)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.onEvent(TransactionsOverviewContract.UiEvent.BackClicked)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Nazad",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Pregled transakcija",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tip transakcije",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.typeFilter == filter,
                        onClick = {
                            viewModel.onEvent(TransactionsOverviewContract.UiEvent.TypeFilterChanged(filter))
                        },
                        label = { Text(text = filter.toLabel()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sortiranje po iznosu",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateSortOrder.entries.forEach { sortOrder ->
                    FilterChip(
                        selected = state.dateSortOrder == sortOrder,
                        onClick = {
                            viewModel.onEvent(
                                TransactionsOverviewContract.UiEvent.DateSortOrderChanged(sortOrder)
                            )
                        },
                        label = { Text(text = sortOrder.toLabel()) }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 14.dp, bottom = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
            )

            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ucitavanje transakcija...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    )
                }

                state.filteredTransactions.isEmpty() -> {
                    Text(
                        text = "Nema transakcija za izabrane filtere.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = state.filteredTransactions,
                            key = { it.id }
                        ) { transaction ->
                            OverviewTransactionCard(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTransactionCard(transaction: TransactionsOverviewContract.TransactionItem) {
    val isReceived = transaction.type == TransactionsOverviewContract.TransactionType.RECEIVED
    val amountPrefix = if (isReceived) "+" else "-"
    val amountColor = if (isReceived) {
        rs.raf.banka4mobile.ui.theme.SuccessGreen
    } else {
        rs.raf.banka4mobile.ui.theme.ErrorRed
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = transaction.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$amountPrefix${formatAmount(transaction.amount)} ${transaction.currency}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                textAlign = TextAlign.End
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        )
    }
}

private fun TypeFilter.toLabel(): String {
    return when (this) {
        TypeFilter.ALL -> "Sve"
        TypeFilter.RECEIVED -> "Priliv"
        TypeFilter.SENT -> "Odliv"
    }
}

private fun DateSortOrder.toLabel(): String {
    return when (this) {
        DateSortOrder.ASCENDING -> "Rastuce"
        DateSortOrder.DESCENDING -> "Opadajuce"
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)
}

