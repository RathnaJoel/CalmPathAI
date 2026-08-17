package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favoriteEntities: List<FavoritePlaceEntity> = emptyList(),
    val favoritePlaces: List<Place> = emptyList(),
    val isLoading: Boolean = true
)

class FavoritesViewModel(
    private val repository: CalmPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.favoritesFlow.collect { entities ->
                val places = entities.map { it.toPlace() }
                _uiState.value = _uiState.value.copy(
                    favoriteEntities = entities,
                    favoritePlaces = places,
                    isLoading = false
                )
            }
        }
    }

    fun removeFavorite(place: Place) {
        viewModelScope.launch {
            repository.removeFavoriteById(place.id)
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
        }
    }
}

class FavoritesViewModelFactory(
    private val repository: CalmPathRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoritesViewModel(repository) as T
    }
}
