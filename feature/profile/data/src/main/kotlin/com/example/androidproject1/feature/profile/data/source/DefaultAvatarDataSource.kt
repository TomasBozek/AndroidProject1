package com.example.androidproject1.feature.profile.data.source

import android.content.Context
import android.net.Uri
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Copies a picked or captured picture into `filesDir/avatar/`.
 *
 * Copying is the point rather than an implementation detail. The Photo Picker's URI is readable
 * only until this process dies, and the camera writes into a cache directory the system may clear,
 * so a stored profile that pointed at either would show a broken picture the next morning. What is
 * stored is a file this app owns.
 *
 * Switching to IO is this class's job, not the repository's: `BaseRepository` runs on the caller's
 * context and the caller is `viewModelScope`, which is `Dispatchers.Main`.
 */
class DefaultAvatarDataSource(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
) : AvatarDataSource {

    override suspend fun store(sourceUri: String): String = withContext(dispatcherProvider.io) {
        val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
        val target = File(directory, "avatar-${System.currentTimeMillis()}.jpg")

        val source = context.contentResolver.openInputStream(Uri.parse(sourceUri))
            ?: error("Nothing to read at $sourceUri")
        source.use { input -> target.outputStream().use(input::copyTo) }

        // One avatar at a time: whatever was here before is now unreachable and would only grow
        // the app's storage.
        directory.listFiles()?.forEach { if (it != target) it.delete() }

        Uri.fromFile(target).toString()
    }

    private companion object {

        const val DIRECTORY = "avatar"
    }
}
