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


    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

    implementation(platform(libs.androidx.compose.bom))

// Compose libraries
    implementation(libs.androidx.ui)               // ensure this references the stable compose.ui
    implementation(libs.androidx.ui.graphics)      // same note as above
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)        // stable version, matching the BOM
    implementation("androidx.compose.material:material-icons-core:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

// DHIS2 libraries (fine, presumably)
    implementation("org.hisp.dhis:android-core:1.11.0")
    implementation("org.hisp.dhis.rules:rule-engine:2.0.48")
    implementation(libs.play.services.tflite.support)
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.play.services.fido)
    implementation("org.hisp.dhis.mobile:designsystem-android:0.4.1")
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
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.0.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0") // For testing flows

    // Android testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.mockito:mockito-android:5.0.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    // Debug implementations
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
}