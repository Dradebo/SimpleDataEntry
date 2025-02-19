plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

android {
    namespace = "com.xavim.testsimpleact"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xavim.testsimpleact"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(platform(libs.androidx.compose.bom))

// Compose libraries
    implementation(libs.androidx.ui)               // ensure this references the stable compose.ui
    implementation(libs.androidx.ui.graphics)      // same note as above
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)        // stable version, matching the BOM


// DHIS2 libraries (fine, presumably)
    implementation("org.hisp.dhis:android-core:1.11.0")
    implementation("org.hisp.dhis.rules:rule-engine:2.0.48")
    implementation(libs.play.services.tflite.support)

// Hilt
    val hiltVersion = "2.48"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

// Jetpack Navigation
// be sure that libs.androidx.navigation.safe.args.generator is a plugin in your classpath, not a runtime lib
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

// Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

// AndroidX core & lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

// Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    api(libs.dhis2.mobile.designsystem)
}