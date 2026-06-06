# Specs for Android

An Android version of the [Specs](https://github.com/enzo-quirici/Specs) hardware information tool. Built with modern Android technologies to provide detailed insights into your device's hardware and system software.

![App Icon](app/src/main/res/drawable/app_icon.png)

## Features

- **CPU & GPU**: Detailed processor information, core counts, architecture, cluster configuration (Big.Little), real-time core frequencies, and GPU details (Vendor, Renderer, GL version).
- **Device**: Comprehensive device specs including Model, Manufacturer, Brand, Screen size/resolution/density, RAM monitoring, and Internal Storage tracking.
- **System**: Android version, API level, Build ID, Kernel version, and Java VM info.
- **Battery**: Real-time battery monitoring (Level, Status, Health, Temperature, Voltage, and Technology).
- **Thermal**: Live readings from internal thermal sensors.
- **Sensors**: Full list of all hardware sensors available on the device.
- **Material 3 UI**: Clean, modern interface with a bottom navigation bar for easy access to all categories.
- **Edge-to-Edge**: Fully supports modern Android display standards, including Android 15 and 16.

## Screenshots

*(Coming Soon)*

## Technical Details

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (ViewModel, StateFlow)
- **Minimum SDK**: 23 (Android 6.0 Marshmallow)
- **Target SDK**: 37 (Android 16)

## Development

The project is structured into three main layers:
1. **Data Layer (`HardwareProvider`)**: Interacts with Android system APIs and reads from `/proc` and `/sys` filesystems to retrieve hardware data.
2. **ViewModel Layer (`MainViewModel`)**: Manages the application state and handles periodic data refreshes for dynamic information.
3. **UI Layer (`MainActivity`)**: A single-activity architecture using Compose for all screens and navigation.

## License

This project is licensed under the same terms as the original Specs repository.
