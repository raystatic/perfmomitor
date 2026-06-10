package com.example.androidapp.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapp.data.FeedRepository
import com.example.androidapp.data.FastPost
import com.example.androidapp.data.SlowPost
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── SLOW ViewModel ───────────────────────────────────────────────────────────

/**
 * ❌ PERF: Uses SnapshotStateList directly. Updating a single item notifies ALL
 * observers and forces the entire LazyColumn to re-measure items without keys.
 */
class SlowFeedViewModel : ViewModel() {
    val posts: MutableList<SlowPost> = mutableStateListOf<SlowPost>().also {
        it.addAll(FeedRepository.generateSlowPosts())
    }

    /**
     * ❌ PERF: Blocks the main thread with Thread.sleep to simulate a synchronous
     * "network call". This causes dropped frames every time the user taps Like.
     */
    fun likePost(postId: Int) {
        Thread.sleep(32) // simulates blocking I/O on the main thread
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            posts[index] = posts[index].copy(likes = posts[index].likes + 1)
        }
    }
}

// ─── FAST ViewModel ──────────────────────────────────────────────────────────

/**
 * ✅ FIX: Exposes ImmutableList via StateFlow. Compose's collectAsStateWithLifecycle
 * only triggers recomposition of composables that actually read the new value.
 * Like operations run on a coroutine, never touching the main thread.
 */
class FastFeedViewModel : ViewModel() {
    private val _posts = MutableStateFlow<ImmutableList<FastPost>>(
        FeedRepository.generateFastPosts().toImmutableList()
    )
    val posts: StateFlow<ImmutableList<FastPost>> = _posts.asStateFlow()

    private val _likedIds = MutableStateFlow<ImmutableSet<Int>>(persistentSetOf())
    val likedIds: StateFlow<ImmutableSet<Int>> = _likedIds.asStateFlow()

    fun likePost(postId: Int) {
        viewModelScope.launch {
            // ✅ FIX: Runs on the coroutine dispatcher, never blocks the main thread.
            _posts.update { list ->
                list.map { post ->
                    if (post.id == postId) post.copy(likes = post.likes + 1) else post
                }.toImmutableList()
            }
            _likedIds.update { it.add(postId) }
        }
    }
}
