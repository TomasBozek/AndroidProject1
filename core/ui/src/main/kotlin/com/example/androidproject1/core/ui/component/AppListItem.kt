package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The base of every list in the app.
 *
 * Its height is the density's row height — 56 dp on touch — so a row is a comfortable target
 * without anything inside it having to grow. [leading] is where an avatar or an icon goes, and
 * [trailing] a tag or a value.
 *
 * [selected] is for a row that is currently *shown elsewhere* — the chosen item of a list–detail
 * pair on a wide screen. It is not the same as checked, which is [AppCheckbox]'s job and is state
 * the user set; a selected row is where the app is, and it says so to a screen reader through
 * `Role.Tab`'s selected state rather than by colour alone.
 *
 * [onLongClick] is how a list enters selection mode. It needs [onClick] too: a row that can only
 * be long-pressed is a row a screen reader cannot reach, so the long press is an addition to a
 * tap, never a replacement.
 *
 * [headlineModifier] reaches the headline's own text — for `Modifier.appSharedElement`, so the
 * name can travel to the screen the row opens (D76). The row does not know why.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
    headlineModifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                // A row that can be the shown one reports *which* it is, so a screen reader says
                // "selected" rather than only "button"; a row that cannot is a plain button.
                when {
                    onClick == null -> Modifier
                    selected -> Modifier.selectable(
                        selected = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    onLongClick != null -> Modifier.combinedClickable(
                        role = Role.Button,
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                    else -> Modifier.clickable(role = Role.Button, onClick = onClick)
                },
            )
            .background(if (selected) AppTheme.colors.confirm.container else Color.Transparent)
            .defaultMinSize(minHeight = AppTheme.density.listRowHeight)
            .padding(
                horizontal = AppTheme.spacing.inset.lg,
                vertical = AppTheme.spacing.inset.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        leading?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
        ) {
            AppText(text = headline, role = TextRole.Body, modifier = headlineModifier)
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
        headline = "Kofola",
        supporting = "0,5 l — shown on the right",
        onClick = {},
        selected = true,
    )
    AppListItem(
        headline = "Camera",
        supporting = "android.permission.CAMERA",
        trailing = { AppTag(label = "Granted", tone = TagTone.Positive) },
    )
    AppListItem(
        headline = "Cordless drill",
        supporting = "Jana Nováková",
        onClick = {},
        leading = { AppAvatar(name = "Jana Nováková") },
        trailing = { AppTag(label = "Good") },
    )
}
