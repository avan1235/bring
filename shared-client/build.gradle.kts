import buildsrc.convention.Env_gradle.Env.BringPackageName
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem as currentOS

plugins {
    id("buildsrc.convention.env")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinxRpc)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xnon-local-break-continue")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

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

    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)

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

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = "$BringPackageName.sharedClient"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
