plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.finalexam.project"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.finalexam.project"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.github.Dimezis:BlurView:version-1.6.6")

    // --- DEPENDENCIES MỚI CHO API/NETWORK VÀ COROUTINES ---
    // Retrofit cho các cuộc gọi API REST
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Converter để tự động phân tích JSON sang các data class Kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Kotlin Coroutines cho ViewModels và lifecycleScope
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // Lifecycle runtime ktx (cần cho flow)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    // ----------------------------------------------------

    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // Cần thiết cho delegate 'by viewModels()' trong Fragment
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.viewpager2)
    implementation(libs.recyclerview)
    implementation(libs.firebase.sessions)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}