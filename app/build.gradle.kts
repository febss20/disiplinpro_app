plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

val localPropertiesFile = rootProject.file("local.properties")
val localPropertiesLines = if (localPropertiesFile.exists()) {
    localPropertiesFile.readLines()
} else {
    emptyList()
}

val geminiApiKey = localPropertiesLines
    .find { it.startsWith("GEMINI_API_KEY=") }
    ?.substringAfter("=")
    ?: ""

val awsAccessKey = localPropertiesLines
    .find { it.startsWith("AWS_ACCESS_KEY=") }
    ?.substringAfter("=")
    ?: ""

val awsSecretKey = localPropertiesLines
    .find { it.startsWith("AWS_SECRET_KEY=") }
    ?.substringAfter("=")
    ?: ""

android {
    namespace = "com.dsp.disiplinpro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dsp.disiplinpro"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "AWS_ACCESS_KEY", "\"$awsAccessKey\"")
        buildConfigField("String", "AWS_SECRET_KEY", "\"$awsSecretKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        buildConfig = true
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
    implementation("androidx.credentials:credentials:1.3.0-alpha01")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0-alpha01")
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
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.google.accompanist:accompanist-pager:0.27.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.1-alpha")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.test:core:1.5.0")
    implementation("androidx.compose.material:material:1.5.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("com.amazonaws:aws-android-sdk-s3:2.20.0")
    implementation("com.amazonaws:aws-android-sdk-core:2.20.0")
    implementation("com.amazonaws:aws-android-sdk-cognito:2.20.0")
    implementation("com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.20.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}