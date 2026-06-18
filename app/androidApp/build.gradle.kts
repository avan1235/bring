import buildsrc.convention.Env_gradle.Env.SavvryVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.lang.System.getenv

plugins {
    id("buildsrc.convention.env")
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

dependencies {
    implementation(projects.app.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.filekit.dialogs)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "in.procyk.savvry"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = env.APPLICATION_ID.value
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libsVersionCode
        versionName = SavvryVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,io.netty.versions.properties}"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

private val libsVersionCode: Int
    get() {
        val code = libs.versions.versionCode.get().toInt()
        val bump = getenv()["BUMP_FILE_VERSION_CODE"]?.toBooleanStrictOrNull() ?: false
        if (!bump) return code

        val file = project.file("../../gradle/libs.versions.toml")
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
