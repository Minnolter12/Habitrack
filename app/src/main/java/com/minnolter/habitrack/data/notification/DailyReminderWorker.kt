package com.minnolter.habitrack.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.minnolter.habitrack.HabitractApplication
import com.minnolter.habitrack.domain.model.TimeRange
import kotlinx.coroutines.flow.firstOrNull

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        val app = applicationContext as? HabitractApplication ?: return androidx.work.ListenableWorker.Result.success()
        val settings = app.settingsDataStore.settings.firstOrNull() ?: return androidx.work.ListenableWorker.Result.success()

        if (!settings.dailyRemindersEnabled) return androidx.work.ListenableWorker.Result.success()

        val todayMinutes = app.repository.observeTotalMinutes(TimeRange.TODAY).firstOrNull() ?: 0
        if (todayMinutes == 0) {
            HabitNotificationManager(applicationContext).sendDuolingoPrompt()
        }

        return androidx.work.ListenableWorker.Result.success()
    }
}
