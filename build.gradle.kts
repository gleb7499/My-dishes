plugins {
    alias(libs.plugins.android.application)
    id("org.sonarqube") version "6.2.0.5505"
}

sonar {
    properties {
        property("sonar.projectKey", "gleb7499_My-dishes")
        property("sonar.organization", "gleb7499-1")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

android {
    namespace = "com.mydishes.mydishes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mydishes.mydishes"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.jsoup)
    implementation(libs.glide)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
}