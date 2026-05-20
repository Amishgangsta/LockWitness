# Room — keep entity and DAO classes
-keep class com.lockwitness.app.data.incident.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# Device Admin receiver — must not be renamed
-keep class com.lockwitness.app.admin.LockWitnessDeviceAdminReceiver { *; }

# Foreground capture service
-keep class com.lockwitness.app.capture.LockWitnessCaptureService { *; }

# Google Play Billing — keep all public API
-keep class com.android.billingclient.** { *; }

# DataStore — keep generated serializers
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Prevent stripping Kotlin metadata needed for reflection
-keepattributes *Annotation*, Signature, Exception, InnerClasses, EnclosingMethod
