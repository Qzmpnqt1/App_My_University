package com.example.app_my_university.ui.components.picker

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/**
 * Поле только для выбора: по тапу открывается sheet / диалог с поиском.
 *
 * [OutlinedTextField] сам перехватывает касания по области ввода — из‑за этого тап «по плейсхолдеру»
 * часто не доходит до [Modifier.clickable] на том же модификаторе. Накладываем прозрачный слой
 * на весь размер поля, который получает все нажатия, когда [enabled] = true.
 *
 * Оверлей измеряется через [Modifier.matchParentSize] в [BoxScope]: при родителе в [verticalScroll]/
 * [Column] с неограниченной высотой [androidx.compose.foundation.layout.fillMaxSize] на оверлее даёт
 * неверный hitbox (часто только верхняя полоса поля).
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
    Box(modifier.fillMaxWidth()) {
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
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        if (enabled) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        role = Role.Button,
                    ) { onClick() },
            )
        }
    }
}
