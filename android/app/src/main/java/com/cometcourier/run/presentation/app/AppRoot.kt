package com.cometcourier.run.presentation.app

import android.app.Activity
import android.animation.ValueAnimator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import com.cometcourier.run.core.di.ServiceLocator
import com.cometcourier.run.core.navigation.*
import com.cometcourier.run.core.theme.LocalMotionEnabled
import com.cometcourier.run.presentation.splash.*
import com.cometcourier.run.presentation.menu.*
import com.cometcourier.run.presentation.game.*
import com.cometcourier.run.presentation.gameover.*
import com.cometcourier.run.presentation.hullshop.*
import com.cometcourier.run.presentation.missions.*
import com.cometcourier.run.presentation.settings.*

@Composable
fun AppRoot() {
    val factory = remember { ServiceLocator.factory() }
    val app: AppViewModel = viewModel(factory = factory)
    val splash: SplashViewModel = viewModel(factory = factory)
    val menu: MenuViewModel = viewModel(factory = factory)
    val game: GameViewModel = viewModel(factory = factory)
    val summary: GameOverViewModel = viewModel(factory = factory)
    val shop: HullShopViewModel = viewModel(factory = factory)
    val missions: MissionsViewModel = viewModel(factory = factory)
    val settings: SettingsViewModel = viewModel(factory = factory)
    val screen by app.screen.collectAsStateWithLifecycle()
    val splashState by splash.state.collectAsStateWithLifecycle()
    val menuState by menu.state.collectAsStateWithLifecycle()
    val gameState by game.state.collectAsStateWithLifecycle()
    val summaryState by summary.state.collectAsStateWithLifecycle()
    val shopState by shop.state.collectAsStateWithLifecycle()
    val missionState by missions.state.collectAsStateWithLifecycle()
    val settingsState by settings.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    val haptics = LocalHapticFeedback.current
    val motionEnabled = !settingsState.settings.reducedMotion && ValueAnimator.areAnimatorsEnabled()
    val lightBars = screen == Screen.Menu || screen == Screen.Missions || screen == Screen.Settings || screen is Screen.GameOver
    SideEffect {
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = lightBars
            controller.isAppearanceLightNavigationBars = lightBars
        }
    }

    fun dispatch(event: UiEvent) {
        when (event) {
            is UiEvent.Navigate -> {
                (event.screen as? Screen.GameOver)?.let { summary.show(it.resultId) }
                app.navigate(event.screen)
            }
            is UiEvent.Start -> { game.start(event.runId); app.start(event.runId) }
            UiEvent.Menu -> app.menu()
            UiEvent.Back -> app.back()
            UiEvent.Exit -> activity?.finish()
            UiEvent.Haptic -> haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    val dispatchLatest by rememberUpdatedState<(UiEvent) -> Unit>(::dispatch)
    LaunchedEffect(Unit) { app.events.collect { dispatchLatest(it) } }
    LaunchedEffect(Unit) { menu.events.collect { dispatchLatest(it) } }
    LaunchedEffect(Unit) { game.events.collect { dispatchLatest(it) } }
    LaunchedEffect(Unit) { summary.events.collect { dispatchLatest(it) } }
    BackHandler {
        if (screen is Screen.Game) {
            if (gameState.pauseDialog) game.dismissPause() else game.leave(false)
        } else app.back()
    }
    CompositionLocalProvider(LocalMotionEnabled provides motionEnabled) {
        AnimatedContent(targetState = screen, transitionSpec = {
            val duration = if (!motionEnabled) 120 else if (initialState == Screen.Splash) 250 else 200
            fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
        }, label = "route") { destination ->
            when (destination) {
                Screen.Splash -> SplashScreen(splashState, splash.finished, app::finishSplash)
                Screen.Menu -> {
                    DisposableEffect(Unit) { menu.visible(); onDispose { } }
                    MenuScreen(menuState, menu::play, menu::open, menu::cycle, menu::retry)
                }
                is Screen.Game -> GameRoute(game, gameState)
                is Screen.GameOver -> {
                    DisposableEffect(destination.resultId) { summary.show(destination.resultId); onDispose { } }
                    GameOverScreen(summaryState, summary::again, summary::menu, { summary.open(Screen.Missions) }, summary::retry)
                }
                Screen.HullShop -> HullShopScreen(shopState, shop::choose, app::menu, shop::retry)
                Screen.Missions -> {
                    val owner = LocalLifecycleOwner.current
                    DisposableEffect(owner) {
                        missions.visible()
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_START) missions.visible()
                            if (event == Lifecycle.Event.ON_STOP) missions.hidden()
                        }
                        owner.lifecycle.addObserver(observer)
                        onDispose { owner.lifecycle.removeObserver(observer); missions.hidden() }
                    }
                    MissionsScreen(missionState, missions::claim, app::menu, missions::retry)
                }
                Screen.Settings -> SettingsScreen(settingsState, settings::change, settings::rules, app::menu, settings::retry)
            }
        }
    }
}

@Composable
private fun GameRoute(viewModel: GameViewModel, state: GameUiState) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) viewModel.pause() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); viewModel.pause() }
    }
    GameScreen(state, viewModel::move, { viewModel.pause(true) }, viewModel::resume, { viewModel.finish() }, { viewModel.leave(true) }, viewModel::dismissPause, viewModel::summary, viewModel::retry)
}
