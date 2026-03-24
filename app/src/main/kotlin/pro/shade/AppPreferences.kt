package pro.shade

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shade_prefs")

class AppPreferences(private val context: Context) {

    fun isGrayscale(packageName: String): Flow<Boolean> =
        context.dataStore.data.map { it[booleanPreferencesKey(packageName)] ?: false }

    fun all(): Flow<Map<String, Boolean>> =
        context.dataStore.data.map { prefs ->
            prefs.asMap().entries.associate { (key, value) ->
                key.name to (value as? Boolean ?: false)
            }
        }

    suspend fun set(packageName: String, grayscale: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey(packageName)] = grayscale }
    }
}
