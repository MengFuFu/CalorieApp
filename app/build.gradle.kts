plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)      // Hilt 的注解处理走 KSP
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.mh.calorie"   // 代码命名空间，与 applicationId 解耦（以后可加 .debug 后缀）
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mh.calorie"
        minSdk = 26                // Android 8.0：避开大量兼容坑，且 java.time 原生可用
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        debug {
            // debug 包用不同 applicationId，可以和正式包同时装在一台机器上
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true          // 混淆 + 压缩，正式包必须开
            isShrinkResources = true
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

    // Kotlin 编译目标必须和 Java 一致，否则会出现 "Inconsistent JVM-target" 报错
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true    // 本项目不用 Compose，ViewBinding 是核心
        buildConfig = true
    }
}

dependencies {
    // 自有模块
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    // AndroidX 基础
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 导航：单 Activity + 多 Fragment 的核心
    implementation(libs.androidx.navigation.fragment)
}