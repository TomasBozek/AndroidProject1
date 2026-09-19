package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** What a banner is saying. The default is [Warning], which is what "you're offline" is. */
enum class BannerTone { Info, Success, Warning, Error }

/**
 * One line, full width, in a feedback role (D78), drawn above the screen rather than over it: a
 * fact about the whole app that stays true until it stops — the device is offline.
 *
 * Not a toast (D63): a toast reports something that happened and goes away; a banner reports
 * something that is so, for as long as it is so, and has no action of its own. What to do about
 * it is the screen's business. `MainActivity` draws one above `AppNavHost` while
 * `MainViewModel.online` is false (D68).
 */
@Composable
fun AppBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.Warning,
) {
    val feedback = AppTheme.colors.feedback
    val family = when (tone) {
        BannerTone.Info -> feedback.info
        BannerTone.Success -> feedback.success
        BannerTone.Warning -> feedback.warning
        BannerTone.Error -> feedback.error
    }
    AppText(
        text = text,
        role = TextRole.Label,
        color = family.onContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .background(family.container)
            .padding(
                horizontal = AppTheme.spacing.inset.lg,
                vertical = AppTheme.spacing.inset.sm,
            ),
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm)) {
        AppBanner(text = "You're offline")
        AppBanner(text = "Synced a minute ago", tone = BannerTone.Info)
        AppBanner(text = "All receipts printed", tone = BannerTone.Success)
        AppBanner(text = "The printer is out of paper", tone = BannerTone.Error)
    }
}
