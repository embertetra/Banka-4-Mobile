package rs.raf.banka4mobile.presentation.transactionsoverview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rs.raf.banka4mobile.presentation.components.AccountSwitcherHeader
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.AmountSortOrder
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.DateSortOrder
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.SideEffect
import rs.raf.banka4mobile.presentation.transactionsoverview.TransactionsOverviewContract.TypeFilter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionsOverviewScreen(
    onBack: () -> Unit,
    viewModel: TransactionsOverviewViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val extraBottomScrollSpace = 116.dp

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 12.dp),
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

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = {
                viewModel.onEvent(TransactionsOverviewContract.UiEvent.ToggleFiltersClicked)
            }) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Prikazi filtere",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
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
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .padding(top = 12.dp),
                            contentPadding = PaddingValues(bottom = extraBottomScrollSpace),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = state.filteredTransactions,
                                key = { it.id }
                            ) { transaction ->
                                OverviewTransactionCard(
                                    transaction = transaction,
                                    isExpanded = state.expandedTransactionIds.contains(transaction.id),
                                    onToggleExpanded = {
                                        viewModel.onEvent(
                                            TransactionsOverviewContract.UiEvent.ToggleTransactionExpanded(
                                                transaction.id
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (state.isFiltersVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.onEvent(TransactionsOverviewContract.UiEvent.ToggleFiltersClicked)
                            }
                    )
                }

                FiltersOverlay(
                    isVisible = state.isFiltersVisible,
                    typeFilter = state.typeFilter,
                    amountSortOrder = state.amountSortOrder,
                    dateSortOrder = state.dateSortOrder,
                    onTypeFilterChanged = {
                        viewModel.onEvent(TransactionsOverviewContract.UiEvent.TypeFilterChanged(it))
                    },
                    onAmountSortOrderChanged = {
                        viewModel.onEvent(
                            TransactionsOverviewContract.UiEvent.AmountSortOrderChanged(it)
                        )
                    },
                    onDateSortOrderChanged = {
                        viewModel.onEvent(
                            TransactionsOverviewContract.UiEvent.DateSortOrderChanged(it)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FiltersOverlay(
    isVisible: Boolean,
    typeFilter: TypeFilter,
    amountSortOrder: AmountSortOrder,
    dateSortOrder: DateSortOrder,
    onTypeFilterChanged: (TypeFilter) -> Unit,
    onAmountSortOrderChanged: (AmountSortOrder) -> Unit,
    onDateSortOrderChanged: (DateSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
        exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Tip transakcije",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = typeFilter == filter,
                            onClick = { onTypeFilterChanged(filter) },
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmountSortOrder.entries.forEach { sortOrder ->
                        FilterChip(
                            selected = amountSortOrder == sortOrder,
                            onClick = { onAmountSortOrderChanged(sortOrder) },
                            label = { Text(text = sortOrder.toLabel()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sortiranje po datumu",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateSortOrder.entries.forEach { sortOrder ->
                        FilterChip(
                            selected = dateSortOrder == sortOrder,
                            onClick = { onDateSortOrderChanged(sortOrder) },
                            label = { Text(text = sortOrder.toLabel()) }
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
            )
        }
    }
}

@Composable
private fun OverviewTransactionCard(
    transaction: TransactionsOverviewContract.TransactionItem,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val isReceived = transaction.type == TransactionsOverviewContract.TransactionType.RECEIVED
    val amountPrefix = if (isReceived) "+" else "-"
    val amountColor = if (isReceived) {
        rs.raf.banka4mobile.ui.theme.SuccessGreen
    } else {
        rs.raf.banka4mobile.ui.theme.ErrorRed
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 3.dp else 8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.22f else 0.42f)
        ),
        modifier = Modifier
            .animateContentSize(animationSpec = tween(220))
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleExpanded
            )
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$amountPrefix${formatAmount(transaction.amount)} ${transaction.currency}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    textAlign = TextAlign.End
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Sakrij detalje" else "Prikazi detalje",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
            exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140))
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TransactionDetailRow("Status", transaction.status)
                TransactionDetailRow("Svrha", transaction.purpose)
                TransactionDetailRow("Sifra placanja", transaction.paymentCode)
                TransactionDetailRow("Racun primaoca", transaction.recipientAccount)
                TransactionDetailRow("Racun platioca", transaction.payerAccount)
                TransactionDetailRow(
                    "Datum",
                    formatDateTime(transaction.createdAtEpochMillis, transaction.createdAt)
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
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

private fun AmountSortOrder.toLabel(): String {
    return when (this) {
        AmountSortOrder.ASCENDING -> "Rastuce"
        AmountSortOrder.DESCENDING -> "Opadajuce"
    }
}

private fun DateSortOrder.toLabel(): String {
    return when (this) {
        DateSortOrder.ASCENDING -> "Rastuce"
        DateSortOrder.DESCENDING -> "Opadajuce"
    }
}

private fun formatDateTime(createdAtEpochMillis: Long, fallback: String): String {
    if (createdAtEpochMillis <= 0L) return fallback

    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        Instant.ofEpochMilli(createdAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }.getOrDefault(fallback)
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)
}
