package com.example.androidproject1.feature.inventory.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppTag
import com.example.androidproject1.core.ui.component.TagTone
import com.example.androidproject1.feature.inventory.domain.ItemCondition

/**
 * An item's condition as a tag — the list's trailing slot and the detail's header both show it,
 * so the word and the tone are decided once. New is positive, good is neutral, worn is a warning:
 * a state, never a component (E3U1), and the label always carries it.
 */
@Composable
fun ItemConditionTag(
    condition: ItemCondition,
    modifier: Modifier = Modifier,
) {
    val tone = when (condition) {
        ItemCondition.New -> TagTone.Positive
        ItemCondition.Good -> TagTone.Neutral
        ItemCondition.Worn -> TagTone.Warning
    }
    AppTag(label = stringResource(condition.labelRes()), tone = tone, modifier = modifier)
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    ItemConditionTag(condition = ItemCondition.New)
    ItemConditionTag(condition = ItemCondition.Good)
    ItemConditionTag(condition = ItemCondition.Worn)
}
