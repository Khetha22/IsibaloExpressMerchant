package za.co.hbtechinc.isibalo.sync

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import za.co.hbtechinc.isibalo.data.SaleWithItems
import za.co.hbtechinc.isibalo.data.StockItem

interface RemoteApi {
    suspend fun push(sales: List<SaleWithItems>, stock: List<StockItem>): Boolean
}

object SyncScheduler {
    fun schedule(context: Context) {
        SyncWorker.schedule(context)
    }

    fun syncNow(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
