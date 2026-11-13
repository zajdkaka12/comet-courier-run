package com.cometcourier.run.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometcourier.run.data.feedback.LocalFeedbackPlayer
import com.cometcourier.run.data.sample.SampleData
import com.cometcourier.run.domain.repository.ProgressRepository
import com.cometcourier.run.domain.usecase.*
import com.cometcourier.run.presentation.app.AppViewModel
import com.cometcourier.run.presentation.splash.SplashViewModel
import com.cometcourier.run.presentation.menu.MenuViewModel
import com.cometcourier.run.presentation.game.GameViewModel
import com.cometcourier.run.presentation.gameover.GameOverViewModel
import com.cometcourier.run.presentation.hullshop.HullShopViewModel
import com.cometcourier.run.presentation.missions.MissionsViewModel
import com.cometcourier.run.presentation.settings.SettingsViewModel

class AppViewModelFactory(private val progress: ProgressRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AppViewModel::class.java -> AppViewModel(progress)
        SplashViewModel::class.java -> SplashViewModel(progress)
        MenuViewModel::class.java -> MenuViewModel(progress, SampleData.missions)
        GameViewModel::class.java -> GameViewModel(progress, RecordDeliveryUseCase(progress), FinalizeRunUseCase(progress), LocalFeedbackPlayer())
        GameOverViewModel::class.java -> GameOverViewModel(progress)
        HullShopViewModel::class.java -> HullShopViewModel(progress, UnlockHullUseCase(progress), SampleData.hulls)
        MissionsViewModel::class.java -> MissionsViewModel(progress, ClaimMissionUseCase(progress), SampleData.missions)
        SettingsViewModel::class.java -> SettingsViewModel(progress)
        else -> throw IllegalArgumentException(modelClass.name)
    } as T
}
