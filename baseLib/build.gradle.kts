plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "com.foxluo.baselib"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(libs.arouter.api)
    api("com.airbnb.android:lottie:6.6.2")
    kapt(libs.arouter.compiler)
    api(libs.xui)
    api(libs.github.glide)
    api(libs.retrofit2.retrofit)
    api(libs.glide.transformations)
    api(libs.refresh.layout.kernel)
    api(libs.refresh.header.classics)
    api(libs.converter.gson)
    api(libs.xxpermissions)
    api(libs.lifecycle.runtime.ktx)
    api(libs.lifecycle.viewmodel.ktx)
    api(libs.androidx.activity.activity.ktx)
    api(libs.androidx.fragment.ktx)
    api(libs.videocache)
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.appcompat)
    api(libs.material)
    api(libs.constraintlayout)
    api(libs.android.defensecrash)
    api(libs.utilcodex)
    implementation(libs.androidx.core.animation)
    testApi(libs.junit)
    testApi(libs.junit.junit)
    androidTestApi(libs.androidx.junit)
    androidTestApi(libs.androidx.espresso.core)
    testApi(libs.pandora)
}