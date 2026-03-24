package pro.shade

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
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
    private var imes: Set<String> = emptySet()

    override fun onServiceConnected() {
        // Detect all input method packages — these are overlays, not real app switches
        imes = packageManager.queryIntentServices(
            Intent("android.service.inputmethod.InputMethodService"),
            PackageManager.MATCH_ALL
        ).map { it.serviceInfo.packageName }.toSet()

        scope.launch {
            AppPreferences(this@AppWatcherService).all().collect { cache = it }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg in imes) return
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
