# PerfMonitor 🚀

A lightweight, interactive Android performance profiling CLI tool built with Kotlin. 

`PerfMonitor` allows developers to capture real-time Memory (RAM) and CPU usage metrics while manually interacting with an Android application. It also provides a "Cold Start" measurement to benchmark app launch efficiency.

## Key Features
- **Cold Start Benchmarking**: Measures the time it takes for your app to become interactive.
- **Live Performance Dashboard**: Real-time terminal table showing current Memory and CPU load.
- **Multi-Core CPU Tracking**: Accurately captures multi-threaded activity (e.g., values > 100% on multi-core devices).
- **Skip Startup Mode**: Attach the monitor to an already running process to test deep-link features.
- **Zero Dependencies**: A single-file Kotlin script (`.main.kts`) that runs on any machine with `kotlinc`.

## Installation

Ensure you have the following installed:
1. **ADB**: Part of the Android SDK Platform-Tools.
2. **Kotlin**: `kotlinc` version 1.3 or higher.

## Usage

### 1. Basic Profiling (with Cold Start)
This will force-stop the app, measure the launch time, and start the live monitor:
```bash
kotlinc -script PerfMonitor.main.kts -- --package com.example.app
```

### 2. Monitoring an Already Running App
Use the `--skip-startup` flag to attach to a specific screen or feature without restarting the app:
```bash
kotlinc -script PerfMonitor.main.kts -- --package com.example.app --skip-startup
```

### 3. Install and Profile
Provide an APK path to install the latest build before starting the session:
```bash
kotlinc -script PerfMonitor.main.kts -- --package com.example.app --apk path/to/your-app.apk
```

## How to Read the Metrics

- **Memory (RAM)**: Measured in MB (PSS). High "Peak" memory is a common cause of Android OOM (Out of Memory) crashes.
- **CPU Load**: Measured as a percentage of a single core. Spikes during interaction are normal; high idle usage indicates a battery-drain bug.

## License
MIT License - See the [LICENSE](LICENSE) file for details.
