import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.icure.cardinal.compose.multiplatform.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        // Run commonTest on the Android host JVM too.
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            // Keep the framework name stable so the iOS `import ComposeApp` keeps working.
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.materialIconsCore)
            implementation(libs.compose.ui)
            implementation(libs.compose.componentsResources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.cardinal.sdk)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.datastore.preferences)
        }
    }
}

// --- iOS build configuration (xcconfig) generation ---
val EXTERNAL_SERVICES_SPEC_ID = providers.gradleProperty("EXTERNAL_SERVICES_SPEC_ID").get()
val APPLICATION_ID = providers.gradleProperty("APPLICATION_ID").get()
val PRODUCT_BUNDLE_IDENTIFIER = providers.gradleProperty("PRODUCT_BUNDLE_IDENTIFIER").get()
val PROCESS_ID = providers.gradleProperty("PROCESS_ID").get()

// Task to generate xcconfig file for iOS with build configuration values
abstract class GenerateIosConfigTask : DefaultTask() {
    @get:Input
    abstract val externalServicesSpecId: Property<String>

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val productBundleId: Property<String>

    @get:Input
    abstract val processId: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val configFile = outputFile.get().asFile
        configFile.parentFile.mkdirs()
        configFile.writeText("""
            // Auto-generated from gradle.properties - DO NOT EDIT MANUALLY
            EXTERNAL_SERVICES_SPEC_ID=${externalServicesSpecId.get()}
            APPLICATION_ID=${applicationId.get()}
            PRODUCT_BUNDLE_IDENTIFIER=${productBundleId.get()}
            PROCESS_ID=${processId.get()}
        """.trimIndent())
        println("Generated iOS config at: ${configFile.absolutePath}")
    }
}

tasks.register<GenerateIosConfigTask>("generateIosConfig") {
    externalServicesSpecId.set(EXTERNAL_SERVICES_SPEC_ID)
    applicationId.set(APPLICATION_ID)
    productBundleId.set(PRODUCT_BUNDLE_IDENTIFIER)
    processId.set(PROCESS_ID)
    outputFile.set(project.rootProject.file("iosApp/Configuration/BuildConfig.xcconfig"))
}

// Run before iOS builds
tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach {
    dependsOn("generateIosConfig")
}
