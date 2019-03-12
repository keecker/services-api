<p align="center"><img src="https://keecker.gitlab.io/developer-website/assets/images/keecker-opened.jpg" width="500"/></p>

# Keecker Services API

This **Android library** allows your app to access [Keecker](https://www.keecker.com) features. It provides a low-level access to sensors and movement, as well as higher-level information:

- Front and 360 cameras
- Depth camera and proximity sensors
- Movement commands and location on the map
- Wall and charging station detection

## Getting Started

### Prerequisites

- You will need Android Studio, check the install instructions on the [official website](https://developer.android.com/studio).
- You may want to check "[Build your first app](https://developer.android.com/training/basics/firstapp/)" on the Android developer website.
- Create a new Android TV app, preferably using the Kotlin language.

### Add the library in your Android app

[![](https://jitpack.io/v/keecker/services-api.svg)](https://jitpack.io/#keecker/services-api)

1. Add the JitPack repository in your root build.gradle at the end of repositories.

   ```gradle
       allprojects {
           repositories {
               ...
               maven { url 'https://jitpack.io' }
           }
       }
   ```
2. Add the dependency, replace Tag by the latest version: [JitPack](https://jitpack.io/#keecker/services-api).

   ```gradle
        dependencies {
            implementation 'com.github.keecker:services-api:Tag'
        }
   ```

### Use the Keecker Services

In your main AndroidManifest.xml, add the following permission to be allowed to move the projector:

```xml
<manifest>
    ....
    <uses-permission android:name="com.keecker.permission.PROJECTION" />
```

Add the code that will actually move the projector in your activity:

```kotlin
fun wobbleProjector() {
    val projector = KeeckerServices.getProjectorClient(applicationContext)
    GlobalScope.launch(Dispatchers.IO) {
        while (true) {
            projector.setState(ProjectorState(orientation = 0))
            delay(5000)
            projector.setState(ProjectorState(orientation = 90))
        }
    }
}
```

At the moment you can use the following clients:

- [`ProjectorClient`](https://github.com/keecker/services-api/blob/master/src/main/java/com/keecker/services/interfaces/projection/ProjectorClient.kt), requiring the `com.keecker.permission.PROJECTION` permission.
- [`PerceptionClient`](https://github.com/keecker/services-api/blob/master/src/main/java/com/keecker/services/interfaces/navigation/PerceptionClient.kt), requiring the `com.keecker.permission.PERCEPTION` permission.
- [`MovementClient`](https://github.com/keecker/services-api/blob/master/src/main/java/com/keecker/services/interfaces/navigation/MovementClient.kt), requiring the `com.keecker.permission.MOVEMENT` permission.

## Building

This is not needed to use the library, add it to your project as described above. However, you may be interested to run some of the tests that show how Android handles IPC and permissions.

You need a working Android development environment to compile this library. [Follow the instructions on the official website](https://developer.android.com/sdk/index.html).

### Compiling

Generate the library.

```bash
./gradlew assembleRelease
```

### Unit test

Unit tests specify and test the logic behind API Clients.

```bash
./gradlew test
```

Then check the report in `build/reports/tests`

### Android tests

Android tests show how Android handles IPC and permissions, and what to expect from it. You will need a plugged Android device or emulator to run those tests.

```bash
./gradlew connectedAndroidTest
```

Then check the report in `build/reports/tests`

### Coding-style tests

Coding style is not yet tested, we follow the Google coding styles:

- [Google Kotlin style guide](https://developer.android.com/kotlin/style-guide)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)

## License

Licensed under the Apache License, Version 2.0 - see the [LICENSE.md](LICENSE.md) file for details.
