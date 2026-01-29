package com.example.social_rede_mobile.network

import com.example.social_rede_mobile.network.models.*
import retrofit2.http.*

interface ApiService {

    // ------------------ USERS ------------------
    @POST("users/")
    suspend fun registerUser(@Body body: UserCreate): UserOut

    @POST("auth/login")
    suspend fun loginUser(@Body body: UserCreate): UserOut

    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): UserOut

    @PUT("users/{username}")
    suspend fun updateUser(
        @Path("username") username: String,
        @Body body: UserUpdate
    ): UserOut
    //to search
    @GET("users/search")
    suspend fun searchUsers(@Query("query") query: String): List<UserOut>

    // ------------------ POSTS ------------------
    @POST("posts/")
    suspend fun createPost(@Body body: PostCreate): PostOut

    @GET("posts/")
    suspend fun getAllPosts(): List<PostOut>

    @GET("posts/user/{username}")
    suspend fun getUserPosts(@Path("username") username: String): List<PostOut>

    @POST("likes/{postId}/{username}")
    suspend fun likePost(
        @Path("postId") postId: Int,
        @Path("username") username: String
    )

    @DELETE("likes/{postId}/{username}")
    suspend fun unlikePost(
        @Path("postId") postId: Int,
        @Path("username") username: String
    )
    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Int,
        @Query("username") username: String
    )

    // simplest report: query params (no new model needed)
    @POST("posts/report")
    suspend fun reportPost(
        @Query("postId") postId: Int,
        @Query("reportedBy") reportedBy: String
    )
    @GET("posts/{username}")
    suspend fun getAllPosts(
        @Path("username") username: String
    ): List<PostOut>


    // ------------------ COMMENTS ------------------
    @POST("comments/")
    suspend fun createComment(@Body comment: CommentCreate): CommentOut

    @GET("comments/{postId}")
    suspend fun getComments(@Path("postId") postId: Int): List<CommentOut>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") id: Int)

    // ------------------ FOLLOW ------------------
    @POST("follows/")
    suspend fun followUser(@Body body: FollowCreate): FollowOut

    @HTTP(method = "DELETE", path = "follows/", hasBody = true)
    suspend fun unfollowUser(@Body body: FollowCreate): FollowOut

    @GET("follows/followerCount/{username}")
    suspend fun getFollowerCount(@Path("username") username: String): Int

    @GET("follows/followingCount/{username}")
    suspend fun getFollowingCount(@Path("username") username: String): Int

    @GET("follows/isFollowing/{follower}/{followed}")
    suspend fun isFollowing(
        @Path("follower") follower: String,
        @Path("followed") followed: String
    ): Boolean



    // ------------------ NOTIFICATIONS ------------------
    @POST("notifications/")
    suspend fun sendNotification(@Body body: NotificationCreate): NotificationOut

    @GET("notifications/{username}")
    suspend fun getNotifications(@Path("username") username: String): List<NotificationOut>

    @GET("chat/conversations/{username}")
    suspend fun getConversations(
        @Path("username") username: String
    ): List<String>

    @GET("chat/conversation")
    suspend fun getConversation(
        @Query("user1") user1: String,
        @Query("user2") user2: String
    ): List<ChatMessageOut>
    @POST("chat/send")
    suspend fun sendMessage(
        @Body body: ChatMessageCreate
    ): ChatMessageOut
    @POST("chat/mark-read")
    suspend fun markRead(
        @Query("sender") sender: String,
        @Query("receiver") receiver: String
    )
    @GET("follows/following/{username}")
    suspend fun getFollowing(
        @Path("username") username: String
    ): List<String>

    // -------------------- CHAT REQUESTS --------------------

    @GET("chat/requests/{username}")
    suspend fun getChatRequests(
        @Path("username") username: String
    ): List<ChatRequestOut>

    @POST("chat/requests/accept")
    suspend fun acceptChatRequest(
        @Query("sender") sender: String,
        @Query("receiver") receiver: String
    )

    @DELETE("chat/requests")
    suspend fun deleteChatRequest(
        @Query("sender") sender: String,
        @Query("receiver") receiver: String
    )


}
