import buildsrc.convention.Env_gradle.Env.BringPackageName
import buildsrc.convention.Env_gradle.Env.BringVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem as currentOS

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxRpc)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.ktor)
    alias(libs.plugins.graalVM)
    application
    id("buildsrc.convention.env")
}

group = BringPackageName
version = BringVersion

application {
    mainClass.set("in.procyk.bring.ApplicationKt")
}

dependencies {
    implementation(projects.shared)

    implementation(libs.logback)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auto.head.response)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.serialization.kotlinx.cbor)

    implementation(libs.kotlinx.rpc.server)
    implementation(libs.kotlinx.rpc.server.ktor)
    implementation(libs.kotlinx.rpc.serialization.cbor)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.pgjdbc.ng)

    implementation(libs.dotenv)
    implementation(libs.koin.ktor)
    implementation(libs.kotlinx.datetime)
    implementation(libs.arrow.core)
    implementation(libs.arrow.core.serialization)

    implementation(libs.ksoup)
    implementation(libs.ksoup.network)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(projects.sharedClient)
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            optIn("kotlin.uuid.ExperimentalUuidApi")
            optIn("kotlin.time.ExperimentalTime")
            optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

buildConfig {
    buildConfigField("VERSION", BringVersion)
}

graalvmNative {
    binaries {
        named("main") {
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            })
            resources.autodetect()
            fallback.set(false)
            verbose.set(true)

            buildArgs(
                listOf(
                    "--initialize-at-build-time=ch.qos.logback",
                    "--initialize-at-build-time=io.ktor",
                    "--initialize-at-build-time=kotlin",
                    "--initialize-at-build-time=kotlinx.io",
                    "--initialize-at-build-time=org.slf4j.LoggerFactory",
                    "--initialize-at-build-time=org.slf4j.helpers.Reporter",

                    "--initialize-at-build-time=kotlinx.serialization.modules.SerializersModuleKt",
                    "--initialize-at-build-time=kotlinx.serialization.internal",
                    "--initialize-at-build-time=kotlinx.serialization.cbor.Cbor\$Default",
                    "--initialize-at-build-time=kotlinx.serialization.cbor.Cbor",
                    "--initialize-at-build-time=kotlinx.serialization.cbor.CborImpl",

                    "--initialize-at-build-time=com.impossibl.postgres.jdbc.PGDriver",
                    "--initialize-at-build-time=com.impossibl.postgres.system.Version",
                    "--initialize-at-build-time=com.impossibl.postgres.protocol.ssl.ConsolePasswordCallbackHandler",
                    "--initialize-at-build-time=java.sql.DriverManager",
                    "--initialize-at-build-time=okhttp3.Protocol",

                    "--initialize-at-run-time=kotlin.uuid.SecureRandomHolder",

                    "-H:+InstallExitHandlers",
                    "-H:+ReportUnsupportedElementsAtRuntime",
                    "-H:+ReportExceptionStackTraces",

                    "--verbose"
                ) + when {
                    currentOS().isMacOsX -> emptyList()
                    else -> listOf(
                        "-H:+StaticExecutableWithDynamicLibC",
                    )
                }
            )

            imageName.set("server")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}