# Keep Firestore model classes (reflection-based serialization)
-keep class com.example.appointmentapp.data.model.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**