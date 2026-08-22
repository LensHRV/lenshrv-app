package com.lenshrv.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences_repository")

@Singleton
class AppPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,

    ) {
    companion object {
        private val TERMS_ACCEPTED_KEY = booleanPreferencesKey("terms_accepted")
        private val TELEMETRY_ENABLED_KEY = booleanPreferencesKey("telemetry_enabled")
        private val ANONYMOUS_USER_ID = stringPreferencesKey("anonymous_user_id")
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

    suspend fun hasDecidedTelemetry(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences.contains(TELEMETRY_ENABLED_KEY)
        }.first()
    }
    suspend fun saveTelemetryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TELEMETRY_ENABLED_KEY] = enabled
        }
    }

    suspend fun isTelemetryEnabled(): Boolean {
        val flow = context.dataStore.data.map { preferences ->
            preferences[TELEMETRY_ENABLED_KEY] ?: false
        }
        return flow.first()
    }

    suspend fun ensureAnonymousIdCreated() {
        context.dataStore.edit { preferences ->
            if (preferences[ANONYMOUS_USER_ID] == null) {
                val newUuid = UUID.randomUUID().toString()
                preferences[ANONYMOUS_USER_ID] = "user_anon_$newUuid"
            }
        }
    }

    suspend fun getAnonymousId(): String {
        return context.dataStore.data.map { it[ANONYMOUS_USER_ID] ?: "" }.first()
    }


    val telemetryEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val enabled = preferences[TELEMETRY_ENABLED_KEY] ?: false
        enabled
    }

    val measurementPrepEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[PREP_ENABLED_KEY] ?: true
    }

    suspend fun saveMeasurementPrepEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PREP_ENABLED_KEY] = enabled }
    }

}
