plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.gutlog.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.gutlog.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.androidx.room.runtime)
    androidTestImplementation(libs.androidx.sqlite.bundled)
    androidTestImplementation(libs.kotlinx.coroutines.core)
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.10.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.10.5")
}
