
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.dokka)
    id("signing")
}

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }
    //    js(IR) {
//        browser()
//    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

//    macosX64()
//    macosArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Decomposite"
            isStatic = true
            export(libs.decompose)
            export(libs.decompose.extensions)
            export(libs.essenty.lifecycle)
            export(libs.essenty.stateKeeper)
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {

    }

    composeCompiler {
        enableStrongSkippingMode = true
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(libs.kotlinx.serialization.json)
                api(libs.decompose)
                api(libs.decompose.extensions)
                api(libs.essenty.lifecycle)
                api(libs.essenty.stateKeeper)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.appcompat)
                api(libs.decompose.extensions)
            }
        }

        val jvmMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.swing)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.compose.uiTestJunit4)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.junit.junit)
            implementation(libs.compose.uiTestJunit4)
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.nxoim.decomposite"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        release {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
    kotlin {
        jvmToolchain(17)
    }
}

group = "com.nxoim"
description = "Navigation library for Compose Multiplatform projects"
version = "0.2.1.0-test-deployment2"

// stupid ass gradle bullshit omfg
//region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// <https://youtrack.jetbrains.com/issue/KT-46466>
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
//endregion

mavenPublishing {
    coordinates(group.toString(), "decomposite", version.toString())
    pom {
        name.set("decomposite")
        description.set("Navigation library for Compose Multiplatform projects")
        url.set("https://github.com/nxoim/decomposite")

        licenses {
            license {
                name.set("APACHE LICENSE, VERSION 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("nxoim")
                name.set("nxoim")
                url.set("https://github.com/nxoim/")
            }
        }

        scm {
            url.set("https://github.com/nxoim/decomposite")
            connection.set("scm:git:git://github.com/nxoim/decomposite.git")
            developerConnection.set("scm:git:ssh://git@github.com/nxoim/decomposite.git")
        }
    }

    publishing {
        repositories {
            maven {
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }
    }
}

signing {
    useGpgCmd()

    sign(publishing.publications)
}