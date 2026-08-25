plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.adforus.cubidsdksample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.adforus.cubidsdksample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xsuppress-version-warnings",
                "-Xskip-metadata-version-check",
                "-Xskip-prerelease-check"
            )
        }
    }
}



dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")

    implementation("com.adforus.sdk:cubid:1.4.0")
    implementation("com.adforus.sdk:adsu:2.3.0")
    implementation("com.adforus.sdk:cubex:1.4.0")
    implementation("com.adforus.sdk:upan:1.3.0")
    implementation("com.adforus.sdk:ual:1.0.0")
}
