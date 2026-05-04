package rs.raf.banka4mobile.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rs.raf.banka4mobile.presentation.components.AccountSwitcherHeader
import java.util.Locale

private val GradientColor = Color(0xFF005EAD)

@Composable
fun HomeScreen(
    onOpenCards: (String) -> Unit,
    onOpenLoans: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(HomeContract.UiEvent.ScreenOpened)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect: HomeContract.SideEffect ->
            when (sideEffect) {
                is HomeContract.SideEffect.NavigateToCards -> onOpenCards(sideEffect.accountNumber)
                HomeContract.SideEffect.NavigateToLoans -> onOpenLoans()
                is HomeContract.SideEffect.ShowToast -> {
                    Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HomeScreenContent(
        state = state,
        onPrevious = { viewModel.onEvent(HomeContract.UiEvent.PreviousAccountClicked) },
        onNext = { viewModel.onEvent(HomeContract.UiEvent.NextAccountClicked) },
        onCreditInstallmentClick = { viewModel.onEvent(HomeContract.UiEvent.OpenLoansClicked) },
        onCardsClick = { viewModel.onEvent(HomeContract.UiEvent.OpenCardsClicked) },
        onInfoClick = { viewModel.onEvent(HomeContract.UiEvent.OpenInfoClicked) },
        onDismissInfo = { viewModel.onEvent(HomeContract.UiEvent.DismissInfoClicked) }
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeContract.UiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCreditInstallmentClick: () -> Unit,
    onCardsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onDismissInfo: () -> Unit
) {
    val selectedAccount = state.selectedAccount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = GradientColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Ucitavanje...", color = Color(0xFF5A5A5A))
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = Color(0xFFB3261E),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            selectedAccount != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(if (state.isInfoDialogVisible) 5.dp else 0.dp)
                        .statusBarsPadding()
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    AccountSwitcherHeader(
                        accountName = selectedAccount.name,
                        accountNumber = selectedAccount.accountNumber,
                        onPrevious = onPrevious,
                        onNext = onNext,
                        accentColor = GradientColor
                    )

                    BalanceCircle(
                        account = selectedAccount,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )

                    ActionRow(
                        onCreditInstallmentClick = onCreditInstallmentClick,
                        onInfoClick = onInfoClick,
                        onCardsClick = onCardsClick,
                        modifier = Modifier.padding(top = 14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (state.transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nema izvrsenih placanja na ovom racunu.",
                                color = Color(0xFF5A5A5A),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(items = state.transactions, key = { it.id }) { transaction ->
                                TransactionCard(transaction = transaction)
                            }
                        }
                    }
                }

                if (state.isInfoDialogVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.18f))
                    )

                    AccountInfoDialog(
                        selectedAccount = selectedAccount,
                        details = state.accountDetails,
                        onDismiss = onDismissInfo
                    )
                }
            }
        }
    }
}


@Composable
private fun ActionRow(
    onCreditInstallmentClick: () -> Unit,
    onInfoClick: () -> Unit,
    onCardsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionIconItem(
            label = "Rata",
            icon = {
                Icon(
                    imageVector = Icons.Default.RequestQuote,
                    contentDescription = "Rata",
                    tint = GradientColor
                )
            },
            onClick = onCreditInstallmentClick
        )

        ActionIconItem(
            label = "Informacije",
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Informacije",
                    tint = GradientColor
                )
            },
            onClick = onInfoClick
        )

        ActionIconItem(
            label = "Kartice",
            icon = {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Kartice",
                    tint = GradientColor
                )
            },
            onClick = onCardsClick
        )
    }
}

@Composable
private fun ActionIconItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEFF6FF))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4B4B4B)
        )
    }
}

@Composable
private fun TransactionCard(transaction: HomeContract.TransactionItem) {
    val isReceived = transaction.type == HomeContract.TransactionType.RECEIVED
    val amountColor = if (isReceived) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val amountPrefix = if (isReceived) "+" else "-"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
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
                color = Color(0xFF1F1F1F),
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
            color = GradientColor.copy(alpha = 0.20f)
        )
    }
}

@Composable
private fun BalanceCircle(
    account: HomeContract.AccountItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(350.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.20f to GradientColor.copy(alpha = 0.26f),
                            1.00f to Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension * 0.5f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(color = Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "STANJE",
                    color = Color(0xFF7A7A7A),
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatAmount(account.balance),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                HorizontalDivider(
                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 5.dp,
                        start = 13.dp,
                        end = 13.dp
                    ),
                    thickness = 0.5.dp,
                    color = GradientColor.copy(alpha = 0.20f)
                )
                Text(
                    text = formatAmount(account.availableBalance),
                    color = Color(0xFF6A6A6A),
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "RASPOLOZIVA SREDSTVA",
                    color = Color(0xFF7A7A7A),
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = account.currency,
                    color = Color(0xFF7A7A7A),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun AccountInfoDialog(
    selectedAccount: HomeContract.AccountItem,
    details: HomeContract.AccountDetailsItem?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        tonalElevation = 2.dp,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Informacije o računu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Zatvori",
                        tint = GradientColor
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Tip racuna", details?.accountType ?: selectedAccount.accountType)
                InfoRow("Vrsta racuna", details?.accountKind ?: selectedAccount.accountKind)
                InfoRow("Dnevni limit", details?.dailyLimit?.let { formatAmount(it) } ?: "-")
                InfoRow("Mesecni limit", details?.monthlyLimit?.let { formatAmount(it) } ?: "-")
                InfoRow("Dnevna potrosnja", details?.dailySpending?.let { formatAmount(it) } ?: "-")
                InfoRow(
                    "Mesecna potrosnja",
                    details?.monthlySpending?.let { formatAmount(it) } ?: "-")
                InfoRow(
                    "Rezervisana sredstva",
                    details?.reservedFunds?.let { formatAmount(it) }
                        ?: formatAmount(selectedAccount.reservedFunds))
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3A3A3A)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1F1F1F),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 6.dp),
            thickness = 0.8.dp,
            color = GradientColor.copy(alpha = 0.20f)
        )
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)
}
