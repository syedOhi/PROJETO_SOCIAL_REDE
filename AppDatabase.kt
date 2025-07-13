package com.example.social_rede_mobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Post::class, User::class, Follow::class, Comment::class, ChatMessageEntity::class], version = 6)

abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
    abstract fun commentDao(): CommentDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buzz_db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // ⚠️ Use only for development/debugging
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
