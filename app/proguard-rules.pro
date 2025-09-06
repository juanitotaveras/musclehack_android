# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;

}

#-keep class Pro
#-keepclasseswithmembernames class com.musclehack.targetedHypertrophyTraining.premium.PagerContentskeepattributes Signature
-keepclassmembers class com.musclehack.targetedHypertrophyTraining.premium.PagerContents {*;}
-keep class com.musclehack.targetedHypertrophyTraining.premium.PagerContents {*;}
-keepclassmembers class com.musclehack.targetedHypertrophyTraining.premium.** {<fields>;}
-keep class com.android.vending.billing.**
# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable


-keepnames class com.path.to.your.ParcelableArg
-keepnames class com.path.to.your.SerializableArg
-keepnames class com.path.to.your.EnumArg

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile


# added
-dontwarn retrofit2.Platform$Java8
-keep class retrofit2.Platform$Java8 { *;}


# keep webview
#-keep class android.support.v4.app.** { *; }
#-keep interface android.support.v4.app.** { *; }
#-keep class android.support.v7.app.** { *; }
#-keep interface android.support.v7.app.** { *; }


# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
