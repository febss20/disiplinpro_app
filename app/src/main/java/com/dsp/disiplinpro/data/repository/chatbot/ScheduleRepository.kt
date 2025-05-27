package com.dsp.disiplinpro.data.repository.chatbot

import com.dsp.disiplinpro.data.model.Schedule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getUserSchedules(): List<Schedule> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("schedules")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Schedule::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSchedulesByDay(day: String): List<Schedule> {
        val allSchedules = getUserSchedules()
        return allSchedules.filter { it.hari.equals(day, ignoreCase = true) }
            .sortedBy { it.waktuMulai.seconds }
    }

    suspend fun getTodaySchedules(todayName: String): List<Schedule> {
        return getSchedulesByDay(todayName)
    }

    fun getDayOrder(day: String): Int {
        return when (day.lowercase()) {
            "senin" -> 1
            "selasa" -> 2
            "rabu" -> 3
            "kamis" -> 4
            "jumat" -> 5
            "sabtu" -> 6
            "minggu" -> 7
            else -> 8
        }
    }

    suspend fun getSortedSchedules(): List<Schedule> {
        val schedules = getUserSchedules()
        return schedules.sortedWith(
            compareBy({ getDayOrder(it.hari) }, { it.waktuMulai.seconds })
        )
    }
}