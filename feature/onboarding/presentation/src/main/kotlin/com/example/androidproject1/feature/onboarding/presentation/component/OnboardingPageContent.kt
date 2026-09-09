package com.example.androidproject1.feature.onboarding.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.text.resolve
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.onboarding.presentation.onboarding.OnboardingPage

/**
 * One page of the first-run tour: a heading and a sentence, centred.
 *
 * Private to this feature — a tour page is not a shape another screen reaches for, so it lives
 * here rather than in `:core:ui`. It composes `:core:ui` components and reads roles from
 * `AppTheme`, exactly as a screen does.
 */
@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.inset.xl),
        verticalArrangement = Arrangement.spacedBy(
            AppTheme.spacing.stack.md,
            Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppText(
            text = page.title.resolve(),
            role = TextRole.Title,
        )
        AppText(
            text = page.body.resolve(),
            role = TextRole.Body,
            color = AppTheme.colors.textSecondary,
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    OnboardingPageContent(
        page = OnboardingPage(
            title = "Everything in one place".toUiText(),
            body = "A sentence long enough to wrap, because that is what these always do.".toUiText(),
        ),
    )
}
