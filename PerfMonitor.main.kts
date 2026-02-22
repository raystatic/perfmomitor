import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Scanner
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: kotlinc -script PerfMode.main.kts -- --package <package_name> [--apk <path_to_apk>] [--skip-startup]")
        return
    }

    val packageIdx = args.indexOf("--package")
    val packageName = args[packageIdx + 1]
    
    val apkIdx = args.indexOf("--apk")
    val apkPath = if (apkIdx != -1 && apkIdx + 1 < args.size) args[apkIdx + 1] else null
    
    val skipStartup = args.contains("--skip-startup")

    // 1. Setup & Installation
    if (apkPath != null) {
        println("📦 Installing $apkPath...")
        executeAdb("install", "-r", apkPath)
    }

    // 2. Cold Start Measurement (Optional)
    var coldStartTime: String = "Skipped"
    if (!skipStartup) {
        println("⏱️  Measuring Cold Start Time...")
        executeAdb("shell", "am", "force-stop", packageName)
        Thread.sleep(1000)
        val startResult = executeAdb("shell", "am start -W -p $packageName")
        coldStartTime = Regex("TotalTime: (\\d+)").find(startResult)?.groupValues?.get(1) ?: "N/A"
        if (coldStartTime != "N/A") coldStartTime += " ms"
    } else {
        println("⏭️  Skipping Cold Start. Monitoring existing process...")
    }
    
    // Find PID
    val pid = executeAdb("shell", "pidof $packageName").trim()
    if (pid.isEmpty() && skipStartup) {
        println("⚠️  Warning: App $packageName does not seem to be running. Please open it first!")
    }

    // 3. Monitoring Setup
    val isRunning = AtomicBoolean(true)
    val memData = mutableListOf<Long>()
    val cpuData = mutableListOf<Double>()

    println("\n" + "!".repeat(60))
    println("  KOTLIN LIVE MONITORING ACTIVE")
    println("!".repeat(60))
    println("STARTUP STATE: $coldStartTime")
    println("-".repeat(60))
    println("%-15s | %-12s | %-10s".format("Timestamp", "Memory (MB)", "CPU (%)"))
    println("-".repeat(60))

    val monitorThread = thread(start = true) {
        try {
            while (isRunning.get()) {
                val timestamp = java.time.LocalTime.now().withNano(0).toString()
                
                // --- MEMORY ---
                var currentMem: Double? = null
                val memOut = executeAdb("shell", "dumpsys meminfo $packageName | grep 'TOTAL PSS'")
                memOut.trim().split(Regex("\\s+")).getOrNull(2)?.toLongOrNull()?.let {
                    memData.add(it)
                    currentMem = it / 1024.0
                }

                // --- CPU ---
                var currentCpu: Double? = null
                val topOut = executeAdb("shell", "top -n 1 -b | grep $packageName")
                if (topOut.isNotEmpty()) {
                    val cols = topOut.trim().split(Regex("\\s+"))
                    val cpuVal = cols.getOrNull(8)?.replace("%", "")?.toDoubleOrNull()
                    if (cpuVal != null) {
                        currentCpu = cpuVal
                    } else {
                        val matches = Regex("(\\d+(\\.\\d+)?)").findAll(topOut).toList()
                        if (matches.size >= 7) {
                            currentCpu = matches[matches.size - 2].value.toDoubleOrNull()
                        }
                    }
                }

                if (currentCpu != null) cpuData.add(currentCpu!!)

                // --- LIVE LOG ---
                val memStr = currentMem?.let { "%.2f MB".format(it) } ?: "---"
                val cpuStr = currentCpu?.let { "%.1f%%".format(it) } ?: "---"
                println("%-15s | %-12s | %-10s".format(timestamp, memStr, cpuStr))

                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
            // Thread was stopped
        }
    }

    val scanner = Scanner(System.`in`)
    println("\n👉 Press [ENTER] to stop and view final report.\n")
    if (scanner.hasNextLine()) scanner.nextLine()
    
    isRunning.set(false)
    monitorThread.interrupt()
    monitorThread.join(1000)

    println("\n" + "=".repeat(40))
    println("📊 KOTLIN PERFORMANCE FINAL REPORT")
    println("=".repeat(40))
    println("Startup Time:  $coldStartTime")
    println("-".repeat(40))
    if (memData.isNotEmpty()) {
        println("Memory (RAM):")
        println("  - Average: %.2f MB".format(memData.average() / 1024.0))
        println("  - Peak:    %.2f MB".format((memData.maxOrNull()?.toDouble() ?: 0.0) / 1024.0))
        
        if (cpuData.isNotEmpty()) {
            println("\nCPU Load:")
            println("  - Average: %.1f%%".format(cpuData.average()))
            println("  - Peak:    %.1f%%".format(cpuData.maxOrNull() ?: 0.0))
        }
    } else {
        println("❌ No performance data captured. Is the app running?")
    }
    println("=".repeat(40))
}

fun executeAdb(vararg args: String): String {
    return try {
        val process = ProcessBuilder("adb", *args).start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        output
    } catch (e: Exception) {
        ""
    }
}

main(args)
