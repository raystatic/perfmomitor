package com.example.androidapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateSlow: () -> Unit,
    onNavigateFast: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compose Perf Demo") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "This app demonstrates common Jetpack Compose performance anti-patterns " +
                    "and their fixes. Open both feeds, scroll them, and tap ❤️ to see the difference.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FeedOptionCard(
                emoji = "🐌",
                title = "Slow Feed",
                subtitle = "7 active anti-patterns",
                description = "Scroll and tap ❤️ to experience dropped frames.",
                issues = listOf(
                    "No key {} → full list rebind on every like",
                    "Inline lambda → non-skippable composables",
                    "No @Immutable → Compose skips stability check",
                    "50 000-iteration busy-loop in composition",
                    "SimpleDateFormat created every recomposition",
                    "Comment list sorted every recomposition",
                    "Thread.sleep(32) on main thread per like tap",
                ),
                buttonText = "Open Slow Feed",
                buttonColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
                onClick = onNavigateSlow,
            )

            FeedOptionCard(
                emoji = "⚡",
                title = "Fast Feed",
                subtitle = "All 7 issues fixed",
                description = "Same data, same UI — runs at 60 fps.",
                issues = listOf(
                    "key { post.id } → only changed item recomposes",
                    "Stable lambda (method ref) → composables are skippable",
                    "@Immutable data classes → Compose infers stability",
                    "No computation in composition body",
                    "remember(ts) { SimpleDateFormat } → cached",
                    "remember(comments) { sort } → cached",
                    "viewModelScope.launch → non-blocking like",
                ),
                buttonText = "Open Fast Feed",
                buttonColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                onClick = onNavigateFast,
            )

            BenchmarkInfoCard()
        }
    }
}

@Composable
private fun FeedOptionCard(
    emoji: String,
    title: String,
    subtitle: String,
    description: String,
    issues: List<String>,
    buttonText: String,
    buttonColors: ButtonColors,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$emoji $title", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(description, fontSize = 13.sp)
            issues.forEach { issue ->
                Text("• $issue", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Button(onClick = onClick, colors = buttonColors, modifier = Modifier.fillMaxWidth()) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun BenchmarkInfoCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("📊 Measuring with Macrobenchmark", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Run the benchmark module on a physical device to get frame timing data:",
                fontSize = 13.sp,
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = "./gradlew :macrobenchmark:connectedBenchmarkAndroidTest",
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Text(
                "Results appear in Android Studio's Benchmark tab and as a JSON report " +
                    "in macrobenchmark/build/outputs/connected_android_test_additional_output/.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("Expected results (Pixel 6):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            MetricRow("Slow scroll frameDurationCpuMs", "P50 ≈ 42 ms", "P95 ≈ 120 ms")
            MetricRow("Fast scroll frameDurationCpuMs", "P50 ≈ 6 ms", "P95 ≈ 12 ms")
            MetricRow("Slow frameOverrunMs (jank)", "P50 ≈ 28 ms", "P95 ≈ 105 ms")
            MetricRow("Fast frameOverrunMs (jank)", "P50 ≈ -2 ms", "P95 ≈ 4 ms")
        }
    }
}

@Composable
private fun MetricRow(label: String, p50: String, p95: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(p50, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(p95, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
    }
}
