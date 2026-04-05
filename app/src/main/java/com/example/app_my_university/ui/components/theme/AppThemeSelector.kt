package com.example.app_my_university.ui.components.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.app_my_university.data.theme.AppThemePreference
import com.example.app_my_university.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppThemeSelectorRow(
    selected: AppThemePreference,
    onSelect: (AppThemePreference) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceS),
    ) {
        ThemeChip(
            label = "Как в системе",
            icon = Icons.Outlined.SettingsBrightness,
            selected = selected == AppThemePreference.SYSTEM,
            onClick = { onSelect(AppThemePreference.SYSTEM) },
            enabled = enabled,
        )
        ThemeChip(
            label = "Светлая",
            icon = Icons.Outlined.LightMode,
            selected = selected == AppThemePreference.LIGHT,
            onClick = { onSelect(AppThemePreference.LIGHT) },
            enabled = enabled,
        )
        ThemeChip(
            label = "Тёмная",
            icon = Icons.Outlined.DarkMode,
            selected = selected == AppThemePreference.DARK,
            onClick = { onSelect(AppThemePreference.DARK) },
            enabled = enabled,
        )
    }
}

@Composable
private fun ThemeChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}
