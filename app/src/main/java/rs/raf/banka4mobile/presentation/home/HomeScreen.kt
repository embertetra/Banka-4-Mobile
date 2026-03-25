package rs.raf.banka4mobile.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rs.raf.banka4mobile.presentation.home.HomeContract.SideEffect
import rs.raf.banka4mobile.presentation.home.HomeContract.UiEvent


private val GradientColor = Color(0xFF270071)

@Composable
fun HomeScreen(
    onOpenVerification: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.onEvent(UiEvent.ScreenOpened)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            if (sideEffect is SideEffect.NavigateToVerification) {
                onOpenVerification()
            }
        }
    }

    HomeScreenContent(
        state = state,
        onOpenVerification = { viewModel.onEvent(UiEvent.OpenVerificationClicked) },
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeContract.UiState,
    onOpenVerification: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(color = GradientColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Učitavanje...",
                    color = Color(0xFF5A5A5A)
                )
            }

            state.errorMessage != null -> {
                Text(
                    text = "Greška pri učitavanju: ${state.errorMessage}",
                    color = Color(0xFFB3261E)
                )
            }

            else -> {
                Button(
                    onClick = { onOpenVerification() },
                    interactionSource = interactionSource,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPressed) Color(0xFF3B1291) else GradientColor,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Text(text = "Prikaži kod")
                }
            }
        }
    }
}