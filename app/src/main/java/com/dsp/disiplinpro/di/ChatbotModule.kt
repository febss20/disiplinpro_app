package com.dsp.disiplinpro.di

import android.content.Context
import com.dsp.disiplinpro.data.repository.NotificationRepository
import com.dsp.disiplinpro.data.repository.chatbot.AppDataRepository
import com.dsp.disiplinpro.data.repository.chatbot.ChatHistoryRepository
import com.dsp.disiplinpro.data.repository.chatbot.FAQRepository
import com.dsp.disiplinpro.data.repository.chatbot.NotificationInfoRepository
import com.dsp.disiplinpro.data.repository.chatbot.ScheduleRepository
import com.dsp.disiplinpro.data.repository.chatbot.SubjectRepository
import com.dsp.disiplinpro.data.repository.chatbot.TaskRepository
import com.dsp.disiplinpro.data.repository.chatbot.UserRepository
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
    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepository()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): TaskRepository {
        return TaskRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ScheduleRepository {
        return ScheduleRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserRepository {
        return UserRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideFAQRepository(): FAQRepository {
        return FAQRepository()
    }

    @Provides
    @Singleton
    fun provideNotificationInfoRepository(
        notificationRepository: NotificationRepository
    ): NotificationInfoRepository {
        return NotificationInfoRepository(notificationRepository)
    }

    @Provides
    @Singleton
    fun provideSubjectRepository(
        taskRepository: TaskRepository,
        scheduleRepository: ScheduleRepository
    ): SubjectRepository {
        return SubjectRepository(taskRepository, scheduleRepository)
    }

    @Provides
    @Singleton
    fun provideAppDataRepository(
        taskRepository: TaskRepository,
        scheduleRepository: ScheduleRepository,
        userRepository: UserRepository,
        faqRepository: FAQRepository,
        subjectRepository: SubjectRepository,
        notificationInfoRepository: NotificationInfoRepository
    ): AppDataRepository {
        return AppDataRepository(
            taskRepository,
            scheduleRepository,
            userRepository,
            faqRepository,
            subjectRepository,
            notificationInfoRepository
        )
    }

    @Provides
    @Singleton
    fun provideChatHistoryRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ChatHistoryRepository {
        return ChatHistoryRepository(firestore, auth)
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