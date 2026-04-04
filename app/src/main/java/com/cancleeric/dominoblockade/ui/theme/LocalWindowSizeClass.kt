package com.cancleeric.dominoblockade.ui.theme

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.compositionLocalOf

/**
 * Composition local that provides the current [WindowSizeClass].
 * Defaults to `null`, which is treated as [WindowWidthSizeClass.Compact].
 *
 * Provide this in [MainActivity] via [androidx.compose.runtime.CompositionLocalProvider].
 */
val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass?> { null }

/** Returns `true` when the window is compact (phone portrait) or when no size class is provided. */
val WindowSizeClass?.widthIsCompact: Boolean
    get() = this == null || widthSizeClass == WindowWidthSizeClass.Compact

/** Returns `true` for medium (foldable / landscape phone) or expanded (tablet) widths. */
val WindowSizeClass?.widthIsMediumOrExpanded: Boolean
    get() = this != null &&
        (widthSizeClass == WindowWidthSizeClass.Medium || widthSizeClass == WindowWidthSizeClass.Expanded)
