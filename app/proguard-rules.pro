# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Google Play Services Auth
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Google Drive API
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.services.drive.**

# Guava
-dontwarn com.google.common.**

# AndroidX Core
-keep class androidx.core.content.FileProvider { *; }
-keep class androidx.core.app.ComponentActivity { *; }

# AndroidX AppCompat
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

# Google Material
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ConstraintLayout
-keep class androidx.constraintlayout.widget.** { *; }
-keep class androidx.constraintlayout.solver.** { *; }
-dontwarn androidx.constraintlayout.**

# Lifecycle LiveData & ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class androidx.lifecycle.LiveData {
    *;
}

# Google HTTP Client Gson
-keep class com.google.api.client.json.gson.** { *; }
-dontwarn com.google.api.client.json.gson.**

# Google API Client for Android
-keep class com.google.api.client.googleapis.extensions.android.** { *; }
-dontwarn com.google.api.client.googleapis.extensions.android.**

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory {
    <init>();
}
-keepclassmembers class kotlinx.coroutines.CoroutineExceptionHandler {
    <init>(...);
}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}

# AndroidX Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ZXing (For QR Code)
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }
-dontwarn com.journeyapps.barcodescanner.**
-dontwarn com.google.zxing.**