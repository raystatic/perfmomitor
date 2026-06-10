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
 * Macrobenchmark for the SLOW feed.
 *
 * Captures:
 *  - frameDurationCpuMs  (how long each frame takes on the CPU)
 *  - frameOverrunMs      (how many ms each frame exceeded its deadline — positive = jank)
 *
 * Run on a physical device (not emulator) for accurate results:
 *   ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest \
 *       -P android.testInstrumentationRunnerArguments.class=com.example.benchmark.SlowFeedBenchmark
 *
 * Results appear in Android Studio > Benchmark tool window and in
 * macrobenchmark/build/outputs/connected_android_test_additional_output/
 *
 * To capture a Perfetto trace alongside benchmark results, run with:
 *   compilationMode = CompilationMode.Full()
 * and open the resulting .perfetto-trace file in ui.perfetto.dev
 */
@RunWith(AndroidJUnit4::class)
class SlowFeedBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun slowFeedScroll() = rule.measureRepeated(
        packageName = "com.example.androidapp",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),   // simulates a fully AOT-compiled release app
        startupMode = StartupMode.COLD,
        iterations = 5,
        setupBlock = {
            // Navigate to the Slow Feed before measurement starts
            pressHome()
            startActivityAndWait()
        },
        measureBlock = {
            val device = this.device

            // Tap "Open Slow Feed" button
            device.wait(Until.hasObject(By.text("Open Slow Feed")), 3_000)
            device.findObject(By.text("Open Slow Feed")).click()
            device.waitForIdle()

            // Scroll the feed 5 times to capture representative frame data
            val list = device.findObject(By.scrollable(true))
            repeat(5) {
                list.fling(Direction.DOWN)
                device.waitForIdle()
            }
        },
    )

    @Test
    fun slowFeedLikeJank() = rule.measureRepeated(
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
            device.wait(Until.hasObject(By.text("Open Slow Feed")), 3_000)
            device.findObject(By.text("Open Slow Feed")).click()
            device.waitForIdle()

            // Tap the like button on the first 10 visible posts
            // Each tap triggers Thread.sleep(32) on the main thread → dropped frames
            repeat(10) {
                val likeBtn = device.findObject(By.desc("Like"))
                likeBtn?.click()
                device.waitForIdle()
            }
        },
    )
}
