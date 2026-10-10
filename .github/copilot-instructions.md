# Mumla project guidance

## Build, test, and lint

- Read [README.md](../README.md) for Android SDK setup. Use JDK 21 and initialize the Humla Git submodule before building. The currently documented NDK version is 25.1.8937393.
- The Humla build currently needs its SpongyCastle JAR built separately:
  ```sh
  git submodule update --init --recursive
  pushd libraries/humla/libs/humla-spongycastle
  ../../gradlew jar
  popd
  ./gradlew assembleDebug
  ```
  On Windows, use `.\gradlew.bat` for root tasks and this PowerShell sequence for the submodule JAR:
  ```powershell
  Push-Location libraries\humla\libs\humla-spongycastle
  ..\..\gradlew.bat jar
  Pop-Location
  .\gradlew.bat assembleDebug
  ```
- `./gradlew assembleDebug` is the build used by CI. To build one distribution variant, use `./gradlew :app:assembleFossDebug` (or replace `Foss` with `Goog`, `Donation`, or `Beta`).
- CI documents `./gradlew :app:testDebugUnitTest` for unit tests and `./gradlew :app:lintDebug` for lint; those CI jobs are currently commented out. There are no checked-in `app/src/test` tests. To run one JVM test when present, use `./gradlew :app:testFossDebugUnitTest --tests 'se.lublin.mumla.YourTest'`; append `.yourTestMethod` to target one method. Replace the wrapper command with `.\gradlew.bat` on Windows.

## Architecture

- `app` is the Android client; `libraries/humla` is a Git submodule providing the Mumble protocol, connection, and audio service. Keep protocol-level changes in Humla rather than duplicating them in the app.
- `MumlaActivity` owns drawer/navigation, binds to `MumlaService`, and provides the database. `MumlaService` extends Humla's `HumlaService` and adds Mumla behavior such as notifications, reconnect handling, and push-to-talk controls. The service persists independently of individual screens.
- Screens that need the connection derive from `HumlaServiceFragment`. They register through `HumlaServiceProvider` and use `onServiceBound`/`onServiceUnbound` (and service observers) to attach and detach from connection state.
- `MumlaDatabase` is the persistence boundary; `MumlaSQLiteDatabase` implements it with SQLite. UI code obtains it through `DatabaseProvider`.
- Shared app code and resources belong in `app/src/main`. The `foss`, `goog`, `donation`, and `beta` product flavors overlay manifests, resources, and `StartupAction`; keep distribution-specific behavior in those source sets rather than branching throughout shared code.

## Repository conventions

- When adding a user-facing string, add it to the default resources in `app/src/main/res/values/strings.xml`; translations are maintained through Weblate in the locale-specific `values-*` directories.
- A release-news entry requires both a versioned `app_news_items_vX_Y_Z` string in `strings.xml` and a matching entry in `NEWS_ITEMS` in `DialogUtils`.
- The app's minimum supported Android version is API 21. The Android Gradle Plugin compiles against API 36; Humla's native build uses the NDK version specified by its Gradle configuration.
