import org.jetbrains.kotlin.com.intellij.ide.plugins.ActionDescriptorName
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.load.kotlin.signatures
import org.jetbrains.kotlin.utils.sure

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

    @OptIn(ExperimentalWasmDsl::class)
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            this.groupId = project.group.toString()
            this.artifactId = "decomposite"
            this.version = project.version.toString()

            pom {
                this.description = project.description.toString()
                this.url = "https://github.com/nxoim/decomposite"

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
                    url = "https://github.com/nxoim/decomposite"
                    connection = "scm:git:git://github.com/nxoim/decomposite.git"
                    developerConnection = "scm:git:ssh://git@github.com/nxoim/decomposite.git"
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    useInMemoryPgpKeys(
        findProperty("signingKey")?.toString() ?: System.getenv("GPG_PRIVATE_KEY"),
        findProperty("signingPassword")?.toString() ?: System.getenv("GPG_PASSPHRASE")
    )
    sign(publishing.publications.findByName("maven"))
}