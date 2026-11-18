package com.app.toonplay.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.toonplay.data.model.LayoutOverride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.toonPlayDataStore by preferencesDataStore(name = "toonplay_prefs")

class UserPreferencesRepository(private val context: Context) {

  private object Keys {
    val SAVED_IDS = stringSetPreferencesKey("saved_anime_ids")
    val LAYOUT_OVERRIDE = stringPreferencesKey("layout_override")
  }

  val savedIds: Flow<Set<String>> =
    context.toonPlayDataStore.data.map { prefs -> prefs[Keys.SAVED_IDS] ?: emptySet() }

  val layoutOverride: Flow<LayoutOverride> =
    context.toonPlayDataStore.data.map { prefs ->
      val raw = prefs[Keys.LAYOUT_OVERRIDE]
      LayoutOverride.entries.firstOrNull { it.name == raw } ?: LayoutOverride.AUTO
    }

  suspend fun toggleSaved(id: String) {
    context.toonPlayDataStore.edit { prefs ->
      val current = prefs[Keys.SAVED_IDS]?.toMutableSet() ?: mutableSetOf()
      if (!current.add(id)) {
        current.remove(id)
      }
      prefs[Keys.SAVED_IDS] = current
    }
  }

  suspend fun setLayoutOverride(override: LayoutOverride) {
    context.toonPlayDataStore.edit { prefs -> prefs[Keys.LAYOUT_OVERRIDE] = override.name }
  }
}
