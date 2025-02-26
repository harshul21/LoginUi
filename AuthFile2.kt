// AuthState.kt
sealed class AuthState {
    object PairingCodeEntry : AuthState()
    object CheckingPairingCode : AuthState()
    data class PairingCodeValidated(val isRegistered: Boolean) : AuthState()
    object GoogleLoginRequired : AuthState()
    object FetchingToken : AuthState()
    data class LoggedIn(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Models.kt
data class PairingCodeRequest(val code: String)
data class PairingCodeResponse(
    val status: String,
    val message: String? = null
)

data class GoogleLoginRequest(
    val macAddress: String,
    val sessionId: String
)

data class TokenPollResponse(
    val token: String,
    val user: User
)

data class User(
    val name: String,
    val profilePictureUrl: String
)

// ApiClient.kt
object ApiClient {
    private const val BASE_URL = "https://api.example.com/"
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun <T> post(
        path: String,
        requestBody: Any,
        responseType: Class<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(requestBody)
            val body = json.toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(BASE_URL + path)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.Error("Request failed: ${response.code}")
            }

            val responseBody = response.body?.string()
            responseBody?.let {
                val result = gson.fromJson(it, responseType)
                return@withContext Result.Success(result)
            } ?: Result.Error("Empty response body")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun <T> get(
        path: String,
        responseType: Class<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(BASE_URL + path)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.Error("Request failed: ${response.code}")
            }

            val responseBody = response.body?.string()
            responseBody?.let {
                val result = gson.fromJson(it, responseType)
                return@withContext Result.Success(result)
            } ?: Result.Error("Empty response body")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error<T>(val message: String) : Result<T>()
    }
}

// AuthViewModel.kt
class AuthViewModel : ViewModel() {
    private var sessionId: String = generateSessionId()
    private val macAddress: String = getMacAddress()

    var authState by mutableStateOf<AuthState>(AuthState.PairingCodeEntry)
        private set

    fun submitPairingCode() {
        viewModelScope.launch {
            authState = AuthState.CheckingPairingCode
            when (val result = ApiClient.post(
                "validate-pairing",
                PairingCodeRequest(sessionId),
                PairingCodeResponse::class.java
            )) {
                is ApiClient.Result.Success -> {
                    if (result.data.status == "registered") {
                        fetchAuthToken()
                    } else {
                        authState = AuthState.PairingCodeValidated(false)
                    }
                }
                is ApiClient.Result.Error -> {
                    authState = AuthState.Error(result.message)
                }
            }
        }
    }

    fun handleGoogleLogin() {
        authState = AuthState.GoogleLoginRequired
        viewModelScope.launch {
            when (val result = ApiClient.post(
                "google-login",
                GoogleLoginRequest(macAddress, sessionId),
                Unit::class.java
            )) {
                is ApiClient.Result.Success -> startTokenPolling()
                is ApiClient.Result.Error -> authState = AuthState.Error(result.message)
            }
        }
    }

    private fun startTokenPolling() {
        viewModelScope.launch {
            authState = AuthState.FetchingToken
            while (true) {
                delay(3000) // Poll every 3 seconds
                when (val result = ApiClient.get(
                    "token?session=$sessionId",
                    TokenPollResponse::class.java
                )) {
                    is ApiClient.Result.Success -> {
                        authState = AuthState.LoggedIn(result.data.user)
                        break
                    }
                    is ApiClient.Result.Error -> {
                        if (result.message.contains("token not ready")) continue
                        authState = AuthState.Error(result.message)
                        break
                    }
                }
            }
        }
    }

    private suspend fun fetchAuthToken() {
        authState = AuthState.FetchingToken
        when (val result = ApiClient.get(
            "token?session=$sessionId",
            TokenPollResponse::class.java
        )) {
            is ApiClient.Result.Success -> {
                authState = AuthState.LoggedIn(result.data.user)
            }
            is ApiClient.Result.Error -> {
                authState = AuthState.Error(result.message)
            }
        }
    }

    private fun generateSessionId(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun getMacAddress(): String {
        // Implementation depends on device API level and permissions
        return "02:00:00:00:00:00" // Placeholder
    }
}

// AuthScreen.kt
@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {
    when (val state = viewModel.authState) {
        AuthState.PairingCodeEntry -> PairingCodeScreen(viewModel)
        AuthState.CheckingPairingCode -> LoadingScreen("Verifying pairing code...")
        is AuthState.PairingCodeValidated -> {
            if (state.isRegistered) {
                LoadingScreen("Authenticating...")
            } else {
                GoogleLoginScreen(viewModel)
            }
        }
        AuthState.GoogleLoginRequired -> GoogleLoginScreen(viewModel)
        AuthState.FetchingToken -> LoadingScreen("Finalizing login...")
        is AuthState.LoggedIn -> ProfileScreen(state.user)
        is AuthState.Error -> ErrorScreen(state.message)
    }
}

@Composable
fun PairingCodeScreen(viewModel: AuthViewModel) {
    var code by rememberSaveable { mutableStateOf(viewModel.sessionId) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pairing Code: $code", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { viewModel.submitPairingCode() }) {
            Text("Submit Pairing Code")
        }
    }
}

@Composable
fun GoogleLoginScreen(viewModel: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Google Login Required", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { viewModel.handleGoogleLogin() }) {
            Text("Sign in with Google")
        }
    }
}

@Composable
fun ProfileScreen(user: User) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = user.profilePictureUrl,
            contentDescription = "Profile picture"
        )
        Text(user.name, style = MaterialTheme.typography.h4)
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Error: $message", color = Color.Red)
    }
}
