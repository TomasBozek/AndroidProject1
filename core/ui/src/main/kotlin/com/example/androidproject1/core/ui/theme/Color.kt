package com.example.androidproject1.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * One action family, in the four parts a pressable surface needs.
 *
 * [edge] is the key's fixed bottom edge rather than a blurred shadow — see [Elevation]. [container]
 * and [onContainer] are the soft pair a tag or a banner uses, where the family has to read as a
 * tint rather than as a button.
 */
@Immutable
data class ActionColors(
    val bg: Color,
    val edge: Color,
    val label: Color,
    val container: Color,
    val onContainer: Color,
)

/**
 * Layer 2 — semantic. Roles, not values: this is the only layer that differs between light and
 * dark, and the names stay identical across the two.
 *
 * Read it through [AppTheme.colors]. A screen asks for `colors.confirm.bg`, never for a ramp step
 * and never for a hex — so a re-brand is a change to [Ramp] and to the two factories below, and no
 * screen is touched. Status is the one place where colour carries meaning on its own, which is why
 * a status colour is always accompanied by text.
 */
@Immutable
data class AppColors(
    /** Under a keypad or a list — the ground the base surface sits on. */
    val surfaceSunken: Color,
    /** The screen itself. */
    val surfaceBase: Color,
    /** Card, key, dialog. */
    val surfaceRaised: Color,
    /** Tooltip and the contextual toolbar: a different mode, so it inverts. */
    val surfaceInverse: Color,
    val textOnInverse: Color,
    /** Names, amounts, values. */
    val textPrimary: Color,
    /** Labels and metadata. */
    val textSecondary: Color,
    /**
     * A missing value — the dash in a table, never an empty cell and never a zero.
     *
     * It measures 4.3:1 light and 3.8:1 dark, under the 4.5:1 threshold for body text, which is
     * why it is scoped to a mark that carries no information of its own: the row's label is what
     * says what is missing. Anything a person actually reads uses [textSecondary], including field
     * placeholders — they were on this role until 3.6 measured them.
     */
    val textTertiary: Color,
    val textDisabled: Color,
    /** Pay, confirm, OK. */
    val confirm: ActionColors,
    /** Delete, remove, discard. */
    val destructive: ActionColors,
    /** Card, selection, information. */
    val info: ActionColors,
    /** In progress, warning. */
    val warning: ActionColors,
    /** Number keys and secondary actions. */
    val neutral: ActionColors,
    /** Hairline between rows — decorative, so it is allowed to be quiet. */
    val border: Color,
    /**
     * The edge of something you can press or type into. Held at 3:1 against every surface in both
     * themes — including [surfaceRaised], which is the one a field inside a card or a dialog sits
     * on, and the one a border a step away from the surface loses. `ContrastTest` asserts it.
     */
    val borderStrong: Color,
    /** Keyboard focus, on every platform. Never removed — a desktop is driven by keyboard too. */
    val focusRing: Color,
    val scrim: Color,
    /** How far the scrim dims what is under an overlay. */
    val scrimAlpha: Float,
    /**
     * Text and icons *on* the scrim — the loading overlay's wording and its spinner.
     *
     * The one role that does not flip between the themes, and the reason it exists: the scrim is a
     * dark wash in both, so what sits on it is light in both. [textOnInverse] is the role this
     * looks like and it is the wrong one — it follows the inverse *surface*, which does flip, and
     * in dark mode that puts near-black text on a near-black wash.
     */
    val textOnScrim: Color,
    val isLight: Boolean,
) {
    /** Status. The three are aliases, so a status row and a button cannot drift apart. */
    val statusPositive: ActionColors get() = confirm
    val statusWarning: ActionColors get() = warning
    val statusNegative: ActionColors get() = destructive
}

/**
 * Light. A solid action is `bg` at ramp step 500 with a 700 edge on a white label; the soft pair is
 * 100 on 700.
 */
fun lightAppColors(): AppColors = AppColors(
    surfaceSunken = Ramp.Gray100,
    surfaceBase = Ramp.Gray50,
    surfaceRaised = Ramp.White,
    surfaceInverse = Ramp.Gray900,
    textOnInverse = Ramp.Gray50,
    textPrimary = Ramp.Gray900,
    textSecondary = Ramp.Gray600,
    textTertiary = Ramp.Gray500,
    textDisabled = Ramp.Gray400,
    confirm = ActionColors(Ramp.Green500, Ramp.Green700, Ramp.White, Ramp.Green100, Ramp.Green700),
    destructive = ActionColors(Ramp.Red500, Ramp.Red700, Ramp.White, Ramp.Red100, Ramp.Red700),
    info = ActionColors(Ramp.Blue500, Ramp.Blue700, Ramp.White, Ramp.Blue100, Ramp.Blue700),
    warning = ActionColors(Ramp.Amber500, Ramp.Amber700, Ramp.White, Ramp.Amber100, Ramp.Amber700),
    neutral = ActionColors(Ramp.White, Ramp.Gray300, Ramp.Gray900, Ramp.Gray100, Ramp.Gray600),
    border = Ramp.Gray200,
    borderStrong = Ramp.Gray500,
    focusRing = Ramp.Blue500,
    scrim = Ramp.Gray950,
    scrimAlpha = 0.45f,
    textOnScrim = Ramp.Gray50,
    isLight = true,
)

