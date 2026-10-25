plugins {
    id("com.android.application") version "9.2.1" apply false
    // AGP 9 has built-in Kotlin, so org.jetbrains.kotlin.android is no longer applied.
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
}
