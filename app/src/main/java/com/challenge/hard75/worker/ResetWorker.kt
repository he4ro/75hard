package com.challenge.hard75.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.challenge.hard75.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ResetWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).challengeDao()
        val state = dao.getChallengeStateOnce() ?: return Result.success()
        val rules = dao.getAllRules().first()
        if (rules.isEmpty()) return Result.success()

        // Check yesterday's logical date
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        val logs = dao.getLogsForDateOnce(yesterday)
        val completedIds = logs.filter { it.isCompleted }.map { it.ruleId }.toSet()
        val allCompleted = rules.all { it.id in completedIds }

        if (!allCompleted) {
            // PENALTY: reset to Day 1
            dao.setChallengeState(state.copy(startDateMillis = System.currentTimeMillis()))
        }

        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            // Schedule daily check at the end-of-day hour
            val work = PeriodicWorkRequestBuilder<ResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
        }

        private fun calculateInitialDelay(): Long {
            val now = Calendar.getInstance()
            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 2)
                set(Calendar.MINUTE, 5)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            return next.timeInMillis - now.timeInMillis
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ResetWorker.schedule(context)
        }
    }
}
