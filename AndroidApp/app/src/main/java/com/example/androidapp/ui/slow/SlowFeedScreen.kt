package com.example.androidapp.ui.slow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidapp.data.SlowComment
import com.example.androidapp.data.SlowPost
import com.example.androidapp.viewmodel.SlowFeedViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlowFeedScreen(
    onBack: () -> Unit,
    viewModel: SlowFeedViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐌 Slow Feed — Anti-patterns Active") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            )
        }
    ) { padding ->
        // ❌ PERF ISSUE 1: No key {} in items() block.
        // When any item changes (e.g. a like), LazyColumn cannot track identity
        // and must rebind ALL visible item composables instead of just the changed one.
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(viewModel.posts) { post ->           // ← no key!
                SlowPostItem(
                    post = post,
                    // ❌ PERF ISSUE 2: Lambda literal allocated on every recomposition.
                    // Compose sees a new function object each time, making SlowPostItem
                    // non-skippable even if 'post' has not changed.
                    onLike = { viewModel.likePost(post.id) },
                )
            }
        }
    }
}

@Composable
fun SlowPostItem(post: SlowPost, onLike: () -> Unit) {
    // ❌ PERF ISSUE 3: Heavy computation running inline during composition.
    // This busy-loop simulates expensive work (image decoding, JSON parsing, etc.)
    // being done directly on the composition / main thread instead of a background thread.
    var dummy = 0L
    repeat(50_000) { dummy += it }          // ~2–5 ms of wasted CPU per item per frame

    // ❌ PERF ISSUE 4: Creating new Color objects without remember.
    // Color() is cheap, but this pattern scales to real objects (Paint, Path, Shader)
    // that cost significantly more — and it runs on EVERY recomposition of this item.
    val avatarColor = Color(
        red = abs(post.authorName.hashCode()) % 200 / 255f + 0.2f,
        green = abs(post.authorId * 97) % 200 / 255f + 0.2f,
        blue = abs(post.authorName.length * 53) % 200 / 255f + 0.2f,
    )

    // ❌ PERF ISSUE 5: Sorting the comments list inline during composition.
    // This is O(n log n) work that runs every time this composable recomposes,
    // even when the comments have not changed at all.
    val sortedComments = post.comments.sortedByDescending { it.timestampMs }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = post.authorName.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(post.authorName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    // ❌ PERF ISSUE 6: SimpleDateFormat created on every recomposition — no remember.
                    // SimpleDateFormat is expensive to construct and not thread-safe.
                    Text(
                        text = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                            .format(Date(post.timestampMs)),
                        fontSize = 11.sp,
                        color = Color.Gray,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(post.content, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            // Like button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text("${post.likes}", fontSize = 13.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            sortedComments.forEach { comment ->
                SlowCommentItem(comment = comment)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun SlowCommentItem(comment: SlowComment) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = comment.authorName,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.widthIn(max = 100.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(comment.text, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
        // ❌ PERF ISSUE 7: New SimpleDateFormat + new Date object on every recomposition.
        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(comment.timestampMs)),
            fontSize = 10.sp,
            color = Color.LightGray,
        )
    }
}
