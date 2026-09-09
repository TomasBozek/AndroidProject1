package com.example.androidproject1.feature.profile.presentation.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.feature.profile.presentation.R
import java.io.File

/** Takes a picture into this app's cache and hands back the URI it was written to. */
@Composable
fun CaptureButton(onCaptured: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Saveable, not remembered: the camera is a different app, and this one is a prime candidate
    // for being killed while it is in front. Without this the picture comes back to a screen that
    // has forgotten where it asked for it to be put.
    var target by rememberSaveable { mutableStateOf<String?>(null) }

    val camera = rememberLauncherForActivityResult(contract = CaptureToUri) { saved ->
        target?.takeIf { saved }?.let(onCaptured)
    }

    AppButton(
        label = stringResource(R.string.profile_capture),
        onClick = {
            val uri = context.newCaptureUri().toString()
            target = uri
            camera.launch(Uri.parse(uri))
        },
        modifier = modifier.testTag("profile_captureButton"),
    )
}

/**
 * `TakePicture`, plus the grant the camera app needs for the file it is handed.
 *
 * The stock contract puts the URI in `EXTRA_OUTPUT` and flags nothing, so another app cannot open
 * it — and the symptom is a zero-byte file rather than an error, which is the kind of thing that
 * reaches a release. `createIntent` is open for exactly this.
 */
private object CaptureToUri : ActivityResultContracts.TakePicture() {

    override fun createIntent(context: Context, input: Uri): Intent =
        super.createIntent(context, input).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
}

/**
 * A file in this app's cache that the camera may write into.
 *
 * A camera app cannot be handed a `File`; it needs a `content://` URI it has been granted access
 * to, which is what the `FileProvider` in this module's `AndroidManifest.xml` publishes. The cache
 * is the right home for it — what is worth keeping is the copy the data layer makes.
 */
private fun Context.newCaptureUri(): Uri {
    val directory = File(cacheDir, CAPTURE_DIRECTORY).apply { mkdirs() }
    val file = File(directory, "capture-${System.currentTimeMillis()}.jpg")
    // Matches the authority in AndroidManifest.xml, where it is written as ${applicationId} — so
    // the three flavors, whose ids differ by a suffix, each get their own.
    return FileProvider.getUriForFile(this, "$packageName.profile.captures", file)
}

private const val CAPTURE_DIRECTORY = "captures"

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    CaptureButton(onCaptured = {})
}
