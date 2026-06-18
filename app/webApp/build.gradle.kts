import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    id("buildsrc.convention.env")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlin.time.ExperimentalTime")
            }
        }
        wasmJsMain.dependencies {
            implementation(projects.app.shared)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.browser)
        }
    }
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xwasm-kclass-fqn")
}

fun File.fillClientEnvVariables() {
    if (!exists()) return

    val content = readText()
    val updatedContent = content
        .replace($$"${CLIENT_HOST}", env.CLIENT_HOST.value)
        .replace($$"${CLIENT_PORT}", env.CLIENT_PORT.value)
        .replace($$"${CLIENT_HTTP_PROTOCOL}", env.CLIENT_HTTP_PROTOCOL.value)
    writeText(updatedContent)
}

tasks.named("wasmJsProcessResources") {
    doLast {
        val indexHtml = layout.buildDirectory.file("processedResources/wasmJs/main/index.html").get().asFile
        indexHtml.fillClientEnvVariables()
    }
}
