# Keep Claude API classes
-keep class com.anthropic.** { *; }
-keepclassmembers class com.anthropic.** { *; }

# Keep Supabase classes
-keep class io.github.jan.supabase.** { *; }

# Keep data classes used with Gson/JSON
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Room entities
-keep class com.skylens.data.local.entities.** { *; }

# Keep Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Keep MapLibre
-keep class org.maplibre.** { *; }
-keep class com.mapbox.** { *; }

# Keep Google Credential Manager
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
