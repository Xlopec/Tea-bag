package io.github.xlopec.reader.app.ui.misc

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun InsetsAwareBottomNavigation(
    modifier: Modifier = Modifier,
    elevation: Dp = 1.dp,
    background: Color = MaterialTheme.colors.surface,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        elevation = elevation,
        color = background,
    ) {
        BottomNavigation(
            modifier = Modifier.navigationBarsPadding(),
            elevation = 0.dp,
            backgroundColor = Color.Unspecified,
            contentColor = contentColorFor(background),
            content = content
        )
    }
}
