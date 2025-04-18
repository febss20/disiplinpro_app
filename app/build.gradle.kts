plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.disiplinpro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.disiplinpro"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:4.8.0")
    testImplementation ("org.mockito:mockito-inline:4.8.0")
    testImplementation ("androidx.arch.core:core-testing:2.1.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("com.github.skydoves:landscapist-coil:2.3.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.github.skydoves:landscapist-placeholder:2.3.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.3")
    implementation("androidx.compose.runtime:runtime:1.7.8")
    implementation ("androidx.work:work-runtime-ktx:2.8.1")
    implementation ("com.google.accompanist:accompanist-pager:0.27.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("androidx.test:core:1.5.0")
    implementation("androidx.compose.material:material:1.5.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("com.amazonaws:aws-android-sdk-s3:2.20.0")
    implementation("com.amazonaws:aws-android-sdk-core:2.20.0")
    implementation("com.amazonaws:aws-android-sdk-cognito:2.20.0")
    implementation("com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.20.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}