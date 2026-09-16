# ------------------------------------------------------------------
# Appointment Booking App - ProGuard rules
# ------------------------------------------------------------------

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Google/Firebase SDKs
-keep class com.google.firebase.** { *; }

# Keep data models (used with Firestore serialization)
-keep class com.example.appointmentapp.data.model.** { *; }