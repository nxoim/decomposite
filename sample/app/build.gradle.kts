import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "app"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()
//
//    cocoapods {
//        version = "1.0.0"
//        summary = "Compose application framework"
//        homepage = "empty"
//        ios.deploymentTarget = "11.0"
//        podfile = project.file("../iosApp/Podfile")
//        framework {
//            baseName = "ComposeApp"
//            isStatic = true
//        }
//    }

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.decompose)
            implementation(libs.decompose.extensions)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(project(":core"))
            implementation(project(":optionalExtensions"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activityCompose)
            implementation(libs.compose.uitooling)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.decompose)
            implementation(libs.decompose.extensions)
            implementation(libs.koin.core)
            implementation(libs.koin.android)
            implementation(project(":core"))
            implementation(project(":optionalExtensions"))
        }

        jvmMain.dependencies {
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(project(":core"))
        }
    }
}

android {
    namespace = "com.number869.decomposite"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        applicationId = "com.number869.decomposite.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }
    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/resources")
        resources.srcDirs("src/commonMain/resources")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}
dependencies {
    implementation(project(mapOf("path" to ":core")))
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "com.number869.decomposite.desktopApp"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}
}

