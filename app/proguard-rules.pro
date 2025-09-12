# For more details on Proguard, ee
#   http://developer.android.com/guide/developing/tools/proguard.html

# Required for WebView with JS!
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Required from Gson to create objects from Json.
-keep class com.musclehack.targetedHypertrophyTraining.premium.PagerContents { *; }
-keep class com.musclehack.targetedHypertrophyTraining.premium.PagerContents$Item { *; }
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


# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
