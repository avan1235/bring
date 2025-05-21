plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.buildKonfig) apply false
    alias(libs.plugins.lumo) apply false
    alias(libs.plugins.graalVM) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlinxRpc) apply false

    alias(libs.plugins.dotenvGradle)
}