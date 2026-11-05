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

# Retrofit services and Jackson DTOs are inspected through annotations and reflection.
-keep interface com.blueskybone.arkscreen.data.network.ApiService { *; }
-keep class com.blueskybone.arkscreen.data.network.model.** { *; }
# Jackson also deserializes models outside the network package.
-keep class com.blueskybone.arkscreen.data.resource.model.** { *; }
-keep class com.blueskybone.arkscreen.data.gacha.GachaBackupCodec$ExportBundle { *; }
-keep class com.blueskybone.arkscreen.domain.model.gacha.Record { *; }
-keep class com.blueskybone.arkscreen.domain.model.license.OpenSourceLibrary { *; }
-keep class kotlin.Metadata { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault
-keepattributes RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,MethodParameters

# JNI exports use the fully-qualified ImageProcessor class and method names.
-keep class com.blueskybone.arkscreen.ui.recruit.ocr.ImageProcessor { *; }
