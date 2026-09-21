package com.minnolter.habitrack

import android.app.Application
import com.minnolter.habitrack.data.local.DatabaseBackupManager
import com.minnolter.habitrack.data.local.HabitractDatabase
import com.minnolter.habitrack.data.local.datastore.SettingsDataStore
import com.minnolter.habitrack.data.repository.HabitractRepositoryImpl
import com.minnolter.habitrack.domain.repository.HabitractRepository

/**
 * Application-scoped composition root. No DI framework has been introduced
 * yet (a deliberate Phase 1 decision — see the data-architecture notes), so
 * every app-wide singleton is built here, lazily, and handed down through
 * [com.habitract.app.ui.navigation.HabitractNavHost] to whichever
 * `ViewModelProvider.Factory` needs it.
 *
 * [settingsDataStore] and [backupManager] are new in Phase 6, alongside the
 * Phase 1 [database]/[repository] pair; both are plain `by lazy` singletons
 * for the same reason the database is — a single [SettingsDataStore] means a
 * single underlying DataStore file, and a single [DatabaseBackupManager]
 * means export/import always target the one live [database] instance.
 *
 * Requires `android:name=".HabitractApplication"` on the `<application>` tag
 * in `AndroidManifest.xml`.
 */
class HabitractApplication : Application() {

    private val database: HabitractDatabase by lazy {
        HabitractDatabase.getInstance(this)
    }

    val repository: HabitractRepository by lazy {
        HabitractRepositoryImpl(
            habitDao = database.habitDao(),
            practiceSessionDao = database.practiceSessionDao()
        )
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(this)
    }

    val backupManager: DatabaseBackupManager by lazy {
        DatabaseBackupManager(this)
    }
}
