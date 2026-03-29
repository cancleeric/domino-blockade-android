# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Firebase — keep model classes used by Firestore
-keep class com.cancleeric.dominoblockade.data.model.** { *; }

# Firebase Firestore
-keep class com.google.firebase.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
