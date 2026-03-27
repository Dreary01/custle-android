package ru.custle.mobile.core.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ru.custle.mobile.core.model.Session
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "custle_session")

class SessionStore(
    private val context: Context,
) {
    private object Keys {
        val token = stringPreferencesKey("token")
        val workspaceId = stringPreferencesKey("workspace_id")
    }

    val sessionFlow: Flow<Session?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val token = prefs[Keys.token] ?: return@map null
            Session(
                token = token,
                activeWorkspaceId = prefs[Keys.workspaceId],
            )
        }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.token] = token
        }
    }

    suspend fun saveWorkspace(id: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.workspaceId] = id
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
