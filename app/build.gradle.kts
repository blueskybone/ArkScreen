plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    namespace = "com.blueskybone.arkscreen"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blueskybone.arkscreen"
        minSdk = 26
        targetSdk = 34
        versionCode = 8
        versionName = "2.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
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

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    ksp {
        arg("room.generateKotlin", "true")
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.paging:paging-runtime:3.3.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")

    implementation("io.insert-koin:koin-core:3.4.0")
    implementation("io.insert-koin:koin-android:3.4.0")
    implementation("io.insert-koin:koin-android-compat:3.4.0")

    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")

    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.noties.markwon:core:4.2.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.1")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("com.nex3z:flow-layout:1.3.3")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")

    implementation("com.github.getActivity:EasyWindow:10.62")
    implementation("com.github.getActivity:Toaster:12.6")

    implementation("androidx.work:work-runtime:2.9.0")
}