# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep data classes / domain models
-keepclassmembers class com.cancleeric.dominoblockade.** {
    public <fields>;
    public <methods>;
}
