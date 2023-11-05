plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 29
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {

    implementation("com.github.bumptech.glide:glide:4.16.0") // for image url load

    implementation("com.android.volley:volley:1.2.1") // for api calls via volley

    implementation("com.squareup.retrofit2:retrofit:2.9.0") // for api calls via retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // for api calls via retrofit
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // for api calls via retrofit

    implementation("org.tensorflow:tensorflow-lite-support:0.3.1") // for tflite ml model
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")

    implementation("com.google.android.gms:play-services-location:21.0.1") // for gettting lat/long


    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}