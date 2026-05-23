package rs.raf.banka4mobile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.raf.banka4mobile.data.local.settings.AppThemeOption
import rs.raf.banka4mobile.data.local.settings.ThemePreferenceManager
import rs.raf.banka4mobile.domain.model.home.BankAccountSummary
import rs.raf.banka4mobile.domain.model.home.BankPayment
import rs.raf.banka4mobile.domain.repository.AuthRepository
import rs.raf.banka4mobile.domain.repository.HomeRepository
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val homeRepository: HomeRepository,
    private val themePreferenceManager: ThemePreferenceManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.UiState())
    val state = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<ProfileContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        observeTheme()
    }

    fun onEvent(event: ProfileContract.UiEvent) {
        when (event) {
            ProfileContract.UiEvent.ScreenOpened -> loadProfile()
            ProfileContract.UiEvent.LogoutClicked -> logout()
            is ProfileContract.UiEvent.ThemeChanged -> changeTheme(event.theme)
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferenceManager.selectedTheme.collect { theme ->
                _state.update {
                    it.copy(selectedTheme = theme)
                }
            }
        }
    }

    private fun changeTheme(theme: AppThemeOption) {
        viewModelScope.launch {
            themePreferenceManager.setTheme(theme)
        }
    }

    private fun loadProfile() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val session = authRepository.getSession()

                if (session == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Nema aktivne sesije."
                        )
                    }
                    return@launch
                }

                val user = session.user
                val accounts = homeRepository.getAccounts().getOrDefault(emptyList())
                val payments = loadAllPayments(accounts)

                _state.update {
                    it.copy(
                        isLoading = false,
                        profile = ProfileUiModel(
                            fullName = "${user.firstName} ${user.lastName}",
                            email = user.email,
                            username = user.username,
                            identityType = user.identityType
                        ),
                        monthlyBalance = buildMonthlyBalance(accounts, payments),
                        dailySpending = buildCurrentMonthDailySpending(accounts, payments)
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Greška pri učitavanju profila."
                    )
                }
            }
        }
    }

    private suspend fun loadAllPayments(
        accounts: List<BankAccountSummary>
    ): List<BankPayment> {
        return accounts
            .flatMap { account ->
                homeRepository.getPayments(account.accountNumber).getOrDefault(emptyList())
            }
            .distinctBy { it.id }
    }

    private fun buildMonthlyBalance(
        accounts: List<BankAccountSummary>,
        payments: List<BankPayment>
    ): List<MonthlyBalanceItem> {
        val accountNumbers = accounts.map { it.accountNumber }.toSet()

        val lastSixMonths = (5 downTo 0).map { monthsAgo ->
            YearMonth.now().minusMonths(monthsAgo.toLong())
        }

        return lastSixMonths.map { month ->
            val paymentsInMonth = payments.filter { payment ->
                parsePaymentMonth(payment.createdAt) == month
            }

            val income = paymentsInMonth
                .filter { payment -> payment.recipientAccount in accountNumbers }
                .sumOf { payment -> payment.amount }

            val outcome = paymentsInMonth
                .filter { payment -> payment.payerAccount in accountNumbers }
                .sumOf { payment -> payment.amount }

            MonthlyBalanceItem(
                monthLabel = month.format(DateTimeFormatter.ofPattern("MMM", Locale("sr"))),
                income = income,
                outcome = outcome
            )
        }
    }

    private fun buildCurrentMonthDailySpending(
        accounts: List<BankAccountSummary>,
        payments: List<BankPayment>
    ): List<DailySpendingItem> {
        val accountNumbers = accounts.map { it.accountNumber }.toSet()
        val currentMonth = YearMonth.now()
        val daysInMonth = currentMonth.lengthOfMonth()

        return (1..daysInMonth).map { day ->
            val amountForDay = payments
                .filter { payment ->
                    payment.payerAccount in accountNumbers &&
                            parsePaymentMonth(payment.createdAt) == currentMonth &&
                            parsePaymentDay(payment.createdAt) == day
                }
                .sumOf { payment -> payment.amount }

            DailySpendingItem(
                day = day,
                amount = amountForDay
            )
        }
    }

    private fun parsePaymentMonth(createdAt: String): YearMonth? {
        return runCatching {
            YearMonth.from(OffsetDateTime.parse(createdAt))
        }.getOrNull()
    }

    private fun parsePaymentDay(createdAt: String): Int? {
        return runCatching {
            OffsetDateTime.parse(createdAt).dayOfMonth
        }.getOrNull()
    }

    private fun logout() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                authRepository.logout()
                _state.update { it.copy(isLoading = false) }
                _sideEffects.emit(ProfileContract.SideEffect.NavigateToLogin)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Greška pri odjavi."
                    )
                }
            }
        }
    }
}