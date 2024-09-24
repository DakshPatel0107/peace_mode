plugins {
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.peacemode"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.peacemode"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
        viewBinding = true
    }

    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.circleimageview)
    implementation(libs.picasso)

    // Firebase dependencies
    implementation(libs.firebase.bom)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.auth)

    // Mapbox dependencies
    implementation(libs.android)
    implementation(libs.mapbox.search.android)
    implementation("com.mapbox.maps:android:10.7.0")
    implementation ("com.mapbox.search:autofill:2.3.0")
    implementation ("com.mapbox.search:discover:2.3.0")
    implementation ("com.mapbox.search:place-autocomplete:2.3.0")
    implementation ("com.mapbox.search:offline:2.3.0")
    implementation ("com.mapbox.search:mapbox-search-android:2.3.0")
    implementation ("com.mapbox.search:mapbox-search-android-ui:2.3.0")
    implementation ("com.google.android.gms:play-services-location:18.0.0")



    // Other dependencies
    implementation(libs.androidx.recyclerview)
    implementation(libs.play.services.location)
    implementation(libs.firebase.messaging.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}

