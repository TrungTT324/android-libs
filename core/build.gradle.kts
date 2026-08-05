plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "hdisoft.app.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

// Publishes the "release" AAR to GitHub Packages (Maven registry) as
// hdisoft.app:core, so other repos can consume it via:
//   implementation("hdisoft.app:core:<version>")
// with the GitHub Packages Maven repo declared and authenticated (see this
// repo's root README.md for the full consumer setup).
afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = "hdisoft.app"
                artifactId = "core"
                version = project.findProperty("libVersion") as? String ?: "0.1.0"
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/TrungTT324/android-libs")
                credentials {
                    // Never hardcode these. Provide via env vars (CI) or
                    // ~/.gradle/gradle.properties as gpr.user / gpr.key (local).
                    username = System.getenv("GITHUB_ACTOR")
                        ?: project.findProperty("gpr.user") as? String
                    password = System.getenv("GITHUB_TOKEN")
                        ?: project.findProperty("gpr.key") as? String
                }
            }
        }
    }
}
