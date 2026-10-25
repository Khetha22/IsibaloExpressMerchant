package za.co.hbtechinc.isibalo.sync

import android.content.Context
import androidx.work.*
import za.co.hbtechinc.isibalo.IsibaloApp
import za.co.hbtechinc.isibalo.data.SaleWithItems
import za.co.hbtechinc.isibalo.data.StockItem
import java.util.concurrent.TimeUnit

/** Swap the implementation for Ktor / Retrofit calling your PHP or Ktor + MySQL endpoint. */
interface RemoteApi {
    suspend fun push(sales: List<SaleWithItems>, stock: List<StockItem>): Boolean
}

/** DEMO ONLY: pretends the server accepted everything. Replace before going live. */
object NoopRemoteApi : RemoteApi {
    override suspend fun push(sales: List<SaleWithItems>, stock: List<StockItem>) = true
}

class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val c = (applicationContext as IsibaloApp).container
        return try {
            val sales = c.repository.unsyncedSales()
            val stock = c.repository.unsyncedStock()
            if (sales.isEmpty() && stock.isEmpty()) return Result.success()
            if (c.remote.push(sales, stock)) {
                c.repository.markSynced(sales.map { it.sale.id }, stock.map { it.id })
                Result.success()
            } else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

object SyncScheduler {
    private val online = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun schedule(ctx: Context) {
        val req = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).setConstraints(online).build()
        WorkManager.getInstance(ctx)
            .enqueueUniquePeriodicWork("sync-periodic", ExistingPeriodicWorkPolicy.KEEP, req)
    }

    fun syncNow(ctx: Context) {
        val req = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(online).build()
        WorkManager.getInstance(ctx).enqueueUniqueWork("sync-now", ExistingWorkPolicy.REPLACE, req)
    }
}
