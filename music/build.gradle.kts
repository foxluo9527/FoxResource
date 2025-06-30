plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.foxluo.resource.music"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

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

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}
dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.arouter.api)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit.junit)
    kapt(libs.arouter.compiler)
    implementation(libs.androidx.media3.exoplayer)
    implementation(project(":baseLib"))
    implementation(libs.lyricviewx)
    implementation(libs.androidx.media3.session)
}