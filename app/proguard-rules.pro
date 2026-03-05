# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Room
-keep class androidx.room.** { *; }
-keepnames class androidx.room.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.lifecycle.HiltViewModelFactory
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.ViewModelInject <init>(...);
}
-keep @dagger.hilt.android.scopes.* class *

# Coil
-keep class coil.** { *; }

# Media3
-keep class androidx.media3.** { *; }
