plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "app.opass.ccip"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.opass.ccip"
        minSdk = 24
        targetSdk = 34
        versionCode = 55
        versionName = "3.5.0"

        manifestPlaceholders["manifestApplicationId"] = "$applicationId"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
    }
    packaging {
        resources {
            excludes += listOf("META-INF/atomicfu.kotlin_module")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    lint {
        lintConfig = file("lint.xml")
    }

    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {

    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.core)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.livedata)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)

    // Google
    implementation(libs.google.material)
    implementation(libs.google.flexbox)
    implementation(libs.google.gson)
    implementation(libs.google.firebase.analytics)

    // Coil
    implementation(libs.coil)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // MarkWon
    implementation(libs.markwon.core)
    implementation(libs.markwon.linkify)

    // Okhttp
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.converter.gson)

    // OneSignal
    implementation(libs.onesignal)

    // Zxing
    implementation(libs.zxing.android.embedded)
}
