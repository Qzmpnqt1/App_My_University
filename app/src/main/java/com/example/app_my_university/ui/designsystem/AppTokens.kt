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

object AppNav {
    /** Нижняя панель: достаточная высота, чтобы не обрезать иконки сверху. */
    val bottomBarMinHeight = 68.dp
    val bottomIconSize = 24.dp
}

object AppBar {
    /** Единая минимальная высота контента top bar (без status bar). */
    val minHeight = 64.dp
    val titleSp = 20f
}
