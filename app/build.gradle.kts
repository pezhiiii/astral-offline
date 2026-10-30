plugins { id("com.android.application"); kotlin("android") }
android {
 namespace = "org.bl3.astral"
 compileSdk = 35
 signingConfigs {
  getByName("debug") {
   storeFile = rootProject.file(".signing/astral-debug.keystore")
   storePassword = "android"
   keyAlias = "androiddebugkey"
   keyPassword = "android"
  }
 }
 defaultConfig {
  applicationId = "org.bl3.astral"; minSdk = 28; targetSdk = 35
  versionCode = 67; versionName = "0.1.66-dev"
  ndk { abiFilters += "arm64-v8a" }
  externalNativeBuild { cmake { cppFlags += "-std=c++17" } }
 }
 buildTypes { getByName("debug") { signingConfig = signingConfigs.getByName("debug") } }
 externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" } }
 ndkVersion = "27.2.12479018"
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}

 dependencies { testImplementation("junit:junit:4.13.2") }
