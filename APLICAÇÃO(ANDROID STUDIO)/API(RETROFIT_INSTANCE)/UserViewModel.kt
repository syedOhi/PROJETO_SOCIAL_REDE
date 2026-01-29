package com.example.social_rede_mobile.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.social_rede_mobile.network.RetrofitInstance
import com.example.social_rede_mobile.network.models.UserCreate
import android.widget.Toast
import com.example.social_rede_mobile.network.models.UserUpdate
import com.example.social_rede_mobile.network.models.UserOut
import com.example.social_rede_mobile.network.models.UserLogin
import android.util.Log

import com.example.social_rede_mobile.network.models.FollowCreate

class UserViewModel(application: Application) : AndroidViewModel(application) {
    // ---------------- FOLLOW COUNTS ----------------
    private val _followerCount = MutableStateFlow(0)
    val followerCount = _followerCount.asStateFlow()

    private val _followingCount = MutableStateFlow(0)
    val followingCount = _followingCount.asStateFlow()

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

    val searchResults: StateFlow<List<UserOut>> = _searchQuery
        .debounce(300) // avoid spam requests
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                flow {
                    try {
                        val results = RetrofitInstance.api.searchUsers(query)
                        emit(results)
                    } catch (e: Exception) {
                        emit(emptyList()) // avoid crash on API error
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // -------------------------------
    // 🧑‍💻 Authentication
    // -------------------------------
    fun register(
        username: String,
        fullName: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 🔥 Call FastAPI (NOT Room)
                val response = RetrofitInstance.api.registerUser(
                    UserCreate(
                        username = username,
                        fullName = fullName,
                        password = password
                    )
                )

                // 🔥 Save login session
                val prefs = getApplication<Application>()
                    .getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)

                prefs.edit().putString("username", username).apply()

                // 🔥 Move back to UI thread
                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Registration failed: ${e.message}")
                }
            }
        }
    }


    fun login(
        username: String,
        password: String,
        onSuccess: (UserOut) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val body = UserCreate(
                    username = username,
                    fullName = "", // backend requires this even for login
                    password = password
                )

                val user = RetrofitInstance.api.loginUser(body)

                val prefs = getApplication<Application>().getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("username", user.username).apply()

                withContext(Dispatchers.Main) {
                    onSuccess(user)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Login failed: ${e.message}")
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
        profileImageUri: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val body = UserUpdate(
                    bio = bio,
                    dob = dob,
                    profileImageUri = profileImageUri,
                    fullName = null
                )

                RetrofitInstance.api.updateUser(username, body)

                withContext(Dispatchers.Main) {
                    onComplete()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Erro ao atualizar perfil: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val apiUser = MutableStateFlow<UserOut?>(null)

    fun fetchUserFromApi(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = RetrofitInstance.api.getUser(username)
                apiUser.value = user
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getUserFromApi(username: String): LiveData<UserOut> = liveData {
        try {
            val data = RetrofitInstance.api.getUser(username)
            emit(data)
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error loading user: ${e.message}")
        }
    }
    fun loadUserApi(username: String): StateFlow<UserOut?> {
        val state = MutableStateFlow<UserOut?>(null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = RetrofitInstance.api.getUser(username)
                state.value = data
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading user profile: ${e.message}")
            }
        }
        return state
    }

    fun loadFollowerCount(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = RetrofitInstance.api.getFollowerCount(username)
                _followerCount.value = count
            } catch (e: Exception) {
                Log.e("FollowAPI", "Follower count error: ${e.message}")
            }
        }
    }

    fun loadFollowingCount(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = RetrofitInstance.api.getFollowingCount(username)
                _followingCount.value = count
            } catch (e: Exception) {
                Log.e("FollowAPI", "Following count error: ${e.message}")
            }
        }
    }

    fun loadIsFollowing(follower: String, followed: String) = flow {
        emit(RetrofitInstance.api.isFollowing(follower, followed))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)


    fun followUserApi(follower: String, followed: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitInstance.api.followUser(
                    FollowCreate(
                        followerUsername = follower,
                        followedUsername = followed
                    )
                )
                // 🔁 after backend confirms, refresh follower count for profile owner
                loadFollowerCount(followed)
            } catch (e: Exception) {
                Log.e("FollowAPI", "Error following user: ${e.message}")
            }
        }
    }

    fun unfollowUserApi(follower: String, followed: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitInstance.api.unfollowUser(
                    FollowCreate(
                        followerUsername = follower,
                        followedUsername = followed
                    )
                )
                // 🔁 refresh again from backend
                loadFollowerCount(followed)
            } catch (e: Exception) {
                Log.e("FollowAPI", "Error unfollowing user: ${e.message}")
            }
        }
    }

    fun incrementFollowerCount() {
        _followerCount.value = _followerCount.value + 1
    }

    fun decrementFollowerCount() {
        if (_followerCount.value > 0)
            _followerCount.value = _followerCount.value - 1
    }



}
