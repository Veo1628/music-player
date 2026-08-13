# Keep ExoPlayer
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }

# Keep Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
