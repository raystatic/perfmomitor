package com.example.androidapp.data

private val AUTHOR_NAMES = listOf(
    "Alice Martin", "Bob Chen", "Carol Smith", "David Kim", "Eva Russo",
    "Frank Lee", "Grace Liu", "Henry Park", "Iris Wang", "Jack Brown",
    "Karen Davis", "Leo Miller", "Mia Wilson", "Noah Moore", "Olivia Taylor",
    "Paul Anderson", "Quinn Thomas", "Rachel Jackson", "Sam White", "Tina Harris",
)

private val POST_CONTENTS = listOf(
    "Just shipped a feature that took 3 months. Feels surreal. 🚀",
    "Hot take: the best code is the code you delete.",
    "Spent 2 hours debugging only to find a missing semicolon. Classic.",
    "New blog post on Compose performance is live. Link in bio.",
    "Reminder: premature optimization is the root of all evil. But also measure first.",
    "Working from a mountain cabin this week. Productivity through the roof. 🏔️",
    "Does anyone else rewrite the same function 5 times before it feels right?",
    "Benchmarked our app today. The slow screen had 200ms frame times. Fixed it.",
    "Started learning Rust. My brain hurts in the best way.",
    "Kotlin coroutines are genuinely a game changer for Android development.",
)

private val COMMENT_TEXTS = listOf(
    "Totally agree with this!",
    "Have you tried the new API for this?",
    "Took me a while to figure this out too.",
    "Great post, thanks for sharing.",
    "Following for more content like this.",
    "This is exactly what I needed today.",
    "I had the same issue last week.",
    "Would love to see a follow-up post.",
    "Shared this with my whole team!",
    "The struggle is real 😅",
)

object FeedRepository {
    private val baseTime = System.currentTimeMillis()

    fun generateSlowPosts(count: Int = 100): List<SlowPost> = List(count) { i ->
        SlowPost(
            id = i,
            authorName = AUTHOR_NAMES[i % AUTHOR_NAMES.size],
            authorId = i % AUTHOR_NAMES.size,
            content = POST_CONTENTS[i % POST_CONTENTS.size],
            likes = (i * 37 + 11) % 500,
            comments = List(3) { j ->
                SlowComment(
                    id = i * 10 + j,
                    authorName = AUTHOR_NAMES[(i + j + 1) % AUTHOR_NAMES.size],
                    text = COMMENT_TEXTS[(i + j) % COMMENT_TEXTS.size],
                    timestampMs = baseTime - (i * 60_000L) - (j * 10_000L),
                )
            },
            timestampMs = baseTime - i * 60_000L,
        )
    }

    fun generateFastPosts(count: Int = 100): List<FastPost> = List(count) { i ->
        FastPost(
            id = i,
            authorName = AUTHOR_NAMES[i % AUTHOR_NAMES.size],
            authorId = i % AUTHOR_NAMES.size,
            content = POST_CONTENTS[i % POST_CONTENTS.size],
            likes = (i * 37 + 11) % 500,
            comments = List(3) { j ->
                FastComment(
                    id = i * 10 + j,
                    authorName = AUTHOR_NAMES[(i + j + 1) % AUTHOR_NAMES.size],
                    text = COMMENT_TEXTS[(i + j) % COMMENT_TEXTS.size],
                    timestampMs = baseTime - (i * 60_000L) - (j * 10_000L),
                )
            },
            timestampMs = baseTime - i * 60_000L,
        )
    }
}
