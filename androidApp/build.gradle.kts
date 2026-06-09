import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.koin.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.libsignal.android)

    implementation(libs.compose.uiToolingPreview)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.noztek.xend"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.noztek.xend"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "EMULATOR_API_BASE_URL", "\"http://10.0.2.2:8080\"")
        buildConfigField("String", "DEVICE_API_BASE_URL", "\"http://192.168.1.6:8080\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
