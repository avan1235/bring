import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.lang.System.getenv
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem as currentOS


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxRpc)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.lumo)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xnon-local-break-continue", "-Xwhen-guards")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    if (currentOS().isMacOsX) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
        listOf<KotlinNativeTarget>(
//            macosX64(),
//            macosArm64(),
        ).forEach { macosTarget ->
            macosTarget.binaries.executable {
                entryPoint = "in.procyk.bring.main"
            }
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                    port = env.CORS_PORT.value.toInt()
                }
            }
        }
        binaries.executable()
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
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.kstore.file)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.uiUtil)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kstore)
            implementation(libs.reorderable)
            implementation(libs.compose.colorpicker)
            implementation(libs.materialKolor)
            implementation(libs.generativeaiGoogle)

            implementation(projects.shared)
            implementation(projects.sharedClient)

            implementation(libs.kotlinx.rpc.core)
            
            implementation(libs.ktor.serialization.kotlinx.cbor)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.arrow.core)
            implementation(libs.arrow.core.serialization)
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
        }
        androidUnitTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.androidx.ui.test.junit4)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.junit.rule)

        }
//        macosMain.dependencies {
//            implementation(libs.ktor.client.darwin)
//            implementation(libs.kstore.file)
//        }
    }
}

android {
    namespace = "in.procyk.bring"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "in.procyk.bring"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libsVersionCode
        versionName = "1.1.23"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            manifestPlaceholders += mapOf(
                "cleartextTrafficEnabled" to "true",
            )
        }
        release {
            isMinifyEnabled = false
            manifestPlaceholders += mapOf(
                "cleartextTrafficEnabled" to "false",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

compose.desktop {
    application {
        mainClass = "in.procyk.bring.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "in.procyk.bring"
            packageVersion = "1.0.0"
        }
    }
}

buildkonfig {
    packageName = "in.procyk.bring"
    objectName = "ComposeAppConfig"
    exposeObjectWithName = "ComposeAppConfig"

    defaultConfigs {
        buildConfigField(Type.STRING, "CLIENT_HOST", env.CLIENT_HOST.value)
        buildConfigField(Type.INT, "CLIENT_PORT", env.CLIENT_PORT.value)
        buildConfigField(Type.STRING, "CLIENT_HTTP_PROTOCOL", env.CLIENT_HTTP_PROTOCOL.value)
        buildConfigField(Type.STRING, "CLIENT_WS_PROTOCOL", env.CLIENT_WS_PROTOCOL.value)
        buildConfigField(Type.STRING, "VERSION", version.toString())
        buildConfigField(Type.STRING, "PACKAGE", packageName)
        buildConfigField(Type.STRING, "APP_NAME", "Bring!")
        buildConfigField(Type.STRING, "AUTHOR", "Maciej Procyk")
    }
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xwasm-kclass-fqn")
}

private val libsVersionCode: Int
    get() {
        val code = libs.versions.versionCode.get().toInt()
        val bump = getenv()["BUMP_FILE_VERSION_CODE"]?.toBooleanStrictOrNull() ?: false
        if (!bump) return code

        val file = project.file("../gradle/libs.versions.toml")
        val updatedFile = file.readLines().map { line ->
            if (!line.startsWith("versionCode")) return@map line

            val currentVersionCode = line
                .dropWhile { it != '"' }
                .removePrefix("\"")
                .takeWhile { it != '"' }
                .toInt()
            if (currentVersionCode != code) throw IllegalStateException("Two different version codes: $code vs $currentVersionCode")

            """versionCode = "${currentVersionCode + 1}""""
        }.joinToString(separator = "\n")
        file.writeText(updatedFile)
        return code
    }