plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
}

android {
    namespace = "app.breeze"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.breeze"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        // Use 'named("release") { ... }' for explicit configuration
        named("release") {
            isMinifyEnabled = true
            shrinkResources = true // This should now be resolved
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            // --- SIGNING CONFIGURATION (for GitHub Actions) ---
            // You will need to define 'release' signingConfig elsewhere,
            // likely by injecting secrets in your GitHub Actions workflow.
            // Example: signingConfig = signingConfigs.getByName("release")
        }
        // Use 'named("debug") { ... }' for explicit configuration
        named("debug") {
            isMinifyEnabled = false
            shrinkResources = false // This should also be resolved
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // This MUST be a Compose Compiler version (e.g., 1.5.x, 1.6.x),
        // NOT the Kotlin plugin version.
        kotlinCompilerExtensionVersion = "1.6.11" // Ensure this is correct
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.compose.ui:ui:1.8.3")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("net.engawapg.lib:zoomable:2.8.0")
}
