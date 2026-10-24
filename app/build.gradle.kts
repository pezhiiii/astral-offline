plugins { id("com.android.application"); kotlin("android") }
android {
 namespace = "org.bl3.astral"
 compileSdk = 35
 defaultConfig {
  applicationId = "org.bl3.astral"; minSdk = 28; targetSdk = 35
  versionCode = 3; versionName = "0.1.2-dev"
  ndk { abiFilters += "arm64-v8a" }
  externalNativeBuild { cmake { cppFlags += "-std=c++17" } }
 }
 externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" } }
 ndkVersion = "27.2.12479018"
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
