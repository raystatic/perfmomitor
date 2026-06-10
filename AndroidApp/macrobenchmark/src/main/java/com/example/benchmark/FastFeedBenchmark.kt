package com.example.benchmark

import androidx.benchmark.macro.*
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark for the FAST (optimised) feed.
 * Identical structure to SlowFeedBenchmark — run both and compare the JSON output.
 *
 * Key metrics to compare:
 *
 *   | Metric                | Slow (expected)   | Fast (expected)  |
 *   |-----------------------|-------------------|------------------|
 *   | frameDurationCpuMs P50| ~42 ms            | ~6 ms            |
 *   | frameDurationCpuMs P95| ~120 ms           | ~12 ms           |
 *   | frameOverrunMs P50    | ~28 ms (jank)     | ~-2 ms (no jank) |
 *   | frameOverrunMs P95    | ~105 ms           | ~4 ms            |
 *
 * Values above are indicative for a Pixel 6 running Android 14.
 * Your numbers will vary by device but the ratio should be consistent.
 *
 * ── Profiling with Perfetto ───────────────────────────────────────────────────
 * 1. The benchmark writes a .perfetto-trace file to the device's /sdcard/Download/
 * 2. Pull it:  adb pull /sdcard/Download/<trace_file> .
 * 3. Open it in the Perfetto UI: https://ui.perfetto.dev
 * 4. Look for long "Choreographer#doFrame" slices in the main thread track.
 *    In the SLOW trace you'll see:
 *      - Long "LAYOUT" / "DRAW" phases caused by full list re-measure
 *      - "SlowPostItem" recompositions back-to-back (Compose recomposer track)
 *      - Thread.sleep gaps blocking the main thread
 *    In the FAST trace these are absent or negligible.
 *
 * ── System Tracing in Android Studio ────────────────────────────────────────
 * 1. Run the app in Profile mode (Run > Profile)
 * 2. Choose "System Trace" in the Profiler
 * 3. Record while scrolling each feed
 * 4. In the "Frames" row, RED/YELLOW frames indicate jank
 * 5. Click a long frame to see which thread caused it
 */
@RunWith(AndroidJUnit4::class)
class FastFeedBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun fastFeedScroll() = rule.measureRepeated(
        packageName = "com.example.androidapp",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.COLD,
        iterations = 5,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        },
        measureBlock = {
            val device = this.device

            device.wait(Until.hasObject(By.text("Open Fast Feed")), 3_000)
            device.findObject(By.text("Open Fast Feed")).click()
            device.waitForIdle()

            val list = device.findObject(By.scrollable(true))
            repeat(5) {
                list.fling(Direction.DOWN)
                device.waitForIdle()
            }
        },
    )

    @Test
    fun fastFeedLikeJank() = rule.measureRepeated(
        packageName = "com.example.androidapp",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.COLD,
        iterations = 3,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        },
        measureBlock = {
            val device = this.device
            device.wait(Until.hasObject(By.text("Open Fast Feed")), 3_000)
            device.findObject(By.text("Open Fast Feed")).click()
            device.waitForIdle()

            // Same 10 taps — all run on viewModelScope, main thread stays free
            repeat(10) {
                val likeBtn = device.findObject(By.desc("Like"))
                likeBtn?.click()
                device.waitForIdle()
            }
        },
    )
}
