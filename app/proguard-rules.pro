# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** { *; }

# Keep Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep Kotlinx Coroutines
-keepnames class kotlinx.coroutines.**
-dontwarn kotlinx.coroutines.**

# Keep AndroidX and Lifecycle
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
