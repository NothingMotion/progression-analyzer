buildscript {
    dependencies {
        classpath(libs.gradle)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id ("com.google.dagger.hilt.android") version("2.49")  apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.5" apply false
//    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}