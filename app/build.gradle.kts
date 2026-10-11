
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.enterex.devicemanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.enterex.devicemanager"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
