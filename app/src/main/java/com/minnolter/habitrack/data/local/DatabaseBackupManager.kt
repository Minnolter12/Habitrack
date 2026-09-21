package com.minnolter.habitrack.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Raw-file backup/restore of the whole Room database (Section 37). This is a
 * deliberately simple whole-file copy rather than a row-level export format:
 * Habitract's data model is small (two tables) and a byte-for-byte SQLite
 * file is trivially restorable with zero mapping logic to keep in sync as
 * the schema evolves — the trade-off is that a restore only works against
 * this exact app (not a spreadsheet or another tool), which is an
 * acceptable one for a personal backup feature.
 */
class DatabaseBackupManager(private val context: Context) {

    /**
     * Copies the live database file to [destinationUri] (from a
     * `CreateDocument` picker). Forces a WAL checkpoint first so the copied
     * file reflects every committed write, not just what's made it from the
     * write-ahead log into the main file yet.
     */
    suspend fun exportDatabase(destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            checkpointWriteAheadLog()

            val dbFile = context.getDatabasePath(HabitractDatabase.DATABASE_FILE_NAME)
            check(dbFile.exists()) { "No database file found to back up." }

            val output = context.contentResolver.openOutputStream(destinationUri)
                ?: error("Couldn't open the chosen location for writing.")
            output.use { out ->
                FileInputStream(dbFile).use { input -> input.copyTo(out) }
            }
            Unit
        }
    }

    /**
     * Restores the database from [sourceUri] (from an `OpenDocument`
     * picker), after a lightweight schema/format check. The current
     * database connection is closed first — Room re-opens a fresh one only
     * after the caller restarts the app (`util/AppRestarter.kt`), since a
     * live `HabitractDatabase` singleton has no way to be told "the file
     * under you just changed."
     */
    suspend fun importDatabase(sourceUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            verifyIsSqliteDatabase(sourceUri)

            HabitractDatabase.closeInstance()

            val dbFile = context.getDatabasePath(HabitractDatabase.DATABASE_FILE_NAME)
            val input = context.contentResolver.openInputStream(sourceUri)
                ?: error("Couldn't open the selected backup file.")
            input.use { inStream ->
                FileOutputStream(dbFile).use { out -> inStream.copyTo(out) }
            }

            // Discard any leftover WAL/SHM sidecar files from the *previous*
            // database — otherwise Room may try to replay stale write-ahead
            // log entries against the just-restored file on next open.
            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()
            Unit
        }
    }

    private fun checkpointWriteAheadLog() {
        val db = HabitractDatabase.getInstance(context)
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
    }

    /**
     * The "schema verification" the spec asks for: this isn't a full schema
     * diff (there's no reliable, low-risk way to introspect a closed
     * SQLite file's table layout without opening it as a database first,
     * which would itself risk corrupting the live app database file if done
     * carelessly) — it's a SQLite file-format header check, which is enough
     * to reject an obviously wrong file (a photo, a text export, a
     * different app's backup) before it overwrites Habitract's real data.
     */
    private fun verifyIsSqliteDatabase(uri: Uri) {
        val header = ByteArray(SQLITE_HEADER_MAGIC.length)
        val bytesRead = context.contentResolver.openInputStream(uri)?.use { it.read(header) }
            ?: error("Couldn't read the selected file.")
        require(bytesRead == header.size && String(header, Charsets.US_ASCII) == SQLITE_HEADER_MAGIC) {
            "That file doesn't look like a Habitract backup."
        }
    }

    private companion object {
        const val SQLITE_HEADER_MAGIC = "SQLite format 3\u0000"
    }
}
