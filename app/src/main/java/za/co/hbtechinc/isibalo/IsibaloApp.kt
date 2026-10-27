package za.co.hbtechinc.isibalo

import android.app.Application
import android.content.Context
import androidx.room.Room
import za.co.hbtechinc.isibalo.ai.LocalAi
import za.co.hbtechinc.isibalo.data.AppDatabase
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.sync.HttpRemoteApi
import za.co.hbtechinc.isibalo.sync.RemoteApi
import za.co.hbtechinc.isibalo.sync.SyncScheduler
import za.co.hbtechinc.isibalo.ui.theme.ThemeStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(app: Context) {
    private val db = Room.databaseBuilder(app, AppDatabase::class.java, "isibalo.db")
        .fallbackToDestructiveMigration()
        .build()
    val repository = Repository(db)
    val themeStore = ThemeStore(app)
    val ai = LocalAi(app)
    val remote: RemoteApi = HttpRemoteApi(app, BuildConfig.SYNC_URL, BuildConfig.SYNC_API_KEY)
}

class IsibaloApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CoroutineScope(Dispatchers.IO).launch { container.repository.seedIfEmpty() }
        SyncScheduler.schedule(this)
    }
}
