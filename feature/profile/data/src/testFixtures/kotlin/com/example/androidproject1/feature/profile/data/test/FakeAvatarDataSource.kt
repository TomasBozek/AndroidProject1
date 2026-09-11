package com.example.androidproject1.feature.profile.data.test

import com.example.androidproject1.feature.profile.data.source.AvatarDataSource

/**
 * In-memory [AvatarDataSource] for tests: no [android.content.Context], no file I/O.
 *
 * @property stored the last URI [store] was asked to copy, so a test can assert what would have
 * been written without touching a file.
 * @property failWith thrown from [store] instead of storing — the branch a revoked Photo Picker
 * URI or a cleared camera cache directory takes.
 */
class FakeAvatarDataSource(var failWith: Throwable? = null) : AvatarDataSource {

    var stored: String? = null
        private set

    override suspend fun store(sourceUri: String): String {
        failWith?.let { throw it }
        stored = sourceUri
        return "file:///fake/avatar/$sourceUri"
    }
}
