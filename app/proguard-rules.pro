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

#-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
#    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
#}

#-keepclasseswithmembers class **.R$* {
#    public static final int define_*;
#}
#### No obfuscation
#-dontobfuscate
# -keep public class na.komi.kodesh.ui.** { *; }
# -keep public class na.komi.kodesh.util.skate.** { *; }
# -keepclassmembers class na.komi.kodesh.util.skate.** { *; }
-keep public class na.komi.kodesh.ui.main.** { *; }
-keepclassmembers class na.komi.kodesh.ui.main.** { *; }
-keep public class na.komi.kodesh.ui.find.** { *; }
-keepclassmembers class na.komi.kodesh.ui.find.** { *; }
