package com.cometcourier.run.presentation.hullshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometcourier.run.domain.model.CourierHull
import com.cometcourier.run.domain.repository.ProgressRepository
import com.cometcourier.run.domain.usecase.UnlockHullUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HullShopViewModel(private val progress: ProgressRepository, private val unlock: UnlockHullUseCase, catalog: List<CourierHull>) : ViewModel() {
    private val mutable = MutableStateFlow(HullShopUiState(catalog = catalog))
    val state = mutable.asStateFlow()
    init { viewModelScope.launch { progress.state.collect { p -> mutable.update { it.copy(profile = p.profile, loading = p.loading, readError = p.readError, saveError = p.saveError) } } } }
    fun choose(id: String) {
        if (mutable.value.pendingHullId != null || mutable.value.saveError) return
        val hull = mutable.value.catalog.find { it.id == id } ?: return
        mutable.update { it.copy(pendingHullId = id) }
        viewModelScope.launch {
            if (id in (mutable.value.profile?.owned ?: emptySet())) progress.select(id) else unlock(hull)
            mutable.update { it.copy(pendingHullId = null) }
        }
    }
    fun retry() { viewModelScope.launch { progress.retry() } }
}
