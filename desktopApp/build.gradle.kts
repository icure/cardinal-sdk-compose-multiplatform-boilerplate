import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

val EXTERNAL_SERVICES_SPEC_ID = providers.gradleProperty("EXTERNAL_SERVICES_SPEC_ID").get()
val APPLICATION_ID = providers.gradleProperty("APPLICATION_ID").get()
val PROCESS_ID = providers.gradleProperty("PROCESS_ID").get()

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
}

// Configure JVM arguments for all JavaExec tasks (including jvmRun used by IntelliJ)
tasks.withType<JavaExec>().configureEach {
    systemProperty("EXTERNAL_SERVICES_SPEC_ID", EXTERNAL_SERVICES_SPEC_ID)
    systemProperty("APPLICATION_ID", APPLICATION_ID)
    systemProperty("PROCESS_ID", PROCESS_ID)
    systemProperty("KEY_STORAGE_PATH", "${project.rootDir.absolutePath}/keyStorage")
}

compose.desktop {
    application {
        mainClass = "com.icure.cardinal.compose.multiplatform.MainKt"

        jvmArgs(
            "-DEXTERNAL_SERVICES_SPEC_ID=${EXTERNAL_SERVICES_SPEC_ID}",
            "-DAPPLICATION_ID=${APPLICATION_ID}",
            "-DPROCESS_ID=${PROCESS_ID}",
            "-DKEY_STORAGE_PATH=${project.rootDir.absolutePath}/keyStorage"
        )

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.icure.cardinal.compose.multiplatform"
            packageVersion = "1.0.0"
        }
    }
}
