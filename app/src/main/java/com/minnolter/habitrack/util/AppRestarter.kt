package com.minnolter.habitrack.util

import android.content.Context
import android.content.Intent

/**
 * Restarts the whole app process. Needed after a database restore
 * (`DatabaseBackupManager.importDatabase`): `HabitractApplication` builds its
 * `HabitractDatabase`/`HabitractRepository` as lazy singletons, so once
 * they've been created there's no in-process way to make them forget the
 * closed connection and pick up the freshly-restored file — a clean process
 * restart is the simplest correct fix, using the standard
 * `Intent.makeRestartActivityTask` pattern rather than any ad-hoc
 * kill-and-hope approach.
 */
fun restartApp(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?: error("No launch intent found for ${context.packageName}")
    val restartIntent = Intent.makeRestartActivityTask(launchIntent.component)
    context.startActivity(restartIntent)
    Runtime.getRuntime().exit(0)
}
