package com.example.kotlinquizzes.core.di

import android.content.Context
import androidx.room.Room
import com.example.kotlinquizzes.feature.auth.data.client.GoogleAuthClient
import com.example.kotlinquizzes.feature.quiz.data.local.db.AppDatabase
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.QuizDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.QuizProgressDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.TagPerformanceDao
import com.example.kotlinquizzes.feature.quiz.data.local.db.dao.UserStatsDao
import com.example.kotlinquizzes.feature.quiz.data.repository.QuizRepositoryImpl
import com.example.kotlinquizzes.feature.quiz.domain.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideGoogleAuthClient(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
    ): GoogleAuthClient {
        return GoogleAuthClient(context, firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kotlin_quizzes_db",
        ).build()
    }

    @Provides
    fun provideQuizDao(db: AppDatabase): QuizDao = db.quizDao()

    @Provides
    fun provideTagPerformanceDao(db: AppDatabase): TagPerformanceDao = db.tagPerformanceDao()

    @Provides
    fun provideQuizProgressDao(db: AppDatabase): QuizProgressDao = db.quizProgressDao()

    @Provides
    fun provideUserStatsDao(db: AppDatabase): UserStatsDao = db.userStatsDao()

    @Provides
    @Singleton
    fun provideQuizRepository(impl: QuizRepositoryImpl): QuizRepository = impl
}
