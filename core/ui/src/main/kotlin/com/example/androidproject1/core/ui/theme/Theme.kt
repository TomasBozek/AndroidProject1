package com.example.androidproject1.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * The theme every screen composes inside.
 *
 * Three layers, as the design system defines them: [Ramp] and [Scale] hold raw values and are
 * `internal`; the semantic layer ([AppColors], [AppTypography], [AppShapes], [AppElevation],
 * [AppMotion], [Spacing]) names roles and is the only layer that differs between light and dark;
 * a component binds to a role and to its own state.
 *
 * There is no dynamic colour. The palette is the product — a screen that takes its colours from the
 * wallpaper is a screen that never looks like itself, and the status roles stop meaning anything.
 *
 * @param sizeClass override the class derived from window width; useful in previews and tests.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    sizeClass: SizeClass = sizeClassFor(LocalConfiguration.current.screenWidthDp),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) darkAppColors() else lightAppColors()
    val typography = when (sizeClass) {
        SizeClass.Compact -> compactTypography()
        SizeClass.Regular, SizeClass.Expanded -> regularTypography()
    }
    val shapes = AppShapes()

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalAppShapes provides shapes,
        LocalAppElevation provides AppElevation(),
        LocalAppMotion provides AppMotion(),
        LocalAppDensity provides densityFor(sizeClass),
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = typography.toTypography(),
            shapes = shapes.toShapes(),
            content = content,
        )
    }
}

/**
 * This app's design tokens.
 *
 * `MaterialTheme.colorScheme` and `MaterialTheme.typography` are populated from the same roles, so
 * a stock Material component fits without a wrapper — but prefer these: they carry the roles
 * Material has no slot for, such as an action's edge colour or the status families.
 */
object AppTheme {

    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShapes.current

    val elevation: AppElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalAppElevation.current

    val motion: AppMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalAppMotion.current

    val density: AppDensity
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDensity.current

    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}
