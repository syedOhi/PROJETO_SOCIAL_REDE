package com.example.social_rede_mobile.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = UserRepository(db.userDao())
    private val context = application.applicationContext

    // 🔒 Holds the currently logged-in user
    val loggedInUser: MutableLiveData<User?> = MutableLiveData()

    // -------------------------------
    // 🔍 Search Functionality
    // -------------------------------

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<User>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchUsers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // -------------------------------
    // 🧑‍💻 Authentication
    // -------------------------------

    fun register(
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.userExists(user.username)) {
                withContext(Dispatchers.Main) {
                    onError("Username already exists")
                }
            } else {
                try {
                    repository.register(user)
                    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("username", user.username).apply()
                    loggedInUser.postValue(user)
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onError("Registration failed")
                    }
                }
            }
        }
    }

    fun login(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.login(username, password)
            if (user != null) {
                val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("username", user.username).apply()
                loggedInUser.postValue(user)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Invalid credentials")
                }
            }
        }
    }

    // ✅ Load current user from prefs (call in Splash or Main)
    fun loadLoggedInUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
            val username = prefs.getString("username", null)
            if (username != null) {
                val user = repository.getUser(username)
                loggedInUser.postValue(user)
            }
        }
    }

    fun updateUserDetails(username: String, bio: String, dob: String, profileImageResId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            db.userDao().updateUserDetails(username, bio, dob, profileImageResId)
            onComplete()
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
            loggedInUser.postValue(user)
        }
    }

    fun getAllUsers(): Flow<List<User>> = repository.getAllUsers()

    fun getUserByUsername(username: String): LiveData<User?> {
        return repository.getUserLiveData(username)
    }

    fun getFollowerCount(username: String): Flow<Int> = repository.getFollowerCount(username)

    fun getFollowingCount(username: String): Flow<Int> = repository.getFollowingCount(username)

    fun followUser(followerUsername: String, followedUsername: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.userDao().followUser(Follow(followerUsername, followedUsername))
        }
    }

    fun unfollowUser(followerUsername: String, followedUsername: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.userDao().unfollowUser(Follow(followerUsername, followedUsername))
        }
    }

    fun isFollowing(followerUsername: String, followedUsername: String): Flow<Boolean> {
        return db.userDao().isFollowing(followerUsername, followedUsername)
    }

    fun getFollowedUsers(username: String): Flow<List<User>> {
        return repository.getFollowedUsers(username)
    }

    fun updateUserDetailsWithUri(
        username: String,
        bio: String,
        dob: String,
        profileImageUri: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateUserDetailsWithUri(username, bio, dob, profileImageUri)
            onComplete()
        }
    }


}
