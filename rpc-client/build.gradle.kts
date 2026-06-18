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
        namespace = "$SavvryPackageName.rpcClient"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.serialization.kotlinx.cbor)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kotlinx.rpc.client)
            implementation(libs.kotlinx.rpc.client.ktor)
            implementation(libs.kotlinx.rpc.serialization.cbor)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        if (currentOS().isMacOsX) {
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
