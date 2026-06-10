package com.example.androidapp.ui.fast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidapp.data.FastComment
import com.example.androidapp.data.FastPost
import com.example.androidapp.viewmodel.FastFeedViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastFeedScreen(
    onBack: () -> Unit,
    viewModel: FastFeedViewModel = viewModel(),
) {
    // ✅ FIX 1: collectAsStateWithLifecycle — lifecycle-aware, avoids leaks.
    // The StateFlow<ImmutableList> emits only when data actually changes.
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val likedIds by viewModel.likedIds.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ Fast Feed — Optimised") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ✅ FIX 2: key { post.id } lets LazyColumn track item identity.
            // When a single item's likes change, only that one Card recomposes.
            // Without a key, every visible item is considered potentially changed.
            items(posts, key = { post -> post.id }) { post ->
                val isLiked by remember(post.id) {
                    derivedStateOf { post.id in likedIds }
                }
                FastPostItem(
                    post = post,
                    isLiked = isLiked,
                    // ✅ FIX 3: Stable lambda — we pass a method reference that does NOT
                    // capture a new closure on every recomposition, so FastPostItem
                    // can be skipped when its inputs have not changed.
                    onLike = viewModel::likePost,
                )
            }
        }
    }
}

@Composable
fun FastPostItem(
    post: FastPost,
    isLiked: Boolean,
    onLike: (Int) -> Unit,
) {
    // ✅ FIX 4: remember(key) — Color derivation runs only when authorName/Id changes,
    // not on every recomposition triggered by unrelated state reads.
    val avatarColor = remember(post.authorName, post.authorId) {
        Color(
            red = abs(post.authorName.hashCode()) % 200 / 255f + 0.2f,
            green = abs(post.authorId * 97) % 200 / 255f + 0.2f,
            blue = abs(post.authorName.length * 53) % 200 / 255f + 0.2f,
        )
    }

    // ✅ FIX 5: Sort once and remember the result. The sort only re-runs when the
    // comments list reference changes — not on every frame during scroll.
    val sortedComments = remember(post.comments) {
        post.comments.sortedByDescending { it.timestampMs }
    }

    // ✅ FIX 6: Pre-format the timestamp once per post, not on every frame.
    val formattedTime = remember(post.timestampMs) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(post.timestampMs))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Text(text = formattedTime, fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(post.content, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onLike(post.id) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.error else Color.Gray,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text("${post.likes}", fontSize = 13.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            sortedComments.forEach { comment ->
                FastCommentItem(comment = comment)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun FastCommentItem(comment: FastComment) {
    // ✅ FIX 7: remember(key) for formatted timestamp — only re-runs when timestamp changes.
    val formattedTime = remember(comment.timestampMs) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(comment.timestampMs))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = comment.authorName,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.widthIn(max = 100.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(comment.text, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
        Text(text = formattedTime, fontSize = 10.sp, color = Color.LightGray)
    }
}
