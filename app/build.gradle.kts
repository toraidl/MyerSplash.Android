@file:Suppress("ObjectLiteralToLambda")

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("realm-android")
}

apply(from = "../version.gradle.kts")
apply(from = "../source.gradle.kts")

fun Project.hasLocalProperty(key: String): Boolean {
    return extra.properties.containsKey(key)
}

val kotlinVersion: String by extra
val kotlinCoroutineVersion: String by extra
val realmVersion: String by extra
val androidXVersion: String by extra
val androidXAppCompatVersion: String by extra
val butterKnifeVersion: String by extra
val retrofitVersion: String by extra
val frescoVersion: String by extra
val constraintLayoutVersion: String by extra
val minVersion: Int by extra
val targetVersion: Int by extra
val compileVersion: Int by extra
val buildToolVersion: String by extra

val appVersionCode: Int by extra
val appVersionName: String by extra

android {
    signingConfigs {
        create("release") {
            val key: String by project
            val password: String by project

            keyAlias = key
            keyPassword = password
            storeFile = File("keystore/ms.jks")
            storePassword = password
        }
    }

    compileSdkVersion(compileVersion)
    buildToolsVersion(buildToolVersion)

    defaultConfig {
        applicationId = "com.juniperphoton.myersplash"
        minSdkVersion(minVersion)
        targetSdkVersion(targetVersion)
        versionCode = appVersionCode
        versionName = appVersionName

        val unsplashKey: String by project
        buildConfigField("String", "UNSPLASH_APP_KEY", "\"$unsplashKey\"")
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            resValue("string", "authorities", "com.juniperphoton.myersplash.debug.fileProvider")

            mapOf("APP_NAME" to "MyerSplash DEBUG")
                    .forEach {
                        manifestPlaceholders[it.key] = it.value
                    }
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")

            resValue("string", "authorities", "com.juniperphoton.myersplash.fileProvider")
            mapOf("APP_NAME" to "MyerSplash")
                    .forEach {
                        manifestPlaceholders[it.key] = it.value
                    }

            android.applicationVariants.all(object : Action<ApplicationVariant> {
                override fun execute(t: ApplicationVariant) {
                    if (t.buildType != buildTypes.getByName("release")) {
                        return
                    }

                    t.outputs.all(object : Action<BaseVariantOutput> {
                        override fun execute(t: BaseVariantOutput) {
                            val output = t as? BaseVariantOutputImpl ?: return
                            output.outputFileName =
                                    "myersplash_release_build.${defaultConfig.versionName}" +
                                            ".${defaultConfig.versionCode}.apk"
                        }
                    })
                }
            })
        }
    }

    sourceSets {
        get("main").java.srcDirs("src/main/kotlin")
        get("main").res.srcDirs("src/debug")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutineVersion")

    testImplementation("junit:junit:4.12")

    implementation("androidx.legacy:legacy-support-v13:$androidXVersion")
    implementation("androidx.appcompat:appcompat:$androidXAppCompatVersion")
    implementation("com.google.android.material:material:$androidXVersion")
    implementation("androidx.cardview:cardview:$androidXVersion")
    implementation("androidx.browser:browser:$androidXVersion")
    implementation("androidx.palette:palette:$androidXVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")

    implementation("com.jakewharton:butterknife:$butterKnifeVersion")
    kapt("com.jakewharton:butterknife-compiler:$butterKnifeVersion")

    implementation("com.facebook.fresco:fresco:$frescoVersion")
    implementation("io.reactivex.rxjava2:rxjava:2.1.5")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.1")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")
    implementation("com.google.code.gson:gson:2.8.0")
    implementation("org.greenrobot:eventbus:3.0.0")
    implementation("com.google.android:flexbox:1.1.0")
    implementation("com.juniperphoton:flipperlayout:1.2.5")
    kapt("io.realm:realm-annotations:3.0.0")
    kapt("io.realm:realm-annotations-processor:3.0.0")

    implementation("com.google.dagger:dagger:2.11")
    kapt("com.google.dagger:dagger-compiler:2.11")
}
