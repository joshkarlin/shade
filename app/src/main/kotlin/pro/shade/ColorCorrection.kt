package pro.shade

import android.content.Context
import android.provider.Settings

object ColorCorrection {

    // These are @hide constants — must use string literals
    private const val DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled"
    private const val DALTONIZER = "accessibility_display_daltonizer"
    private const val GRAYSCALE = 0

    fun setGrayscale(context: Context, enabled: Boolean) {
        val resolver = context.contentResolver
        Settings.Secure.putInt(resolver, DALTONIZER_ENABLED, if (enabled) 1 else 0)
        if (enabled) {
            Settings.Secure.putInt(resolver, DALTONIZER, GRAYSCALE)
        }
    }

    fun hasPermission(context: Context): Boolean = try {
        val current = Settings.Secure.getInt(context.contentResolver, DALTONIZER_ENABLED, 0)
        Settings.Secure.putInt(context.contentResolver, DALTONIZER_ENABLED, current)
        true
    } catch (_: SecurityException) {
        false
    }
}
