import java.net.URI

buildscript {
    apply(from = "version.gradle.kts")

    repositories {
        jcenter()
        google()
    }

    dependencies {
        val kotlinVersion: String by extra
        val realmVersion: String by extra

        classpath("com.android.tools.build:gradle:3.3.0")
        classpath("io.realm:realm-gradle-plugin:$realmVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:$kotlinVersion")
    }
}

allprojects {
    repositories {
        maven {
            url = URI("https://oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            url = URI("https://jitpack.io")
        }
        maven {
            url = URI("https://maven.google.com")
        }
        google()
        jcenter()
    }
}