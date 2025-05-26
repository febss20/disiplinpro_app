package com.dsp.disiplinpro.di

import android.content.Context
import com.dsp.disiplinpro.data.repository.AppDataRepository
import com.dsp.disiplinpro.data.service.GeminiChatManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatbotModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAppDataRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): AppDataRepository {
        return AppDataRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideGeminiChatManager(
        appDataRepository: AppDataRepository,
        @ApplicationContext context: Context
    ): GeminiChatManager {
        return GeminiChatManager(appDataRepository, context)
    }
}