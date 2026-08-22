package com.lenshrv.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences_repository")

@Singleton
class AppPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,

    ) {
    companion object {
        private val TERMS_ACCEPTED_KEY = booleanPreferencesKey("terms_accepted")
        private val PREP_ENABLED_KEY = booleanPreferencesKey("measurement_prep_enabled")
    }

    suspend fun hasAcceptedTerms(): Boolean {
        val flow = context.dataStore.data.map { preferences ->
            preferences[TERMS_ACCEPTED_KEY] ?: false
        }
        return flow.first()
    }

    suspend fun saveTermsAcceptance(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TERMS_ACCEPTED_KEY] = accepted
        }
    }


    val measurementPrepEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[PREP_ENABLED_KEY] ?: true
    }

    suspend fun saveMeasurementPrepEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PREP_ENABLED_KEY] = enabled }
    }

}
