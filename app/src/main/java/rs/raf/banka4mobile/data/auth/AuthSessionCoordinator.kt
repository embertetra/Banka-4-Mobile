package rs.raf.banka4mobile.data.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rs.raf.banka4mobile.data.local.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthSessionEvent {
    data object SessionExpired : AuthSessionEvent()
}

@Singleton
class AuthSessionCoordinator @Inject constructor(
    private val sessionManager: SessionManager
) {
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<AuthSessionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun handleUnauthorized() {
        scope.launch {
            mutex.withLock {
                sessionManager.clearSession(keepQuickLoginData = true)
                _events.emit(AuthSessionEvent.SessionExpired)
            }
        }
    }
}


