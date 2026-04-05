package com.example.app_my_university.ui.designsystem

import androidx.compose.ui.unit.dp
import com.example.app_my_university.ui.theme.Dimens

/**
 * Централизованные токены дизайн-системы «Мой ВУЗ».
 * Используйте вместо разрозненных magic numbers на экранах.
 */
object AppSpacing {
    val xxs = Dimens.spaceXXS
    val xs = Dimens.spaceXS
    val s = Dimens.spaceS
    val m = Dimens.spaceM
    val l = Dimens.spaceL
    val xl = Dimens.spaceXL
    /** Горизонтальные отступы корневого контента экранов. */
    val screen = Dimens.screenPadding
    val card = Dimens.cardPadding
    val listItem = Dimens.listItemSpacing
}

object AppRadius {
    val field = 12.dp
    val card = 16.dp
    val chip = 8.dp
    val sheet = 16.dp
}

/**
 * Единый источник размеров для верхней и нижней навигационных панелей и их иконок.
 */
object AppLayout {
    val topAppBarMinHeight = 64.dp
    val bottomNavigationMinHeight = 68.dp
    val barIconSize = 24.dp
    val titleSp = 20f
    /** Нижние action-панели (ввод в чате, кнопки в sheet): горизонтальный отступ. */
    val bottomActionBarHorizontalPadding = AppSpacing.m
    val bottomActionBarVerticalPadding = AppSpacing.s
    val bottomActionBarTonalElevation = 3.dp
    val bottomActionBarDividerElevation = 2.dp
}

/** @deprecated Используйте [AppLayout]. */
@Deprecated("Используйте AppLayout", ReplaceWith("AppLayout.bottomNavigationMinHeight"))
object AppNav {
    val bottomBarMinHeight get() = AppLayout.bottomNavigationMinHeight
    val bottomIconSize get() = AppLayout.barIconSize
}

/** @deprecated Используйте [AppLayout]. */
@Deprecated("Используйте AppLayout", ReplaceWith("AppLayout.topAppBarMinHeight"))
object AppBar {
    val minHeight get() = AppLayout.topAppBarMinHeight
    val titleSp get() = AppLayout.titleSp
}
