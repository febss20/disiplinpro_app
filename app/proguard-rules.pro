# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Optimisasi dasar
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt

# Android khusus
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View

# Jetpack Compose
-keepclasseswithmembers class * {
    @androidx.compose.ui.tooling.preview.Preview *;
}

# OkHttp dan SSL Pinning
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepclassmembers class com.dsp.disiplinpro.data.security.** { *; }

# TwoFactorAuthManager
-keep class com.dsp.disiplinpro.data.security.TwoFactorAuthManager { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-keep class com.dsp.disiplinpro.data.model.** { *; }

# Reflection
-keepattributes InnerClasses

# Serialisasi
-keepattributes *Annotation*, Signature, Exception

# Enkripsi dan security
-keep class javax.crypto.** { *; }
-keep class javax.security.** { *; }
-keep class java.security.** { *; }
-keep class androidx.security.crypto.** { *; }

# Mencegah obfuscation pada kelas keamanan kita
-keep class com.dsp.disiplinpro.data.security.** { *; }
-keep class com.dsp.disiplinpro.util.ValidationUtils { *; }
-keep class com.dsp.disiplinpro.util.SecureErrorHandler { *; }

# AWS SDK
-keep class com.amazonaws.** { *; }
-keep class org.apache.commons.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

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