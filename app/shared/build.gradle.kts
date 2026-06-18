import buildsrc.convention.Env_gradle.Env.SavvryPackageName
import buildsrc.convention.Env_gradle.Env.SavvryVersion
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem as currentOS

plugins {
    id("buildsrc.convention.env")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxRpc)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.lumo)
}

group = SavvryPackageName
version = SavvryVersion

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xwhen-guards")
    }

    if (currentOS().isMacOsX) {
        listOf(
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "App"
                isStatic = true
            }
        }
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
        namespace = "in.procyk.savvry.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
                optIn("kotlinx.coroutines.FlowPreview")
            }
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.kstore.file)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.util)
            implementation(libs.compose.material)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.lumo.composables)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kstore)
            implementation(libs.reorderable)
            implementation(libs.compose.colorpicker)
            implementation(libs.materialKolor)
            implementation(libs.koog.agents)
            implementation(libs.koog.prompt.executor.google)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)

            implementation(projects.core)
            implementation(projects.rpcClient)

            implementation(libs.kotlinx.rpc.core)

            implementation(libs.ktor.serialization.kotlinx.cbor)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.arrow.core)
            implementation(libs.arrow.core.serialization)

            implementation(libs.backdrop)
            implementation(libs.shapes)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)
            implementation(libs.kstore.file)
            implementation(libs.appdirs)
        }
        if (currentOS().isMacOsX) {
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.kstore.file)
            }
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.kstore.storage)
            implementation(libs.kotlinx.browser)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.androidx.ui.test.junit4)
            implementation(libs.androidx.ui.test.manifest)
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.junit.rule)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "savvry.app.generated.resources"
    generateResClass = always
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

buildkonfig {
    packageName = SavvryPackageName
    exposeObjectWithName = "AppConfig"

    defaultConfigs {
        buildConfigField(Type.STRING, "CLIENT_HOST", env.CLIENT_HOST.value)
        buildConfigField(Type.INT, "CLIENT_PORT", env.CLIENT_PORT.value)
        buildConfigField(Type.STRING, "CLIENT_HTTP_PROTOCOL", env.CLIENT_HTTP_PROTOCOL.value)
        buildConfigField(Type.STRING, "CLIENT_WS_PROTOCOL", env.CLIENT_WS_PROTOCOL.value)
        buildConfigField(Type.STRING, "VERSION", version.toString())
        buildConfigField(Type.STRING, "PACKAGE", packageName)
        buildConfigField(Type.STRING, "APP_NAME", "Savvry")
        buildConfigField(Type.STRING, "AUTHOR", "Macie.j Procyk")
    }
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xwasm-kclass-fqn")
}
