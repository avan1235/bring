import buildsrc.convention.Env_gradle.Env.SavvryPackageName
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem as currentOS

plugins {
    id("buildsrc.convention.env")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxRpc)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    if (currentOS().isMacOsX) {
        iosArm64()
        iosSimulatorArm64()
    }

    jvmToolchain(21)
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
        namespace = "$SavvryPackageName.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
        commonMain.dependencies {
            implementation(libs.ktor.serialization.kotlinx.cbor)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.shared.resources)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.rpc.core)
            implementation(libs.arrow.core)
            implementation(libs.arrow.core.serialization)
        }
    }
}
