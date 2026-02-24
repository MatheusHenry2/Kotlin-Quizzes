package com.example.kotlinquizzes.core.di

import android.content.Context
import com.example.kotlinquizzes.feature.auth.data.client.GoogleAuthClient
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
        @ApplicationContext context: Context, firebaseAuth: FirebaseAuth,
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
    fun provideQuizRepository(impl: QuizRepositoryImpl): QuizRepository = impl
}