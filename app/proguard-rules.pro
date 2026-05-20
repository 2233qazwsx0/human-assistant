# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.**
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** { *; }

# Keep all @Serializable classes in the app
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.netty.**

# Keep Kotlinx Coroutines
-keepnames class kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Kotlin Metadata
-keep class kotlin.Metadata { *; }

# Keep AndroidX and Lifecycle
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keep class androidx.lifecycle.** { *; }

# Keep app-specific data classes
-keep class com.humanassistant.server.PendingRequest { *; }
-keep class com.humanassistant.FriendInfo { *; }
-keep class com.humanassistant.server.ChatMessage { *; }
-keep class com.humanassistant.server.ChatCompletionRequest { *; }
-keep class com.humanassistant.server.ChatCompletionChoice { *; }
-keep class com.humanassistant.server.ChatCompletionResponse { *; }
-keep class com.humanassistant.server.ErrorResponse { *; }
-keep class com.humanassistant.server.ErrorDetail { *; }

# Keep Kotlin reflect
-dontwarn kotlin.reflect.**
-keep class kotlin.reflect.** { *; }
