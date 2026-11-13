package com.cometcourier.run.core.di

import android.content.Context
import com.cometcourier.run.data.local.*
import com.cometcourier.run.data.repository.ProgressRepositoryImpl
import com.cometcourier.run.domain.repository.ProgressRepository

object ServiceLocator {
    private var progress: ProgressRepository? = null
    fun initialize(context: Context) {
        if (progress == null) progress = ProgressRepositoryImpl(SharedPreferencesStore(context.applicationContext, ProfileJsonCodec()))
    }
    fun factory(): AppViewModelFactory = AppViewModelFactory(requireNotNull(progress))
}
