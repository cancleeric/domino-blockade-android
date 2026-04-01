# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep source file names and line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Kotlin ----------------------------------------------------------------
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ---- Hilt / Dagger ---------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# ---- Room ------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# ---- Firebase / Firestore --------------------------------------------------
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ---- DataStore / Protobuf --------------------------------------------------
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ---- Coroutines ------------------------------------------------------------
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ---- Jetpack Compose -------------------------------------------------------
-dontwarn androidx.compose.**

# ---- App domain models -----------------------------------------------------
-keep class com.cancleeric.dominoblockade.domain.model.** { *; }
-keep class com.cancleeric.dominoblockade.data.local.entity.** { *; }
