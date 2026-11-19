package za.co.hbtechinc.isibalo.platform

import android.content.Context
import za.co.hbtechinc.isibalo.sync.SyncScheduler

/**
 * Multiplatform abstraction for background data synchronization across platforms.
 */
interface PlatformSyncEngine {
    fun schedulePeriodicSync()
    fun triggerImmediateSync()
}

class AndroidSyncEngine(private val context: Context) : PlatformSyncEngine {
    override fun schedulePeriodicSync() {
        SyncScheduler.schedule(context)
    }

    override fun triggerImmediateSync() {
        SyncScheduler.syncNow(context)
    }
}
