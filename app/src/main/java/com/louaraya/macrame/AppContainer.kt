package com.louaraya.macrame

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.room.Room
import com.louaraya.macrame.data.AppDatabase
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Contenedor  */
class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: AppDatabase = Room
        .databaseBuilder(context.applicationContext, AppDatabase::class.java, "macrame.db")
        .fallbackToDestructiveMigration()
        .build()

    val repository = MacrameRepository(database)
    val session = SessionManager(context.applicationContext, repository, appScope)

    init {
        appScope.launch { repository.seedIfNeeded() }
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no disponible: falta proveerlo en MainActivity")
}
