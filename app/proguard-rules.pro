# Proguard rules for Word Anchor (تثبيت الكلمات)

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.deutscherinnerungen.app.data.entity.** { *; }
-keep class com.deutscherinnerungen.app.backup.** { *; }

# Media3 ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Compose
-dontwarn androidx.compose.**
