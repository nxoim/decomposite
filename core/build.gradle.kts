plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.binary.compatibility.validator)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm("desktop")

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
            baseName = "decomposite-core"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(libs.decompose)
                implementation(libs.decompose.extensions)
            }
        }
        val androidMain by getting {
            dependencies {
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.number869.decomposite.core"

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
    coordinates("com.number869", "decomposite-core", "0.0.1")

    pom {
        name.set(project.name)

        url.set("https://github.com/Number869/Decomposite")
        licenses {
            license {
                name.set("APACHE LICENSE, VERSION 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("Number869")
                name.set("Number 869")
                url.set("https://github.com/Number869/")
            }
        }
        scm {
            url.set("https://github.com/Number869/Decomposite")
            connection.set("scm:git:git://github.com/Number869/Decomposite.git")
            developerConnection.set("scm:git:ssh://git@github.com/Number869/Decomposite.git")
        }
    }
}