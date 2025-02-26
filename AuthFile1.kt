// AuthState.kt
enum class AuthState {
    IDLE,
    FETCHING_SESSION_ID,
    PAIRING_CODE_ENTRY,
    CHECKING_PAIRING_CODE,
    PAIRING_CODE_VALID,
    GOOGLE_LOGIN_REQUIRED,
    FETCHING_TOKEN,
    LOGGED_IN,
    ERROR
}

data class AuthData(
    val sessionId: String = "",
    val user: User? = null,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
)

// Models.kt
data class SessionIdResponse(val sessionId: String)
// Keep other models from previous implementation

// ApiClient.kt (updated with timeout)
object ApiClient {
    private const val BASE_URL = "https://api.example.com/"
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()
    
    // Add session ID endpoint
    suspend fun getSessionId(): Result<SessionIdResponse> = performRequest(
        request = Request.Builder()
            .url("${BASE_URL}generate-session")
            .get()
            .build()
    )

    // Keep other methods from previous implementation
    private suspend fun <T> performRequest(request: Request, responseType: Class<T>): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.Error("Request failed: ${response.code}")
                }
                val body = response.body?.string() ?: return@withContext Result.Error("Empty response")
                val result = Gson().fromJson(body, responseType)
                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}

// AuthViewModel.kt
class AuthViewModel : ViewModel() {
    private var macAddress: String = getMacAddress()
    private val _authState = mutableStateOf(AuthState.IDLE)
    private val _authData = mutableStateOf(AuthData())
    
    val authState: State<AuthState> = _authState
    val authData: State<AuthData> = _authData

    init {
        getSessionId()
    }

    private fun getSessionId() {
        _authState.value = AuthState.FETCHING_SESSION_ID
        viewModelScope.launch {
            when (val result = ApiClient.getSessionId()) {
                is ApiClient.Result.Success -> {
                    _authData.value = _authData.value.copy(sessionId = result.data.sessionId)
                    _authState.value = AuthState.PAIRING_CODE_ENTRY
                }
                is ApiClient.Result.Error -> {
                    _authData.value = _authData.value.copy(errorMessage = result.message)
                    _authState.value = AuthState.ERROR
                }
            }
        }
    }

    fun submitPairingCode() {
        _authState.value = AuthState.CHECKING_PAIRING_CODE
        viewModelScope.launch {
            when (val result = ApiClient.post(
                "validate-pairing",
                PairingCodeRequest(_authData.value.sessionId),
                PairingCodeResponse::class.java
            )) {
                is ApiClient.Result.Success -> handlePairingResponse(result.data)
                is ApiClient.Result.Error -> handleError(result.message)
            }
        }
    }

    private fun handlePairingResponse(response: PairingCodeResponse) {
        _authData.value = _authData.value.copy(isRegistered = response.status == "registered")
        if (response.status == "registered") {
            _authState.value = AuthState.PAIRING_CODE_VALID
            startTokenPolling()
        } else {
            _authState.value = AuthState.GOOGLE_LOGIN_REQUIRED
        }
    }

    fun handleGoogleLogin() {
        viewModelScope.launch {
            when (val result = ApiClient.post(
                "google-login",
                GoogleLoginRequest(macAddress, _authData.value.sessionId),
                Unit::class.java
            )) {
                is ApiClient.Result.Success -> startTokenPolling()
                is ApiClient.Result.Error -> handleError(result.message)
            }
        }
    }

    private fun startTokenPolling() {
        _authState.value = AuthState.FETCHING_TOKEN
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val timeout = 5 * 60 * 1000 // 5 minutes

            while (System.currentTimeMillis() - startTime < timeout) {
                delay(3000) // Poll every 3 seconds
                when (val result = ApiClient.get(
                    "token?session=${_authData.value.sessionId}",
                    TokenPollResponse::class.java
                )) {
                    is ApiClient.Result.Success -> {
                        _authData.value = _authData.value.copy(user = result.data.user)
                        _authState.value = AuthState.LOGGED_IN
                        return@launch
                    }
                    is ApiClient.Result.Error -> {
                        if (result.message.contains("pending")) continue
                        handleError(result.message)
                        return@launch
                    }
                }
            }
            handleError("Token polling timed out after 5 minutes")
        }
    }

    private fun handleError(message: String) {
        _authData.value = _authData.value.copy(errorMessage = message)
        _authState.value = AuthState.ERROR
    }

    private fun getMacAddress(): String {
        // Implementation to get actual MAC address
        return "02:00:00:00:00:00"
    }
}

// AuthScreen.kt (updated)
@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {
    when (viewModel.authState.value) {
        AuthState.FETCHING_SESSION_ID -> LoadingScreen("Generating session...")
        AuthState.PAIRING_CODE_ENTRY -> PairingCodeScreen(viewModel)
        AuthState.CHECKING_PAIRING_CODE -> LoadingScreen("Verifying code...")
        AuthState.PAIRING_CODE_VALID -> LoadingScreen("Authenticating...")
        AuthState.GOOGLE_LOGIN_REQUIRED -> GoogleLoginScreen(viewModel)
        AuthState.FETCHING_TOKEN -> LoadingScreen("Finalizing login...")
        AuthState.LOGGED_IN -> ProfileScreen(viewModel.authData.value.user!!)
        AuthState.ERROR -> ErrorScreen(viewModel.authData.value.errorMessage ?: "Unknown error")
        AuthState.IDLE -> Unit // Initial state
    }
}

@Composable
fun PairingCodeScreen(viewModel: AuthViewModel) {
    val sessionId = viewModel.authData.value.sessionId
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pairing Code: ${sessionId.chunked(3).joinToString(" ")}", 
            style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(32.dp))
        Button(onClick = { viewModel.submitPairingCode() }) {
            Text("Submit Pairing Code")
        }
    }
}

// Keep other composables from previous implementation with similar state checks
