package pro.shade.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.shade.AppPreferences
import pro.shade.ColorCorrection

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
)

@Composable
fun AppListScreen(prefs: AppPreferences, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val apps = remember { mutableStateListOf<AppInfo>() }
    val grayscaleMap by prefs.all().collectAsState(initial = emptyMap())
    var query by remember { mutableStateOf("") }

    val hasPermission = remember { mutableStateOf(ColorCorrection.hasPermission(context)) }
    val serviceEnabled = remember { mutableStateOf(isServiceEnabled(context)) }

    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) { installedApps(context) }
        apps.addAll(loaded)
    }

    val filtered = if (query.isEmpty()) apps
    else apps.filter { it.name.contains(query, ignoreCase = true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // Header
        item {
            Text(
                text = "SHADE",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 5.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
            )
        }

        // Permission banners
        if (!hasPermission.value) {
            item {
                Banner(
                    label = "WRITE_SECURE_SETTINGS not granted",
                    detail = "adb shell pm grant pro.shade android.permission.WRITE_SECURE_SETTINGS",
                )
            }
        }
        if (!serviceEnabled.value) {
            item {
                Banner(
                    label = "Accessibility service inactive",
                    detail = "Enable Shade in Settings → Accessibility",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                )
            }
        }

        // Search
        item {
            SearchField(
                value = query,
                onValueChange = { query = it },
            )
            HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
        }

        // App rows
        items(filtered, key = { it.packageName }) { app ->
            AppRow(
                app = app,
                isGrayscale = grayscaleMap[app.packageName] ?: false,
                onToggle = { enabled ->
                    scope.launch { prefs.set(app.packageName, enabled) }
                },
            )
            HorizontalDivider(
                color = Color(0xFF111111),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 72.dp),
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
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) {
            Text(text = "search", color = Color(0xFF333333), fontSize = 14.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AppRow(app: AppInfo, isGrayscale: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val bitmap = remember(app.packageName) {
            app.icon.toBitmap(96, 96).asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = app.name,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = isGrayscale,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.White,
                uncheckedThumbColor = Color(0xFF444444),
                uncheckedTrackColor = Color(0xFF1C1C1C),
                uncheckedBorderColor = Color(0xFF2A2A2A),
            ),
        )
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

private fun installedApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        .map { ri ->
            AppInfo(
                name = ri.loadLabel(pm).toString(),
                packageName = ri.activityInfo.packageName,
                icon = ri.loadIcon(pm),
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.name.lowercase() }
}
