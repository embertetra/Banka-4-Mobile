package rs.raf.banka4mobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.presentation.home.HomeContract.SideEffect
import rs.raf.banka4mobile.presentation.home.HomeContract.UiEvent
import rs.raf.banka4mobile.presentation.home.HomeContract.UiState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(

) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.ScreenOpened -> fetchData()
            UiEvent.OpenVerificationClicked -> openVerification()
        }
    }

    private fun openVerification() {
        if (state.value.isLoading) return

        viewModelScope.launch {
            _sideEffects.emit(SideEffect.NavigateToVerification)
        }
    }

    private fun fetchData() {
        if (state.value.isLoading) return

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            try {
                // TODO: Replace with real Home data loading (session/user data from repository).
                setState { copy(isLoading = false) }
            } catch (e: Exception) {
                setState { copy(errorMessage = "Greska pri ucitavanju podataka: ${e.message}") }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

}