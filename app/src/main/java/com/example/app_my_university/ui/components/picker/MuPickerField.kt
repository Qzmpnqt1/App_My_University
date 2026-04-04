package com.example.app_my_university.ui.components.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
/**
 * Поле только для выбора: по тапу открывается sheet / диалог с поиском.
 */
@Composable
fun MuPickerField(
    label: String,
    valueText: String,
    placeholder: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = valueText,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = { Text(label) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge) },
        trailingIcon = {
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        },
        supportingText = supportingText,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
