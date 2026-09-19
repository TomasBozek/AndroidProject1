package com.example.androidproject1.feature.devmenu.presentation.devmenu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.devmenu.presentation.BuildInfo
import com.example.androidproject1.feature.devmenu.presentation.DevMenuJump
import com.example.androidproject1.service.core.ui.text.toUiText

/**
 * @property session the signed-in address, or `null` while signed out.
 * @property offlineSupported whether this flavor talks to fixtures and so has a switch to offer.
 * @property realApiSupported the same, for the switch that sends TMDB requests to the real host.
 * @property realApiKeyPresent whether the build was made with a key — without one the switch is
 * drawn disabled, with a hint, rather than promising something the request cannot do.
 * @property jumps the screens the menu opens directly, in the order they are listed.
 */
@Immutable
data class DevMenuState(
    val build: BuildInfo,
    val session: String?,
    val offlineSupported: Boolean,
    val offline: Boolean,
    val realApiSupported: Boolean,
    val realApiKeyPresent: Boolean,
    val realApi: Boolean,
    val jumps: List<DevMenuJump>,
) {

    companion object {

        val PREVIEW = DevMenuState(
            build = BuildInfo.PREVIEW,
            session = "ada@example.com",
            offlineSupported = true,
            offline = false,
            realApiSupported = true,
            realApiKeyPresent = true,
            realApi = false,
            jumps = listOf(
                DevMenuJump(id = "gallery", label = "GalleryScreen".toUiText(), navigate = {}),
                DevMenuJump(id = "tripWizard", label = "TripWizardScreen".toUiText(), navigate = {}),
                DevMenuJump(id = "inventoryEditor", label = "InventoryEditorScreen".toUiText(), navigate = {}),
            ),
        )
    }
}

/** The states this screen is drawn in: signed in, signed out, and a build with no fixture engine. */
class DevMenuStatePreviews : PreviewParameterProvider<DevMenuState> {

    override val values = sequenceOf(
        DevMenuState.PREVIEW,
        DevMenuState.PREVIEW.copy(session = null, offline = true, realApiKeyPresent = false),
        DevMenuState.PREVIEW.copy(
            build = BuildInfo.PREVIEW.copy(
                flavor = "staging",
                baseUrl = "https://staging.example.com/a/rather/long/base/path/",
            ),
            offlineSupported = false,
            realApiSupported = false,
        ),
    )
}
