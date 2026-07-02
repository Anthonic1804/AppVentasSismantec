plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.acae30"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.acae30"
        minSdk = 26
        versionCode = 1
        versionName = "3.77"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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

    kotlinOptions {
        jvmTarget = "17"
    }

    // Configuración de KSP para Room
    ksp {
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }


}


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("androidx.core:core-ktx:1.13.0")

    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.annotation:annotation:1.8.2")

    implementation("androidx.activity:activity-ktx:1.8.2")

    //ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //Timber -> un Wrapper de Log.e
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Scanner cámara
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    //implementation("com.google.zxing:core:3.4.1")

    // Signature pad
    implementation("com.github.gcacace:signature-pad:1.3.1")

    // Exportar PDF
    implementation("com.itextpdf:itextg:5.5.10")

    // Picasso
    implementation("com.squareup.picasso:picasso:2.71828")

    // Lottie
    implementation("com.airbnb.android:lottie:6.0.0")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Room
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    implementation("androidx.activity:activity:1.13.0")
    implementation("androidx.compose.ui:ui:1.11.2")
    ksp("androidx.room:room-compiler:2.7.0")

    // Librería local
    implementation(files("libs/escposprinter-release.aar"))

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
