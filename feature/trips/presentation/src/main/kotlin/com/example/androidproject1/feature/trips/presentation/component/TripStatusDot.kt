package com.example.androidproject1.feature.trips.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppStatusDot
import com.example.androidproject1.core.ui.component.TagTone
import com.example.androidproject1.feature.trips.domain.TripStatus
import com.example.androidproject1.feature.trips.presentation.R

/**
 * A trip's [TripStatus], the one place that maps it to a tone and a label.
 *
 * Three screens show a trip's status — the list, the detail and the dashboard's next trip — so
 * this lives in the feature's own `component/` rather than being written out three times. See
 * `AppStatusDot`'s gallery entry for the component it wraps.
 */
@Composable
fun TripStatusDot(
    status: TripStatus,
    modifier: Modifier = Modifier,
) {
    val (label, tone) = when (status) {
        TripStatus.Upcoming -> stringResource(R.string.trips_status_upcoming) to TagTone.Info
        TripStatus.Active -> stringResource(R.string.trips_status_active) to TagTone.Paid
        TripStatus.Completed -> stringResource(R.string.trips_status_completed) to TagTone.Neutral
    }
    AppStatusDot(label = label, tone = tone, modifier = modifier)
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    TripStatusDot(status = TripStatus.Active)
}
