# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/JeremyShore/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Xrm
-keep class com.microsoft.xrm.sdk.** { *; }
-keep class * extends com.microsoft.xrm.sdk.Entity { *; }

# Google
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

# Debugging
-dontwarn com.facebook.stetho.**
-keep class com.facebook.stetho.** { *; }

-dontwarn okio.**
-keep class okio.** { *; }

#Butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}