plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

import org.gradle.api.tasks.Exec
import java.time.Instant

val generatedVersionCode = (System.getenv("VERSION_CODE")?.toIntOrNull() ?: Instant.now().epochSecond.toInt())
val generatedVersionName = System.getenv("VERSION_NAME") ?: "0.1.$generatedVersionCode"

android {
    namespace = "ru.custle.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.custle.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = generatedVersionCode
        versionName = generatedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "DEFAULT_API_URL", "\"https://my.custle.ru/api/\"")
        buildConfigField("String", "DEFAULT_WEB_URL", "\"https://my.custle.ru/\"")
        buildConfigField("String", "UPDATE_METADATA_URL", "\"https://s3.twcstorage.ru/custle-android/android/debug/latest.json\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.material)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register<Exec>("deployDebugApk") {
    group = "distribution"
    description = "Uploads the assembled debug APK to the configured S3-compatible storage."
    dependsOn("assembleDebug")

    commandLine(
        "bash",
        "${rootDir}/scripts/deploy-apk.sh",
        "${rootDir}/deploy.s3.properties",
        "${rootDir}/app/build/outputs/apk/debug/app-debug.apk",
        generatedVersionCode.toString(),
        "${generatedVersionName}-debug",
    )
}
