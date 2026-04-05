package com.example.app_my_university.data.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.app_my_university.data.auth.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themePreferencesDataStore by preferencesDataStore(name = "theme_prefs")

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ThemePreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
) {

    private fun preferenceKey(userId: Long?) =
        stringPreferencesKey("theme_user_${userId ?: "guest"}")

    fun observeThemePreference(): Flow<AppThemePreference> =
        tokenManager.userId
            .flatMapLatest { uid ->
                val key = preferenceKey(uid)
                context.themePreferencesDataStore.data.map { prefs ->
                    AppThemePreference.fromStorage(prefs[key])
                }
            }
            .distinctUntilChanged()

    suspend fun setThemePreference(mode: AppThemePreference) {
        val uid = tokenManager.getUserId()
        val key = preferenceKey(uid)
        context.themePreferencesDataStore.edit { prefs ->
            if (mode == AppThemePreference.SYSTEM) {
                prefs.remove(key)
            } else {
                prefs[key] = mode.storageValue
            }
        }
    }
}
