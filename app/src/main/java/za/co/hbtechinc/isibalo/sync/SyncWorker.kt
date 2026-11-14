package za.co.hbtechinc.isibalo.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import za.co.hbtechinc.isibalo.BuildConfig
import za.co.hbtechinc.isibalo.IsibaloApp
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker that synchronizes local offline Room SQLite
 * transactions with the cPanel MySQL backend API endpoint.
 */
class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as IsibaloApp
            val repo = app.container.repository

            // 1. Gather pending un-synced sales and records from Room DB
            val pendingSales = repo.getPendingSalesForSync()
            if (pendingSales.isEmpty()) {
                return Result.success()
            }

            // 2. Build JSON Payload
            val jsonPayload = buildString {
                append("{\"apiKey\":\"${BuildConfig.SYNC_API_KEY}\",")
                append("\"sales\":[")
                pendingSales.forEachIndexed { index, sale ->
                    if (index > 0) append(",")
                    append("{\"id\":${sale.id},\"totalCents\":${sale.totalCents},\"method\":\"${sale.method}\",\"createdAt\":${sale.createdAt}}")
                }
                append("]}")
            }

            // 3. HTTP POST to cPanel backend API
            val url = URL(BuildConfig.SYNC_URL)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val responseCode = conn.responseCode
            conn.disconnect()

            if (responseCode in 200..299) {
                // Mark records as successfully synced in local Room DB
                repo.markSalesSynced(pendingSales.map { it.id })
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "IsibaloBackgroundSync",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
