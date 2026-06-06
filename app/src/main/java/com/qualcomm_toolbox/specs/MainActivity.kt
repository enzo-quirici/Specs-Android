package com.qualcomm_toolbox.specs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qualcomm_toolbox.specs.ui.theme.SpecsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpecsTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("CPU", Icons.Default.Memory),
        TabItem("Device", Icons.Default.Devices),
        TabItem("System", Icons.Default.Info),
        TabItem("Battery", Icons.Default.BatteryFull),
        TabItem("Thermal", Icons.Default.Thermostat),
        TabItem("Sensors", Icons.Default.Sensors),
        TabItem("About", Icons.Default.Info)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Specs", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider()
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {} // Remove default divider as we added our own above
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(tab.title) },
                            icon = { Icon(tab.icon, contentDescription = null) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedTabIndex) {
                0 -> CpuTab(viewModel)
                1 -> DeviceTab(viewModel)
                2 -> SystemTab(viewModel)
                3 -> BatteryTab(viewModel)
                4 -> ThermalTab(viewModel)
                5 -> SensorsTab(viewModel)
                6 -> AboutTab()
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)

@Composable
fun InfoList(info: Map<String, String>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(info.toList()) { (key, value) ->
            InfoRow(key, value)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun InfoRow(key: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = key, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CpuTab(viewModel: MainViewModel) {
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("CPU Information", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text(cpuInfo["Processor"] ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }
        items(cpuInfo.toList()) { (key, value) ->
            if (key != "Processor") {
                InfoRow(key, value)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeveloperBoard,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("GPU Information", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }
        items(gpuInfo.toList()) { (key, value) ->
            InfoRow(key, value)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun DeviceTab(viewModel: MainViewModel) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    InfoList(deviceInfo)
}

@Composable
fun SystemTab(viewModel: MainViewModel) {
    val systemInfo by viewModel.systemInfo.collectAsState()
    InfoList(systemInfo)
}

@Composable
fun BatteryTab(viewModel: MainViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    InfoList(batteryInfo)
}

@Composable
fun ThermalTab(viewModel: MainViewModel) {
    val thermalInfo by viewModel.thermalInfo.collectAsState()
    InfoList(thermalInfo)
}

@Composable
fun SensorsTab(viewModel: MainViewModel) {
    val sensors by viewModel.sensors.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(sensors) { sensor ->
            Text(text = sensor, modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
        }
    }
}

@Composable
fun AboutTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Specs", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(text = "v1.0.0", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "An Android version of the Specs hardware info tool.",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
