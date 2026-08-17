package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.auth.AuthState
import com.calmpath.ai.data.model.UserProfile
import com.calmpath.ai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthTab {
    LOGIN,
    REGISTER
}

data class AuthUiState(
    val selectedTab: AuthTab = AuthTab.LOGIN,
    val nameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: UserProfile? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is AuthState.Authenticated -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            currentUser = state.user
                        )
                    }
                    is AuthState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = state.message
                        )
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentUser = null
                        )
                    }
                    AuthState.Initial -> {}
                }
            }
        }
    }

    fun onTabChanged(tab: AuthTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            errorMessage = null
        )
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(nameInput = name, errorMessage = null)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(emailInput = email, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(passwordInput = password, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
        val email = state.emailInput.trim()
        val password = state.passwordInput.trim()

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid email address.")
            return
        }

        if (password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            if (state.selectedTab == AuthTab.LOGIN) {
                val result = authRepository.signIn(email, password)
                if (result.isSuccess) {
                    onSuccess()
                }
            } else {
                val name = state.nameInput.trim()
                if (name.isBlank()) {
                    _uiState.value = state.copy(errorMessage = "Please enter your name.")
                    return@launch
                }
                val result = authRepository.signUp(name, email, password)
                if (result.isSuccess) {
                    onSuccess()
                }
            }
        }
    }

    fun signInAsDemo(onSuccess: () -> Unit) {
        authRepository.signInAsGuest("Joel Wellness", "joel@calmpath.ai")
        onSuccess()
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository) as T
    }
}
