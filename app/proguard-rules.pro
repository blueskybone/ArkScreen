# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

-keep class com.blueskybone.arkscreen.task.** { *; }
-keep class com.blueskybone.arkscreen.common.** { *; }
-keep class com.blueskybone.arkscreen.playerinfo.** { *; }
# Retrofit services and Jackson DTOs are inspected through annotations and reflection.
# The network package moved under data during the refactor; keep the current package
# for test/release builds instead of relying on the obsolete pre-refactor rule.
-keep class com.blueskybone.arkscreen.data.network.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault
-keepattributes RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,MethodParameters

-keep class android.hardware.display.** { *; }
-keep class android.media.projection.** { *; }
-keep class com.blueskybone.arkscreen.common.** { *; }

# JNI exports use the fully-qualified ImageProcessor class and method names.
-keep class com.blueskybone.arkscreen.ui.recruit.ocr.ImageProcessor { *; }
