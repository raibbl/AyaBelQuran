plugins {
    id("com.android.application")
}

// Watch Face Format package: resources only, no code. Published as its own
// listing; the verse comes from the main app's complication data source.
android {
    namespace = "com.raibbl.ayabelquran.watchface"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.raibbl.ayabelquran.watchface"
        // WFF v1 requires Wear OS 4 (API 33).
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            // Keeps the debug APK free of dex too, matching release.
            isMinifyEnabled = true
        }
        release {
            isMinifyEnabled = true
            // Fonts and the watch face XML are only referenced by name at runtime.
            isShrinkResources = false
        }
    }
}
