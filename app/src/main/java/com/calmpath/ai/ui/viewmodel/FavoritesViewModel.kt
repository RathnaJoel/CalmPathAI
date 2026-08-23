package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.local.entities.FavoriteWithPlace
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favoriteItems: List<FavoriteWithPlace> = emptyList(),
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
            repository.favoritesWithPlacesFlow.collect { favoritesList ->
                val places = favoritesList.map { it.place.toDomainModel() }
                _uiState.value = _uiState.value.copy(
                    favoriteItems = favoritesList,
                    favoritePlaces = places,
                    isLoading = false
                )
            }
        }
    }

    fun removeFavorite(place: Place) {
        viewModelScope.launch {
            repository.removeFavorite(place.id)
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
