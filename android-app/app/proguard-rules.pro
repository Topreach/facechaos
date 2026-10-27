# MediaPipe's native graph calls Java framework entry points by name.
# Keep the JNI bridge, while allowing optimization/obfuscation of game code.
-keep class com.google.mediapipe.framework.** { *; }
-keep class com.google.mediapipe.tasks.core.** { *; }
-keep class com.google.mediapipe.proto.** { *; }
# Flogger locates its caller from the Java stack; inlining/renaming breaks it.
-keep class com.google.common.flogger.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
-keepattributes Signature,InnerClasses,EnclosingMethod
# Optional MediaPipe profiling/template APIs and build-time AutoValue annotation
# are absent from the published Android AARs; Face Chaos does not invoke them.
-dontwarn com.google.auto.value.extension.memoized.Memoized
-dontwarn com.google.mediapipe.proto.CalculatorProfileProto$CalculatorProfile
-dontwarn com.google.mediapipe.proto.GraphTemplateProto$CalculatorGraphTemplate
