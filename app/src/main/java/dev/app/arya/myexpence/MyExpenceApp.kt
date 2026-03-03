package dev.app.arya.myexpence

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import dev.app.arya.myexpence.data.AppContainer
import dev.app.arya.myexpence.data.AppContainerImpl
import dev.app.arya.myexpence.data.work.BudgetAlertWorker
import java.util.concurrent.TimeUnit

class MyExpenceApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)

        val request = PeriodicWorkRequestBuilder<BudgetAlertWorker>(1, TimeUnit.DAYS).build()
        container.workManager.enqueueUniquePeriodicWork(
            BudgetAlertWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}

