package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components

import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.xlopec.tea.time.travel.plugin.domain.Validated
import io.github.xlopec.tea.time.travel.plugin.domain.isValid

@Composable
fun ValidatedTextField(
    validated: Validated<*>,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onValueChange: (newValue: String) -> Unit,
    enabled: Boolean = true,
) {
    TextField(
        value = validated.input,
        modifier = modifier,
        enabled = enabled,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        isError = !validated.isValid(),
        singleLine = true,
        onValueChange = onValueChange
    )
}
