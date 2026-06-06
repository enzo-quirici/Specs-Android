package com.qualcomm_toolbox.specs.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import java.io.File
import java.io.RandomAccessFile
import java.util.Locale

class HardwareProvider(private val context: Context) {

    fun getCpuInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        val cpuName = try {
            val lines = File("/proc/cpuinfo").readLines()
            lines.firstOrNull { it.contains("Hardware") }?.substringAfter(":")?.trim()
                ?: lines.firstOrNull { it.contains("Processor") }?.substringAfter(":")?.trim()
                ?: Build.HARDWARE
        } catch (e: Exception) {
            Build.HARDWARE
        }
        info["Processor"] = cpuName
        info["Cores"] = Runtime.getRuntime().availableProcessors().toString()
        info["Architecture"] = System.getProperty("os.arch") ?: "Unknown"
        
        val clusters = if (Runtime.getRuntime().availableProcessors() >= 8) "4+4 Big.Little" else "Standard"
        info["Clusters"] = clusters

        for (i in 0 until Runtime.getRuntime().availableProcessors()) {
            val freq = try {
                val raf = RandomAccessFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq", "r")
                val f = raf.readLine().trim().toLong() / 1000
                raf.close()
                "$f MHz"
            } catch (e: Exception) {
                try {
                    val raf = RandomAccessFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq", "r")
                    val f = raf.readLine().trim().toLong() / 1000
                    raf.close()
                    "$f MHz (Max)"
                } catch (e2: Exception) {
                    "Offline"
                }
            }
            info["Core $i"] = freq
        }
        return info
    }

    fun getGpuInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configInfo = activityManager.deviceConfigurationInfo
        info["GL Version"] = configInfo.glEsVersion
        
        val hardware = Build.HARDWARE.lowercase()
        info["Vendor"] = when {
            hardware.contains("qcom") || hardware.contains("adreno") -> "Qualcomm"
            hardware.contains("mali") || hardware.contains("mt") -> "ARM / MediaTek"
            hardware.contains("exynos") || hardware.contains("s5e") -> "Samsung"
            else -> "Unknown"
        }
        info["Renderer"] = if (hardware.contains("qcom")) "Adreno" else "Mali"
        info["Load"] = "N/A"
        info["Scaling"] = "N/A"

        return info
    }

    fun getDeviceInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        info["Model"] = Build.MODEL
        info["Manufacturer"] = Build.MANUFACTURER
        info["Brand"] = Build.BRAND
        info["Board"] = Build.BOARD
        info["Hardware"] = Build.HARDWARE
        
        val metrics = context.resources.displayMetrics
        info["Screen Size"] = getScreenSize(metrics)
        info["Resolution"] = "${metrics.widthPixels} x ${metrics.heightPixels}"
        info["Density"] = "${metrics.densityDpi} dpi"
        
        val memInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memInfo)
        
        info["Total RAM"] = formatBytes(memInfo.totalMem)
        info["Available RAM"] = formatBytes(memInfo.availMem)
        info["Used RAM"] = formatBytes(memInfo.totalMem - memInfo.availMem)
        
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val totalInternal = internalStat.blockSizeLong * internalStat.blockCountLong
        val freeInternal = internalStat.blockSizeLong * internalStat.availableBlocksLong
        info["Internal Storage Total"] = formatBytes(totalInternal)
        info["Internal Storage Free"] = formatBytes(freeInternal)
        info["Internal Storage Used"] = formatBytes(totalInternal - freeInternal)
        
        return info
    }

    fun getSystemInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        info["Android Version"] = Build.VERSION.RELEASE
        info["API Level"] = Build.VERSION.SDK_INT.toString()
        info["Build ID"] = Build.ID
        info["Kernel"] = System.getProperty("os.version") ?: "Unknown"
        info["Java VM"] = System.getProperty("java.vm.version") ?: "Unknown"
        return info
    }

    fun getBatteryInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level * 100 / scale.toFloat()
            info["Level"] = "$batteryPct%"
            
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            info["Status"] = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Unknown"
            }
            
            val health = it.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            info["Health"] = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                else -> "Unknown"
            }
            
            val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0
            info["Temperature"] = "$temp °C"
            
            val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            info["Voltage"] = "$voltage mV"
            
            info["Technology"] = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        }
        return info
    }

    fun getThermalInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        try {
            val thermalDir = File("/sys/class/thermal/")
            thermalDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("thermal_zone")) {
                    val type = try { File(file, "type").readText().trim() } catch (e: Exception) { "Unknown" }
                    val temp = try { File(file, "temp").readText().trim().toDouble() / 1000.0 } catch (e: Exception) { 0.0 }
                    if (temp > 0) {
                        info[type] = "$temp °C"
                    }
                }
            }
        } catch (e: Exception) {
            info["Error"] = "Could not read thermal data"
        }
        if (info.isEmpty()) info["Thermal"] = "No data available"
        return info
    }

    fun getSensorInfo(): List<String> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getSensorList(Sensor.TYPE_ALL).map { it.name }
    }

    private fun getScreenSize(metrics: DisplayMetrics): String {
        val x = Math.pow((metrics.widthPixels / metrics.xdpi).toDouble(), 2.0)
        val y = Math.pow((metrics.heightPixels / metrics.ydpi).toDouble(), 2.0)
        val screenInches = Math.sqrt(x + y)
        return String.format(Locale.US, "%.2f inches", screenInches)
    }

    private fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format(Locale.US, "%.2f %cB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}
