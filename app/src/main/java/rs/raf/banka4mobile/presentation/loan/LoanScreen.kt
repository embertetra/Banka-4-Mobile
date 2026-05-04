package rs.raf.banka4mobile.presentation.loan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val ScreenBackground = Color(0xFFF5F7FC)
private val AccentBlue = Color(0xFF005EAD)
private val CardBackground = Color(0xFFF8FAFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanScreen(
	onBack: () -> Unit,
	viewModel: LoanViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsStateWithLifecycle()

	LaunchedEffect(Unit) {
		viewModel.onEvent(LoanContract.UiEvent.ScreenOpened)
	}

	Scaffold(
		containerColor = ScreenBackground,
		topBar = {
			TopAppBar(
				title = {
					Text(
						text = "Krediti i obaveze",
						color = AccentBlue,
						fontWeight = FontWeight.Bold
					)
				},
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "Nazad",
							tint = AccentBlue
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = ScreenBackground
				)
			)
		}
	) { paddingValues ->
		when {
			state.isLoading && state.loans.isEmpty() -> {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator(color = AccentBlue)
				}
			}

			state.errorMessage != null && state.loans.isEmpty() -> {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues)
						.padding(24.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = state.errorMessage ?: "Greška",
						color = MaterialTheme.colorScheme.error,
						textAlign = androidx.compose.ui.text.style.TextAlign.Center
					)
				}
			}

			state.loans.isEmpty() -> {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues)
						.padding(horizontal = 24.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = "Nema aktivnih rata za prikaz.",
						color = Color(0xFF5A5A5A),
						textAlign = androidx.compose.ui.text.style.TextAlign.Center
					)
				}
			}

			else -> {
				LazyColumn(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues),
					contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					itemsIndexed(
						items = state.loans,
						key = { _, loan -> loan.id }
					) { index, loan ->
						LoanCard(loan = loan)

						if (index < state.loans.lastIndex) {
							HorizontalDivider(
								modifier = Modifier.padding(top = 4.dp),
								thickness = 1.dp,
								color = AccentBlue.copy(alpha = 0.18f)
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun LoanCard(loan: LoanContract.LoanItem) {
	Card(
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(containerColor = CardBackground),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			LoanInfoRow(label = "ID", value = loan.id.toString())
			LoanInfoRow(label = "Iznos", value = formatAmount(loan.amount) + " ${loan.currency}")
			LoanInfoRow(label = "Tip kredita", value = loan.loanType)
			LoanInfoRow(label = "Mesecna rata", value = formatAmount(loan.monthlyInstallment) + " ${loan.currency}")
			LoanInfoRow(label = "Status", value = loan.status)
		}
	}
}

@Composable
private fun LoanInfoRow(label: String, value: String) {
	androidx.compose.foundation.layout.Row(
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
	return String.format(java.util.Locale.US, "%,.2f", amount)
}
