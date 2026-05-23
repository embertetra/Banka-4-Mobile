package rs.raf.banka4mobile.presentation.profile

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rs.raf.banka4mobile.data.local.settings.AppThemeOption
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileContract.UiEvent.ScreenOpened)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                ProfileContract.SideEffect.NavigateToLogin -> onLogoutSuccess()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.errorMessage != null && state.profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "Greška",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                val profile = state.profile

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    ProfileInfoCard(profile)

                    ThemeSelectorCard(
                        selectedTheme = state.selectedTheme,
                        onThemeSelected = { theme ->
                            viewModel.onEvent(
                                ProfileContract.UiEvent.ThemeChanged(theme)
                            )
                        }
                    )

                    MonthlyBalanceChartCard(
                        items = state.monthlyBalance
                    )

                    DailySpendingChartCard(
                        items = state.dailySpending
                    )

                    Button(
                        onClick = {
                            viewModel.onEvent(
                                ProfileContract.UiEvent.LogoutClicked
                            )
                        },

                        modifier = Modifier.fillMaxWidth(),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),

                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Text(
                            text = "Odjavi se",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSelectorCard(
    selectedTheme: AppThemeOption,
    onThemeSelected: (AppThemeOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Tema aplikacije",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeIconOption(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LightMode,
                    selected = selectedTheme == AppThemeOption.LIGHT,
                    iconColor = Color(0xFFFFB300),
                    onClick = {
                        onThemeSelected(AppThemeOption.LIGHT)
                    }
                )

                ThemeIconOption(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    selected = selectedTheme == AppThemeOption.SYSTEM,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        onThemeSelected(AppThemeOption.SYSTEM)
                    }
                )

                ThemeIconOption(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DarkMode,
                    selected = selectedTheme == AppThemeOption.DARK,
                    iconColor = Color(0xFF1F2A55),
                    onClick = {
                        onThemeSelected(AppThemeOption.DARK)
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeIconOption(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    selected: Boolean,
    iconColor: Color,
    onClick: () -> Unit
) {

    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }

    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick),

        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(46.dp)
        )
    }
}

@Composable
private fun ProfileInfoCard(
    profile: ProfileUiModel?
) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),

        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = profile?.fullName ?: "",

                style = MaterialTheme.typography.headlineSmall,

                color = MaterialTheme.colorScheme.primary,

                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Pregled naloga i osnovnih korisničkih informacija",

                style = MaterialTheme.typography.bodyMedium,

                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            ProfileInfoRow(
                label = "Email",
                value = profile?.email.orEmpty()
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp)
            )

            ProfileInfoRow(
                label = "Korisničko ime",
                value = profile?.username.orEmpty()
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp)
            )

            ProfileInfoRow(
                label = "Tip korisnika",
                value = profile?.identityType.orEmpty()
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value.ifBlank { "-" },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MonthlyBalanceChartCard(
    items: List<MonthlyBalanceItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Mesečni pregled stanja",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Prikaz priliva i odliva po mesecima",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            MonthlyBalanceBars(items = items)
        }
    }
}

@Composable
private fun MonthlyBalanceBars(
    items: List<MonthlyBalanceItem>
) {
    val maxValue = items.maxOfOrNull {
        kotlin.math.max(it.income, it.outcome)
    }?.takeIf { it > 0.0 } ?: 1.0

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            val incomeWeight = (item.income / maxValue).toFloat().coerceIn(0f, 1f)
            val outcomeWeight = (item.outcome / maxValue).toFloat().coerceIn(0f, 1f)

            Column {
                Text(
                    text = item.monthLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                BalanceBar(
                    label = "Priliv",
                    value = item.income,
                    weight = incomeWeight
                )

                Spacer(modifier = Modifier.height(4.dp))

                BalanceBar(
                    label = "Odliv",
                    value = item.outcome,
                    weight = outcomeWeight
                )
            }
        }
    }
}

@Composable
private fun BalanceBar(
    label: String,
    value: Double,
    weight: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatProfileAmount(value),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(weight)
                    .height(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
private fun DailySpendingChartCard(
    items: List<DailySpendingItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Dnevna potrošnja",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Potrošnja po danima u trenutnom mesecu",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            DailySpendingLineChart(items = items)
        }
    }
}

@Composable
private fun DailySpendingLineChart(
    items: List<DailySpendingItem>
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    val safeItems = if (items.size >= 2) items else listOf(
        DailySpendingItem(day = 1, amount = 0.0),
        DailySpendingItem(day = 2, amount = 0.0)
    )

    val maxAmount = safeItems.maxOfOrNull { it.amount }
        ?.takeIf { it > 0.0 }
        ?: 1.0

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val leftPadding = 46f
        val bottomPadding = 34f
        val topPadding = 18f
        val rightPadding = 12f

        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding

        val xAxisY = topPadding + chartHeight
        val yAxisX = leftPadding

        drawLine(
            color = axisColor,
            start = Offset(yAxisX, topPadding),
            end = Offset(yAxisX, xAxisY),
            strokeWidth = 2f
        )

        drawLine(
            color = axisColor,
            start = Offset(yAxisX, xAxisY),
            end = Offset(size.width - rightPadding, xAxisY),
            strokeWidth = 2f
        )

        val points = safeItems.mapIndexed { index, item ->
            val x = leftPadding + index * (chartWidth / (safeItems.size - 1))
            val y = xAxisY - ((item.amount / maxAmount).toFloat() * chartHeight)
            Offset(x, y)
        }

        points.zipWithNext().forEach { pair ->
            drawLine(
                color = primaryColor,
                start = pair.first,
                end = pair.second,
                strokeWidth = 4f
            )
        }

        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 5f,
                center = point
            )
        }

        val paint = Paint().apply {
            color = textColor.toArgb()
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val labelDays = listOf(1, 5, 10, 15, 20, 25, safeItems.last().day)
            .distinct()
            .filter { it in 1..safeItems.last().day }

        labelDays.forEach { day ->
            val index = day - 1
            val x = leftPadding + index * (chartWidth / (safeItems.size - 1))

            drawContext.canvas.nativeCanvas.drawText(
                day.toString(),
                x,
                size.height - 6f,
                paint
            )
        }
    }
}

private fun formatProfileAmount(value: Double): String {
    return String.format(Locale.US, "%,.2f", value)
}