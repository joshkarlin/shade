package pro.shade

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppWatcherService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var prefs: AppPreferences
    private var lastPackage: String? = null

    override fun onServiceConnected() {
        prefs = AppPreferences(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == lastPackage) return
        lastPackage = pkg

        scope.launch {
            val grayscale = prefs.isGrayscale(pkg).first()
            ColorCorrection.setGrayscale(this@AppWatcherService, grayscale)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
