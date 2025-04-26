import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
//    id("com.google.devtools.ksp")

    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

}

val properties = Properties()
val propertiesFile = rootProject.file("local.properties")
if (propertiesFile.exists()){
    propertiesFile.inputStream().use {stream->
        properties.load(stream)
    }
}
android {
    namespace = "com.nothingmotion.brawlprogressionanalyzer"
    compileSdk = 35


    val progressionAnalyzerApi = properties.getProperty("api.progression_analyzer","")
    val brawlifyApi = properties.getProperty("api.brawlify","")
    val brawlifyCdnApi = properties.getProperty("api.brawlify.cdn","")
    val brawlNinjaApi = properties.getProperty("api.brawlninja","")
    val brawlNinjaCdn = properties.getProperty("api.brawlninja.cdn","")
    val frontEndSecret = properties.getProperty("api.application.front_end.key","")
    defaultConfig {
        applicationId = "com.nothingmotion.brawlprogressionanalyzer"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {

            buildConfigField("String","PROGRESSION_ANALYZER_API","\"$progressionAnalyzerApi\"")
            buildConfigField("String","BRAWLIFY_API_URL","\"$brawlifyApi\"")
            buildConfigField("String","APPLICATION_FRONTEND_API_KEY","\"$frontEndSecret\"")
            buildConfigField("String","BRAWLIFY_CDN_API_URL","\"$brawlifyCdnApi\"")
            buildConfigField("String","BRAWL_NINJA_API_URL","\"$brawlNinjaApi\"")
            buildConfigField("String","BRAWL_NINJA_CDN_API_URL","\"$brawlNinjaCdn\"")

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            buildConfigField("String","PROGRESSION_ANALYZER_API","\"$progressionAnalyzerApi\"")
            buildConfigField("String","BRAWLIFY_API_URL","\"$brawlifyApi\"")
            buildConfigField("String","APPLICATION_FRONTEND_API_KEY","\"$frontEndSecret\"")
            buildConfigField("String","BRAWLIFY_CDN_API_URL","\"$brawlifyCdnApi\"")
            buildConfigField("String","BRAWL_NINJA_API_URL","\"$brawlNinjaApi\"")
            buildConfigField("String","BRAWL_NINJA_CDN_API_URL","\"$brawlNinjaCdn\"")
            buildFeatures.buildConfig = true
        }
    }
    buildFeatures {
//        dataBinding = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    val fragment_version = "1.8.6"

    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")

    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // View model
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    // DI
    implementation ("com.google.dagger:hilt-android:2.49")
    kapt ("com.google.dagger:hilt-compiler:2.49")
    //implementation ("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt ("androidx.hilt:hilt-compiler:1.0.0")

    

    // Java language implementation
    implementation("androidx.fragment:fragment:$fragment_version")
    // Kotlin
    implementation("androidx.fragment:fragment-ktx:$fragment_version")

    implementation ("com.airbnb.android:lottie:6.3.0")

    // Preferences and Security
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    // UI Automator for system tests
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    
    // Fragment testing
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")

    implementation ("androidx.core:core-splashscreen:1.0.1")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // JWT
    implementation ("com.auth0:java-jwt:4.4.0")

    // Glide 
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt ("com.github.bumptech.glide:compiler:4.16.0")

    // worker
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    // dagger work
    implementation("androidx.hilt:hilt-work:1.0.0")
    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.6")


    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
//    implementation("androidx.room:room-runtime-android:$room_version")
    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")


    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
}
