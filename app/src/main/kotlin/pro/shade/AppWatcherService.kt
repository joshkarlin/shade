package pro.shade

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppWatcherService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var cache: Map<String, Boolean> = emptyMap()
    private var lastPackage: String? = null

    override fun onServiceConnected() {
        scope.launch {
            AppPreferences(this@AppWatcherService).all().collect { cache = it }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return

        // Ignore system UI — it's not a real app switch
        if (pkg == "com.android.systemui") return
        if (pkg == lastPackage) return
        lastPackage = pkg

        ColorCorrection.setGrayscale(this, cache[pkg] ?: false)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
