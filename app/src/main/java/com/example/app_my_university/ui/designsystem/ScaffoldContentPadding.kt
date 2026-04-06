package com.example.app_my_university.ui.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Добавляет к верхнему inset от Scaffold единый [AppSpacing.belowTopAppBar], чтобы контент не «прилипал»
 * к верхней панели. Если верхний padding от Scaffold равен нулю (нет top bar / нет inset), ничего не меняется.
 */
@Composable
fun PaddingValues.withBelowTopBarContentGap(enabled: Boolean = true): PaddingValues {
    if (!enabled) return this
    val layoutDirection = LocalLayoutDirection.current
    val baseTop = calculateTopPadding()
    if (baseTop == 0.dp) return this
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = baseTop + AppSpacing.belowTopAppBar,
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding(),
    )
}
