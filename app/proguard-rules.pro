# Add project specific ProGuard rules here.

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Car App Library
-keep class * extends androidx.car.app.CarAppService
-keep class * extends androidx.car.app.Screen

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
