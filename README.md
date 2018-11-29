<p align="center"><img src="https://keecker.gitlab.io/developer-website/assets/images/keecker-opened.jpg" width="500"/></p>

# Keecker Services API

This **Android library** allows your app to access [Keecker](https://www.keecker.com) features. It provides a low-level access to sensors and movement, as well as higher-level information:

- Front and 360 cameras
- Depth camera and proximity sensors
- Movement commands and location on the map
- Wall and charging station detection

## Getting Started

Please check the [**Keecker Android SDK website**](http://developer.keecker.com) to get started.

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
2. Add the dependency, find the release on [JitPack](https://jitpack.io/#keecker/services-api).

   ```gradle
        dependencies {
            implementation 'com.github.keecker:services-api:0.2.0-alpha'
        }
   ```

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
