package za.co.hbtechinc.isibalo.platform

/**
 * Kotlin Multiplatform / Compose Multiplatform abstraction layer for platform discovery.
 * Supports Android, Desktop (JVM), iOS, and Web (Wasm).
 */
enum class PlatformType(val displayName: String) {
    ANDROID("Android OS"),
    DESKTOP_JVM("Desktop (Windows/Mac/Linux)"),
    IOS("Apple iOS"),
    WEB_WASM("Web (Wasm)")
}

interface PlatformContext {
    val platformType: PlatformType
    val platformName: String
    val isMobile: Boolean get() = platformType == PlatformType.ANDROID || platformType == PlatformType.IOS
    val isDesktop: Boolean get() = platformType == PlatformType.DESKTOP_JVM
}

class AndroidPlatformContext(val context: android.content.Context) : PlatformContext {
    override val platformType: PlatformType = PlatformType.ANDROID
    override val platformName: String = "Android SDK ${android.os.Build.VERSION.SDK_INT}"
}

object CurrentPlatform {
    var context: PlatformContext? = null
    val type: PlatformType get() = context?.platformType ?: PlatformType.ANDROID
}
