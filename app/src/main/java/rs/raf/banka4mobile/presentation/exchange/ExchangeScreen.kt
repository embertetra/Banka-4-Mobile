package rs.raf.banka4mobile.presentation.exchange

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rs.raf.banka4mobile.presentation.exchange.ExchangeContract.UiEvent

private val PrimaryBlue = Color(0xFF270071)
private val AccentBlue = Color(0xFF2E5BDB)
private val ScreenBackground = Color(0xFFF5F7FC)
private val CardBorder = Color(0xFFE3E8F3)
private val SoftText = Color(0xFF6B7280)
private val PositiveBlueBg = Color(0xFFEAF1FF)
private val SuccessGreenBg = Color(0xFFEAF8EE)
private val SuccessGreenBorder = Color(0xFFBFE3C9)
private val SuccessGreenText = Color(0xFF1E7A3E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    onBack: () -> Unit,
    viewModel: ExchangeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(UiEvent.ScreenOpened)
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text("Menjačnica", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Nazad",
                            tint = PrimaryBlue
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
            state.isLoading && state.rates.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }

            state.errorMessage != null && state.rates.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "Greška",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ExchangeRatesCard(
                        rates = state.rates,
                        updatedAtText = state.updatedAtText,
                        nextUpdateText = state.nextUpdateText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExchangeCalculatorCard(
                        amountInput = state.amountInput,
                        fromCurrency = state.fromCurrency,
                        toCurrency = state.toCurrency,
                        resultText = state.convertedAmountText,
                        helperText = state.helperText,
                        currencies = buildList {
                            add("RSD")
                            addAll(state.rates.map { it.currencyCode })
                        }.distinct(),
                        onAmountChanged = { viewModel.onEvent(UiEvent.AmountChanged(it)) },
                        onFromCurrencyChanged = { viewModel.onEvent(UiEvent.FromCurrencyChanged(it)) },
                        onToCurrencyChanged = { viewModel.onEvent(UiEvent.ToCurrencyChanged(it)) },
                        onSwapClicked = { viewModel.onEvent(UiEvent.SwapCurrenciesClicked) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExchangePurchaseCard(
                        buyAmountInput = state.buyAmountInput,
                        buyFromCurrency = state.buyFromCurrency,
                        buyToCurrency = state.buyToCurrency,
                        previewText = state.buyPreviewText,
                        successMessage = state.successMessage,
                        isBuying = state.isBuying,
                        currencies = buildList {
                            add("RSD")
                            addAll(state.rates.map { it.currencyCode })
                        }.distinct(),
                        onBuyAmountChanged = { viewModel.onEvent(UiEvent.BuyAmountChanged(it)) },
                        onBuyFromCurrencyChanged = { viewModel.onEvent(UiEvent.BuyFromCurrencyChanged(it)) },
                        onBuyToCurrencyChanged = { viewModel.onEvent(UiEvent.BuyToCurrencyChanged(it)) },
                        onPreviewClicked = { viewModel.onEvent(UiEvent.PreviewPurchaseClicked) },
                        onConfirmClicked = { viewModel.onEvent(UiEvent.ConfirmPurchaseClicked) }
                    )

                    if (!state.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ExchangeRatesCard(
    rates: List<ExchangeRateUiModel>,
    updatedAtText: String,
    nextUpdateText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kursna lista",
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (updatedAtText.isNotBlank()) {
                Text(
                    text = "Poslednje ažuriranje: $updatedAtText",
                    color = SoftText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (nextUpdateText.isNotBlank()) {
                Text(
                    text = "Sledeće ažuriranje: $nextUpdateText",
                    color = SoftText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            RatesHeaderRow()

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = CardBorder
            )

            rates.forEachIndexed { index, rate ->
                RateRow(rate = rate)

                if (index != rates.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = CardBorder
                    )
                }
            }
        }
    }
}

@Composable
private fun RatesHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderCell("Valuta", Modifier.weight(1.2f))
        HeaderCell("Kupovni", Modifier.weight(1f))
        HeaderCell("Srednji", Modifier.weight(1f))
        HeaderCell("Prodajni", Modifier.weight(1f))
    }
}

@Composable
private fun RateRow(rate: ExchangeRateUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = rate.currencyCode,
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = rate.currencyName,
                color = SoftText,
                style = MaterialTheme.typography.bodySmall
            )
        }

        ValueCell(text = "${formatRate(rate.buyRate)} RSD", modifier = Modifier.weight(1f))
        ValueCell(text = "${formatRate(rate.middleRate)} RSD", modifier = Modifier.weight(1f))
        ValueCell(text = "${formatRate(rate.sellRate)} RSD", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = SoftText,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ValueCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = Color(0xFF1F2937),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangeCalculatorCard(
    amountInput: String,
    fromCurrency: String,
    toCurrency: String,
    resultText: String,
    helperText: String,
    currencies: List<String>,
    onAmountChanged: (String) -> Unit,
    onFromCurrencyChanged: (String) -> Unit,
    onToCurrencyChanged: (String) -> Unit,
    onSwapClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kalkulator valuta",
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Preračun kursa koristeći najnovije dostupne vrednosti.",
                color = SoftText,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Iznos",
                color = SoftText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Unesite iznos") },
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = AccentBlue,
                    unfocusedIndicatorColor = CardBorder
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Iz valute",
                        color = SoftText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CurrencyDropdown(
                        selectedValue = fromCurrency,
                        values = currencies,
                        onValueSelected = onFromCurrencyChanged
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onSwapClicked,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PositiveBlueBg,
                        contentColor = AccentBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Zameni valute"
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "U valutu",
                        color = SoftText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CurrencyDropdown(
                        selectedValue = toCurrency,
                        values = currencies,
                        onValueSelected = onToCurrencyChanged
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PositiveBlueBg,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFD8E5FF),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = resultText,
                        color = AccentBlue,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (helperText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = helperText,
                            color = SoftText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangePurchaseCard(
    buyAmountInput: String,
    buyFromCurrency: String,
    buyToCurrency: String,
    previewText: String,
    successMessage: String?,
    isBuying: Boolean,
    currencies: List<String>,
    onBuyAmountChanged: (String) -> Unit,
    onBuyFromCurrencyChanged: (String) -> Unit,
    onBuyToCurrencyChanged: (String) -> Unit,
    onPreviewClicked: () -> Unit,
    onConfirmClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kupovina valute",
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Unesite iznos i odaberite valute za simulaciju ili izvršavanje kupovine.",
                color = SoftText,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Iznos za kupovinu",
                color = SoftText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = buyAmountInput,
                onValueChange = onBuyAmountChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Unesite iznos") },
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = AccentBlue,
                    unfocusedIndicatorColor = CardBorder
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Plaćate iz",
                        color = SoftText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CurrencyDropdown(
                        selectedValue = buyFromCurrency,
                        values = currencies,
                        onValueSelected = onBuyFromCurrencyChanged
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kupujete",
                        color = SoftText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CurrencyDropdown(
                        selectedValue = buyToCurrency,
                        values = currencies,
                        onValueSelected = onBuyToCurrencyChanged
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PositiveBlueBg,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFD8E5FF),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = previewText,
                        color = AccentBlue,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Procena koliko biste dobili",
                        color = SoftText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!successMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = SuccessGreenBg,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = SuccessGreenBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = successMessage,
                        color = SuccessGreenText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPreviewClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PositiveBlueBg,
                        contentColor = AccentBlue
                    )
                ) {
                    Text(
                        text = "Prikaži izračun",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onConfirmClicked,
                    enabled = !isBuying,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (isBuying) {
                        CircularProgressIndicator(
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Kupi valutu",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    selectedValue: String,
    values: List<String>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = AccentBlue,
                unfocusedIndicatorColor = CardBorder
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            values.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatRate(value: Double): String {
    return if (value < 1) {
        String.format("%.4f", value)
    } else {
        String.format("%.2f", value)
    }
}