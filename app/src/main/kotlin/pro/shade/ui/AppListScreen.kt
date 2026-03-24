package pro.shade.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.shade.AppPreferences
import pro.shade.ColorCorrection

data class AppInfo(
    val name: String,
    val packageName: String,
)

private val HEADLESS_SYSTEM_PACKAGES = setOf(
    "com.android.launcher3",
    "com.android.systemui",
    "com.lightos",
)

enum class AppFilter(val label: String) {
    MY_APPS("MY APPS"),
    BUILT_IN("BUILT-IN"),
}

private fun rainbowName(text: String) = buildAnnotatedString {
    val upper = text.uppercase()
    val len = maxOf(upper.length - 1, 1)
    upper.forEachIndexed { i, ch ->
        val hue = (i.toFloat() / len) * 360f
        withStyle(SpanStyle(color = Color.hsl(hue, 0.62f, 0.78f))) {
            append(ch)
        }
    }
}

@Composable
fun AppListScreen(prefs: AppPreferences, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val myApps = remember { mutableStateListOf<AppInfo>() }
    val builtIn = remember { mutableStateListOf<AppInfo>() }
    val allPackages = remember { mutableStateListOf<String>() }

    val grayscaleMap by prefs.all().collectAsState(initial = emptyMap())
    var filter by remember { mutableStateOf(AppFilter.MY_APPS) }

    val hasPermission = remember { mutableStateOf(ColorCorrection.hasPermission(context)) }
    val serviceEnabled = remember { mutableStateOf(isServiceEnabled(context)) }

    LaunchedEffect(Unit) {
        val (my, bi) = withContext(Dispatchers.IO) { loadApps(context) }
        myApps.addAll(my)
        builtIn.addAll(bi)
        allPackages.addAll((my + bi).map { it.packageName })
    }

    // Apply correction immediately when Shade's own setting changes (service won't fire for foreground app)
    val selfShaded = grayscaleMap[context.packageName] ?: false
    LaunchedEffect(selfShaded) {
        ColorCorrection.setGrayscale(context, selfShaded)
    }

    val activeList = when (filter) {
        AppFilter.MY_APPS -> myApps
        AppFilter.BUILT_IN -> builtIn
    }

    val activePackages = activeList.map { it.packageName }
    val allShaded = activePackages.isNotEmpty() && activePackages.all { grayscaleMap[it] == true }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // Title + bulk action
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selfShaded) {
                    Text(
                        text = "SHADE",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 8.sp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { scope.launch { prefs.set(context.packageName, false) } },
                    )
                } else {
                    Text(
                        text = rainbowName("SHADE"),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 8.sp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { scope.launch { prefs.set(context.packageName, true) } },
                    )
                }
                Text(
                    text = if (allShaded) "NO SHADE" else "FULL SHADE",
                    color = Color(0xFF888888),
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .border(1.dp, Color(0xFF333333))
                        .clickable { scope.launch { prefs.setAll(activePackages, !allShaded) } }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
        }

        // Permission banners
        if (!hasPermission.value) {
            item {
                Banner(
                    label = "WRITE_SECURE_SETTINGS not granted",
                    detail = "Run ./grant.sh with device connected",
                )
            }
        }
        if (!serviceEnabled.value) {
            item {
                Banner(
                    label = "Accessibility service inactive",
                    detail = "Run ./grant.sh or enable in Settings → Accessibility",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                )
            }
        }

        // Filter tabs
        item {
            FilterRow(active = filter, onSelect = { filter = it })
            HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
        }

        // App list
        items(activeList, key = { "${filter}_${it.packageName}" }) { app ->
            val isGrayscale = grayscaleMap[app.packageName] ?: false
            AppRow(
                app = app,
                isGrayscale = isGrayscale,
                shadeMode = selfShaded,
                onTap = { scope.launch { prefs.set(app.packageName, !isGrayscale) } },
            )
            HorizontalDivider(color = Color(0xFF111111), thickness = 1.dp)
        }
    }
}

@Composable
private fun FilterRow(active: AppFilter, onSelect: (AppFilter) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AppFilter.entries.forEach { f ->
            Text(
                text = f.label,
                color = if (f == active) Color.White else Color(0xFF444444),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(f) }
                    .padding(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun Banner(label: String, detail: String, onClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D0D))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(text = label, color = Color(0xFFAAAAAA), fontSize = 11.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = detail, color = Color(0xFF555555), fontSize = 10.sp)
    }
    HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
}

@Composable
private fun AppRow(app: AppInfo, isGrayscale: Boolean, shadeMode: Boolean, onTap: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() }
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isGrayscale && !shadeMode) {
            Text(
                text = rainbowName(app.name),
                fontSize = 22.sp,
                modifier = Modifier.weight(1f),
            )
        } else {
            Text(
                text = app.name.uppercase(),
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f),
            )
        }
        ToggleIndicator(isOn = isGrayscale)
    }
}

@Composable
private fun ToggleIndicator(isOn: Boolean) {
    Canvas(modifier = Modifier.size(width = 36.dp, height = 16.dp)) {
        val r = size.height / 2f
        val strokePx = 1.5.dp.toPx()
        val lineY = size.height / 2f
        if (isOn) {
            val cx = size.width - r
            drawLine(Color.White, Offset(0f, lineY), Offset(cx, lineY), strokePx)
            drawCircle(Color.White, radius = r, center = Offset(cx, lineY))
        } else {
            val cx = r
            drawLine(Color.White, Offset(cx, lineY), Offset(size.width, lineY), strokePx)
            drawCircle(Color.Black, radius = r, center = Offset(cx, lineY))
            drawCircle(Color.White, radius = r - strokePx / 2f, center = Offset(cx, lineY), style = Stroke(strokePx))
        }
    }
}

private fun isServiceEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    val component = android.content.ComponentName(context, pro.shade.AppWatcherService::class.java)
    return flat.contains(component.flattenToString())
}

private fun loadApps(context: Context): Pair<List<AppInfo>, List<AppInfo>> {
    val pm = context.packageManager

    val launcherPackages = pm.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        PackageManager.MATCH_ALL,
    ).map { it.activityInfo.packageName }.toSet()

    val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    fun android.content.pm.ApplicationInfo.toAppInfo(): AppInfo? = runCatching {
        AppInfo(
            name = pm.getApplicationLabel(this).toString(),
            packageName = packageName,
        )
    }.getOrNull()

    val myApps = allApps
        .filter {
            it.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                it.packageName in launcherPackages
        }
        .mapNotNull { it.toAppInfo() }
        .distinctBy { it.packageName }
        .sortedBy { it.name.lowercase() }

    val builtIn = allApps
        .filter {
            it.packageName in HEADLESS_SYSTEM_PACKAGES ||
                (it.flags and ApplicationInfo.FLAG_SYSTEM != 0 && it.packageName in launcherPackages)
        }
        .mapNotNull { it.toAppInfo() }
        .distinctBy { it.packageName }
        .sortedBy { it.name.lowercase() }

    return Pair(myApps, builtIn)
}
