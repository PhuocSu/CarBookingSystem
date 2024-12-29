plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id ("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)

}

android {
    namespace = "phuocsu.carbookingsystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "phuocsu.carbookingsystem"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.play.services.maps)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Internet



    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation ("com.google.firebase:firebase-database-ktx")
    implementation ("com.google.firebase:firebase-auth-ktx")

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")

    // TODO: Add the dependencies for any other Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication and Cloud Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")


    //FirebaseUI
    implementation("com.firebaseui:firebase-ui-auth:8.0.0") // Sử dụng phiên bản mới nhất


    //Firebase database
    implementation("com.google.firebase:firebase-database:21.0.0")

    //Location
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    //map
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    //GeoFire
    implementation ("com.firebase:geofire-android:3.1.0")
    //Firebase Storage => nhằm tạo folder chứa profile image
    implementation ("com.google.firebase:firebase-storage:20.2.1")
    //Gilde github -> Open Source for upload image
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0") // Nếu dùng Java
    //
    implementation ("androidx.cardview:cardview:1.0.0")
    //Thêm thư viện Google Place SDK -> AutoComplete
    implementation ("com.google.android.libraries.places:places:2.7.0")
    //Google-Directions-Android Library => Draw Routes
    implementation ("com.github.jd-alexander:library:1.1.0")
}