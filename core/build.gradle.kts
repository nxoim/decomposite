import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.binary.compatibility.validator)
}

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }
    //    js(IR) {
//        browser()
//    }

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//    }

    macosX64()
    macosArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "decomposite"
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
                api(libs.decompose)
                api(libs.decompose.extensions)
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
    kotlin {
        jvmToolchain(17)
    }
}

mavenPublishing {
//    publishToMavenCentral(SonatypeHost.DEFAULT)
    // or when publishing to https://s01.oss.sonatype.org
//    publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
//    signAllPublications()
    coordinates("com.nxoim", "decomposite", "0.0.1")

    pom {
        name.set(project.name)

        url.set("https://github.com/nxoim/Decomposite")
        licenses {
            license {
                name.set("APACHE LICENSE, VERSION 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0")
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
            url.set("https://github.com/nxoim/Decomposite")
            connection.set("scm:git:git://github.com/nxoim/Decomposite.git")
            developerConnection.set("scm:git:ssh://git@github.com/nxoim/Decomposite.git")
        }
    }
}