package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The base of every list in the app.
 *
 * Its height is the density's row height — 56 dp on touch — so a row is a comfortable target
 * without anything inside it having to grow. [trailing] is where a tag or a value goes.
 */
@Composable
fun AppListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .defaultMinSize(minHeight = AppTheme.density.listRowHeight)
            .padding(
                horizontal = AppTheme.spacing.inset.lg,
                vertical = AppTheme.spacing.inset.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
        ) {
            AppText(text = headline, role = TextRole.Body)
            if (supporting != null) {
                AppText(text = supporting, role = TextRole.Secondary)
            }
        }
        trailing?.invoke()
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppListItem(headline = "Pilsner Urquell", supporting = "0,5 l", onClick = {})
    AppListItem(
        headline = "Camera",
        supporting = "android.permission.CAMERA",
        trailing = { AppTag(label = "Granted", tone = TagTone.Paid) },
    )
}
