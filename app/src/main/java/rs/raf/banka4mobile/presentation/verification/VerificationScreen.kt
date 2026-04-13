package rs.raf.banka4mobile.presentation.verification

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val GradientColor = Color(0xFF005EAD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timerText = "%02d:%02d".format(state.secondsLeft / 60, state.secondsLeft % 60)

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            if (sideEffect is VerificationContract.SideEffect.ShowToast) {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 140.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timerText,
                        color = GradientColor,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Vreme do generisanja novog koda",
                        color = GradientColor.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = GradientColor
                        )
                    }

                    state.error != null -> {
                        Text(
                            text = state.error?.message ?: "Greška pri učitavanju koda",
                            color = Color(0xFFB3261E),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        TotpDigitsRow(
                            totp = state.totp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TotpDigitsRow(
    totp: String,
    modifier: Modifier = Modifier
) {
    val digits = totp.padStart(6, '0').take(6).toCharArray()

    val digitGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF013F74),
            Color(0xFF017ADC)
        )
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        digits.forEach { digit ->
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(
                        brush = digitGradient,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = digit.toString(),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}