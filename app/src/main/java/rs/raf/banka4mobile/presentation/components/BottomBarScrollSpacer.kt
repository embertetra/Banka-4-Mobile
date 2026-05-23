package rs.raf.banka4mobile.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val BottomBarBodyHeight = 64.dp

@Composable
fun BottomBarScrollSpacer(
    modifier: Modifier = Modifier,
    barHeight: Dp = BottomBarBodyHeight
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(barHeight))
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        Spacer(modifier = Modifier.height(barHeight))
    }
}

