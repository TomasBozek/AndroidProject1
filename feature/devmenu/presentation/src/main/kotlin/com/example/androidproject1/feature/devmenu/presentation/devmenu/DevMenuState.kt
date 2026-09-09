package com.example.androidproject1.feature.devmenu.presentation.devmenu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.devmenu.presentation.BuildInfo

/**
 * @property session the signed-in address, or `null` while signed out.
 * @property offlineSupported whether this flavor talks to fixtures and so has a switch to offer.
 */
@Immutable
data class DevMenuState(
    val build: BuildInfo,
    val session: String?,
    val offlineSupported: Boolean,
    val offline: Boolean,
) {

    companion object {

        val PREVIEW = DevMenuState(
            build = BuildInfo.PREVIEW,
            session = "ada@example.com",
            offlineSupported = true,
            offline = false,
        )
    }
}

/** The states this screen is drawn in: signed in, signed out, and a build with no fixture engine. */
class DevMenuStatePreviews : PreviewParameterProvider<DevMenuState> {

    override val values = sequenceOf(
        DevMenuState.PREVIEW,
        DevMenuState.PREVIEW.copy(session = null, offline = true),
        DevMenuState.PREVIEW.copy(
            build = BuildInfo.PREVIEW.copy(
                flavor = "staging",
                baseUrl = "https://staging.example.com/a/rather/long/base/path/",
            ),
            offlineSupported = false,
        ),
    )
}
