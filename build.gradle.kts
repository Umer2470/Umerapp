// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}
android {
    // آپ کا باقی تمام کوڈ ویسے ہی رہے گا ...

    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "2.0"
    }
}
