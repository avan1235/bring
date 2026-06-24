import buildsrc.convention.Env_gradle.Env.SavvryPackageName
import buildsrc.convention.Env_gradle.Env.SavvryVersion
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("buildsrc.convention.env")
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

dependencies {
    implementation(projects.app.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.filekit.core)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "in.procyk.savvry.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = SavvryPackageName
            packageVersion = SavvryVersion

            linux {
                iconFile.set(project.layout.projectDirectory.file("src/main/resources/icon.png"))
            }
            windows {
                iconFile.set(project.layout.projectDirectory.file("src/main/resources/icon.png"))
            }
            macOS {
                iconFile.set(project.layout.projectDirectory.file("src/main/resources/icon.png"))
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from("proguard-rules.pro")
        }
    }
}
