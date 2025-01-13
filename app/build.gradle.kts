plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.foxluo.resource"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.foxluo.resource"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":home"))
    implementation(project(":community"))
    implementation(project(":chat"))
    implementation(project(":notification"))
    implementation(project(":mine"))
    implementation(libs.androidx.core.animation)
    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.androidx.monitor)
    androidTestImplementation(libs.junit.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
}