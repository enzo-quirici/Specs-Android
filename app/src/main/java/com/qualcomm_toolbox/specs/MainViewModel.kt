package com.qualcomm_toolbox.specs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qualcomm_toolbox.specs.data.HardwareProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val hardwareProvider = HardwareProvider(application)

    private val _cpuInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val cpuInfo: StateFlow<Map<String, String>> = _cpuInfo

    private val _gpuInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val gpuInfo: StateFlow<Map<String, String>> = _gpuInfo

    private val _deviceInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceInfo: StateFlow<Map<String, String>> = _deviceInfo

    private val _systemInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val systemInfo: StateFlow<Map<String, String>> = _systemInfo

    private val _batteryInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val batteryInfo: StateFlow<Map<String, String>> = _batteryInfo

    private val _thermalInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val thermalInfo: StateFlow<Map<String, String>> = _thermalInfo

    private val _sensors = MutableStateFlow<List<String>>(emptyList())
    val sensors: StateFlow<List<String>> = _sensors

    init {
        refreshInfo()
        viewModelScope.launch {
            while (true) {
                _cpuInfo.value = hardwareProvider.getCpuInfo()
                _batteryInfo.value = hardwareProvider.getBatteryInfo()
                _thermalInfo.value = hardwareProvider.getThermalInfo()
                delay(2000)
            }
        }
    }

    private fun refreshInfo() {
        _cpuInfo.value = hardwareProvider.getCpuInfo()
        _gpuInfo.value = hardwareProvider.getGpuInfo()
        _deviceInfo.value = hardwareProvider.getDeviceInfo()
        _systemInfo.value = hardwareProvider.getSystemInfo()
        _batteryInfo.value = hardwareProvider.getBatteryInfo()
        _thermalInfo.value = hardwareProvider.getThermalInfo()
        _sensors.value = hardwareProvider.getSensorInfo()
    }
}
