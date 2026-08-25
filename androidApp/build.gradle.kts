plugins {
    // AGP 9 ships built-in Kotlin support, so org.jetbrains.kotlin.android
    // must NOT be applied here.
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val EXTERNAL_SERVICES_SPEC_ID: String by project
val APPLICATION_ID: String by project
val PRODUCT_BUNDLE_IDENTIFIER: String by project
val PROCESS_ID: String by project

android {
    namespace = "com.icure.cardinal.compose.multiplatform"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.icure.cardinal.compose.multiplatform"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "version"

    productFlavors {
        create("patient-dev") {
            isDefault = true
            dimension = "version"
            applicationId = PRODUCT_BUNDLE_IDENTIFIER

            buildConfigField("String", "externalServicesSpecId", """"$EXTERNAL_SERVICES_SPEC_ID"""")
            buildConfigField("String", "applicationId", """"$APPLICATION_ID"""")
            buildConfigField("String", "processId", """"$PROCESS_ID"""")
        }
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}
