plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.sqlDelight) apply false
}

subprojects {
    configurations.configureEach {
        resolutionStrategy.force(
            "org.jetbrains.androidx.lifecycle:lifecycle-common:${libs.versions.androidx.lifecycle.get()}",
            "org.jetbrains.androidx.lifecycle:lifecycle-runtime:${libs.versions.androidx.lifecycle.get()}",
            "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.androidx.lifecycle.get()}",
            "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:${libs.versions.androidx.lifecycle.get()}",
            "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidx.lifecycle.get()}",
        )
    }
}
