package com.minnolter.habitrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.minnolter.habitrack.data.local.converter.DateTimeConverters
import com.minnolter.habitrack.data.local.dao.HabitDao
import com.minnolter.habitrack.data.local.dao.PracticeSessionDao
import com.minnolter.habitrack.data.local.entity.HabitEntity
import com.minnolter.habitrack.data.local.entity.PracticeSessionEntity

@Database(
    entities = [
        HabitEntity::class,
        PracticeSessionEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class HabitractDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun practiceSessionDao(): PracticeSessionDao

    companion object {
        /** Public in Phase 6 so `DatabaseBackupManager` can locate the file to back up/restore. */
        const val DATABASE_FILE_NAME = "habitract.db"

        /**
         * Phase 5 added [HabitEntity.description] and
         * [PracticeSessionEntity.note] to back the Habit Detail screen's
         * Edit Habit and manual session dialogs — neither existed in the
         * original Phase 1 schema. Both are nullable `TEXT` columns, so the
         * migration is a pair of additive `ALTER TABLE` statements with no
         * data loss (Section 42: "Use Room migrations appropriately").
         */
        val MIGRATION_1_2 = object : Migration(startVersion = 1, endVersion = 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN description TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE practice_sessions ADD COLUMN note TEXT DEFAULT NULL")
            }
        }

        @Volatile
        private var instance: HabitractDatabase? = null

        fun getInstance(context: Context): HabitractDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        /**
         * Closes and forgets the current singleton. Added in Phase 6 for
         * `DatabaseBackupManager.importDatabase`: the on-disk file is about
         * to be replaced wholesale, so the existing connection (and any WAL
         * state it holds) must be torn down first. The very next
         * [getInstance] call after this rebuilds a fresh connection — but in
         * practice the app is fully restarted after an import anyway (see
         * `util/AppRestarter.kt`), so no other code needs to know this
         * happened.
         */
        fun closeInstance() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }

        private fun buildDatabase(context: Context): HabitractDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                HabitractDatabase::class.java,
                DATABASE_FILE_NAME
            )
                .addMigrations(MIGRATION_1_2)
                // No destructive fallback: every schema change ships an
                // explicit Migration per Section 42, never a data wipe.
                .build()
        }
    }
}