/**
 * Dark. A role is not re-picked by hand — it moves along its ramp against the light theme, so green
 * still reads as green on a dark ground. A solid action is 400 on a 600 edge with a 900 label; the
 * soft pair is 800 on 200.
 *
 * The greys are the exception, and `ContrastTest` is why: the three dark surfaces sit far closer
 * together in luminance than the three light ones, so a role mirrored step for step lands short on
 * [surfaceRaised]. [textSecondary] and [borderStrong] are one step lighter than the mirror would
 * give — which is where Material's own dark `onSurfaceVariant` and `outline` sit too.
 */
fun darkAppColors(): AppColors = AppColors(
    surfaceSunken = Ramp.Gray950,
    surfaceBase = Ramp.Gray900,
    surfaceRaised = Ramp.DarkRaised,
    surfaceInverse = Ramp.Gray50,
    textOnInverse = Ramp.Gray900,
    textPrimary = Ramp.Gray50,
    textSecondary = Ramp.Gray300,
    textTertiary = Ramp.Gray500,
    textDisabled = Ramp.Gray500,
    confirm = ActionColors(Ramp.Green400, Ramp.Green600, Ramp.Green900, Ramp.Green800, Ramp.Green200),
    destructive = ActionColors(Ramp.Red400, Ramp.Red600, Ramp.Red900, Ramp.Red800, Ramp.Red200),
    info = ActionColors(Ramp.Blue400, Ramp.Blue600, Ramp.Blue900, Ramp.Blue800, Ramp.Blue200),
    warning = ActionColors(Ramp.Amber400, Ramp.Amber600, Ramp.Amber900, Ramp.Amber800, Ramp.Amber200),
    neutral = ActionColors(Ramp.DarkRaised, Ramp.Gray950, Ramp.Gray50, Ramp.Gray800, Ramp.Gray300),
    border = Ramp.Gray700,
    borderStrong = Ramp.Gray400,
    focusRing = Ramp.Blue400,
    scrim = Ramp.Gray950,
    scrimAlpha = 0.60f,
    textOnScrim = Ramp.Gray50,
    isLight = false,
)

/**
 * The same roles, spelled as Material's own scheme, so `Button`, `TextField` and the rest land on
 * this palette without a wrapper.
 *
 * `primary` is the affirmative action rather than the brand: in this system the colour that means
 * "this is the thing to press" is green, and red is reserved for what cannot be undone. That keeps
 * `error` distinct from `primary`, which a brand-as-primary mapping would not.
 */
fun AppColors.toColorScheme(): ColorScheme {
    val base = if (isLight) lightColorScheme() else darkColorScheme()
    return base.copy(
        primary = confirm.bg,
        onPrimary = confirm.label,
        primaryContainer = confirm.container,
        onPrimaryContainer = confirm.onContainer,
        secondary = info.bg,
        onSecondary = info.label,
        secondaryContainer = info.container,
        onSecondaryContainer = info.onContainer,
        tertiary = warning.bg,
        onTertiary = warning.label,
        tertiaryContainer = warning.container,
        onTertiaryContainer = warning.onContainer,
        error = destructive.bg,
        onError = destructive.label,
        errorContainer = destructive.container,
        onErrorContainer = destructive.onContainer,
        background = surfaceBase,
        onBackground = textPrimary,
        surface = surfaceBase,
        onSurface = textPrimary,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = textSecondary,
        surfaceContainerLowest = surfaceSunken,
        surfaceContainerLow = surfaceBase,
        surfaceContainer = surfaceRaised,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerHighest = surfaceRaised,
        surfaceDim = surfaceSunken,
        surfaceBright = surfaceRaised,
        inverseSurface = surfaceInverse,
        inverseOnSurface = textOnInverse,
        outline = borderStrong,
        outlineVariant = border,
        scrim = scrim,
    )
}

internal val LocalAppColors = staticCompositionLocalOf { lightAppColors() }
