package com.example.androidapp.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * SLOW version: plain data class with no stability annotations.
 * Compose cannot infer stability from List<> parameters, so every recomposition
 * that passes a SlowPost triggers full recomposition of the receiving composable.
 */
data class SlowPost(
    val id: Int,
    val authorName: String,
    val authorId: Int,
    val content: String,
    val likes: Int,
    val comments: List<SlowComment>,
    val timestampMs: Long,
)

data class SlowComment(
    val id: Int,
    val authorName: String,
    val text: String,
    val timestampMs: Long,
)

/**
 * FAST version: @Immutable tells Compose all public properties are read-only
 * and will not change without a structural replacement — enabling skippability.
 */
@Immutable
data class FastPost(
    val id: Int,
    val authorName: String,
    val authorId: Int,
    val content: String,
    val likes: Int,
    val comments: List<FastComment>,
    val timestampMs: Long,
)

@Immutable
data class FastComment(
    val id: Int,
    val authorName: String,
    val text: String,
    val timestampMs: Long,
)
