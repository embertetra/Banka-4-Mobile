package rs.raf.banka4mobile.presentation.cards

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rs.raf.banka4mobile.presentation.cards.CardContract.UiEvent
import rs.raf.banka4mobile.presentation.components.AccountSwitcherHeader
import java.util.Locale

private val GradientColor = Color(0xFF005EAD)

@Composable
fun CardsScreen(
    viewModel: CardsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.onEvent(UiEvent.ScreenOpened)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            state.isLoading -> {
                LoadingState(modifier = Modifier.align(Alignment.Center))
            }

            state.errorMessage != null && state.accounts.isEmpty() -> {
                ErrorState(
                    message = state.errorMessage,
                    onRetry = { viewModel.onEvent(UiEvent.RetryClicked) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.selectedAccount != null -> {
                val selectedAccount = state.selectedAccount

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    AccountSwitcherHeader(
                        accountName = selectedAccount?.name ?: "",
                        accountNumber = selectedAccount?.accountNumber ?: "",
                        onPrevious = { viewModel.onEvent(UiEvent.PreviousAccountClicked) },
                        onNext = { viewModel.onEvent(UiEvent.NextAccountClicked) },
                        accentColor = GradientColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (state.errorMessage != null) {
                        ErrorState(
                            message = state.errorMessage,
                            onRetry = { viewModel.onEvent(UiEvent.RetryClicked) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp)
                        )
                    } else if (state.cards.isEmpty()) {
                        Text(
                            text = "Nema kartica na ovom racunu.",
                            color = Color(0xFF5A5A5A),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(items = state.cards, key = { it.id }) { card ->
                                BankCardItem(card = card)
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = GradientColor)
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Ucitavanje kartica...", color = Color(0xFF5A5A5A))
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = Color(0xFFB3261E),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(text = "Pokusaj ponovo")
        }
    }
}

@Composable
private fun BankCardItem(card: CardContract.CardItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "Kartica",
                        tint = GradientColor
                    )
                    Text(
                        text = card.holderName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1F1F1F),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = card.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = GradientColor,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 1.dp,
                color = GradientColor.copy(alpha = 0.20f)
            )

            InfoRow(label = "Broj kartice", value = card.cardNumberMasked)
            InfoRow(label = "Brend", value = card.cardBrand)
            InfoRow(label = "Tip", value = card.cardType)
            InfoRow(label = "Istek", value = card.expiresAt)
            InfoRow(label = "Limit", value = formatAmount(card.limit))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF3A3A3A)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1F1F1F),
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)
}