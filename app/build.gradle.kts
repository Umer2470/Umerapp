import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

// Use the Kotlin Gradle Plugin 2.x compilerOptions block for Kotlin 2.0+
kotlin {
  compilerOptions {
    // Keep JVM target aligned with project's Java compatibility
    jvmTarget.set(JvmTarget.JVM_17)
    // You can add common freeCompilerArgs here if needed, for example:
    // freeCompilerArgs.addAll(listOf("-Xopt-in=kotlin.RequiresOptIn"))
  }
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.aistudio.umairstore.kzpqv"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      if (file(keystorePath).exists()) {
        storeFile = file(keystorePath)
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      if (file(keystorePath).exists()) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
    debug {
      // Do not force a custom signingConfig for debug; use the default debug keystore
      isDebuggable = true
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  // kotlinOptions (older DSL) removed in favor of the Kotlin 2.x compilerOptions above

  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  // Use Compose BOM (version aligned with catalog). This lets us omit individual Compose versions.
  implementation(platform("androidx.compose:compose-bom:2025.02.00"))

  // AndroidX / Lifecycle
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
  implementation("androidx.activity:activity-compose:1.10.0")

  // Jetpack Compose UI (BOM managed versions)
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-core")
  implementation("androidx.compose.material:material-icons-extended")

  // Other libs
  implementation("androidx.biometric:biometric:1.1.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
  implementation("androidx.navigation:navigation-compose:2.8.7")
  implementation("androidx.room:room-ktx:2.7.0-alpha13")
  implementation("androidx.room:room-runtime:2.7.0-alpha13")
  implementation("io.coil-kt:coil-compose:2.7.0")
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
  implementation("com.squareup.okhttp3:okhttp:4.10.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
  implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
  implementation("com.google.zxing:core:3.5.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

  // Testing
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.robolectric:robolectric:4.16.1")
  testImplementation("io.github.takahirom.roborazzi:roborazzi:1.59.0")
  testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.59.0")
  androidTestImplementation(platform("androidx.compose:compose-bom:2025.02.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test:runner:1.6.2")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  // KSP processors
  ksp("androidx.room:room-compiler:2.7.0-alpha13")
  ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")
}
