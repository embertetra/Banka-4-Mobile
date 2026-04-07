package rs.raf.banka4mobile.presentation.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

private val GradientColor = Color(0xFF005EAD)

@Composable
fun HomeScreen(
    onOpenCards: () -> Unit,
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
                HomeContract.SideEffect.NavigateToCards -> onOpenCards()
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
        onCreditInstallmentClick = { viewModel.onEvent(HomeContract.UiEvent.CreditInstallmentClicked) },
        onCardsClick = { viewModel.onEvent(HomeContract.UiEvent.OpenCardsClicked) }
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeContract.UiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCreditInstallmentClick: () -> Unit,
    onCardsClick: () -> Unit
) {
    val selectedAccount = state.selectedAccount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
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
                    text = "Greska pri ucitavanju: ${state.errorMessage}",
                    color = Color(0xFFB3261E),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            selectedAccount != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    AccountSwitcher(
                        selectedAccount = selectedAccount,
                        onPrevious = onPrevious,
                        onNext = onNext
                    )

                    BalanceCircle(
                        account = selectedAccount,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )

                    ActionRow(
                        onCreditInstallmentClick = onCreditInstallmentClick,
                        onCardsClick = onCardsClick,
                        modifier = Modifier.padding(top = 14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
        }
    }
}

@Composable
private fun AccountSwitcher(
    selectedAccount: HomeContract.AccountItem,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Prethodni racun",
                    tint = GradientColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = selectedAccount.accountType,
                    style = MaterialTheme.typography.titleLarge,
                    color = GradientColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedAccount.accountNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5A5A5A)
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Sledeci racun",
                    tint = GradientColor
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = GradientColor.copy(alpha = 0.20f)
        )
    }
}

@Composable
private fun ActionRow(
    onCreditInstallmentClick: () -> Unit,
    onCardsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onCreditInstallmentClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE9F2FF),
                contentColor = GradientColor
            )
        ) {
            Text(
                text = "Rata za kredit: 100e",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        OutlinedButton(
            onClick = onCardsClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFFE9F2FF)),
            border = BorderStroke(0.dp, Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = "Kartice",
                tint = GradientColor
            )
        }
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
                text = "$amountPrefix${String.format(Locale.US, "%.2f", transaction.amount)} ${transaction.currency}",
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
            .size(308.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.30f to GradientColor.copy(alpha = 0.26f),
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
                    text = "stanje",
                    color = Color(0xFF7A7A7A),
                    fontWeight = FontWeight.Medium,
                    fontSize = 19.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format(Locale.US, "%.2f", account.balance),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = account.currency,
                    color = Color(0xFF7A7A7A),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
