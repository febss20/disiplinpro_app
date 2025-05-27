package com.dsp.disiplinpro.data.repository.chatbot

import com.dsp.disiplinpro.data.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getAllTasks(): List<Task> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCompletedTasks(): List<Task> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .whereEqualTo("completed", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getIncompleteTasks(): List<Task> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .whereEqualTo("completed", false)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOverdueTasks(currentDate: Date): List<Task> {
        val allTasks = getAllTasks()
        return allTasks.filter { task ->
            !task.completed.isTrue() && task.tanggal.toDate().before(currentDate)
        }
    }

    suspend fun getTodayTasks(currentDate: Date): List<Task> {
        val allTasks = getAllTasks()
        return allTasks.filter { task ->
            !task.completed.isTrue() && isSameDay(task.tanggal.toDate(), currentDate)
        }
    }

    suspend fun getTomorrowTasks(currentDate: Date): List<Task> {
        val allTasks = getAllTasks()
        return allTasks.filter { task ->
            !task.completed.isTrue() && isTomorrow(task.tanggal.toDate(), currentDate)
        }
    }

    suspend fun getThisWeekTasks(currentDate: Date): List<Task> {
        val allTasks = getAllTasks()
        return allTasks.filter { task ->
            !task.completed.isTrue() &&
                    !task.tanggal.toDate().before(currentDate) &&
                    isInSameWeek(task.tanggal.toDate(), currentDate)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private fun isTomorrow(date: Date, today: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date }
        val cal2 = java.util.Calendar.getInstance().apply { time = today }
        cal2.add(java.util.Calendar.DAY_OF_YEAR, 1)
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private fun isInSameWeek(date: Date, today: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply {
            time = date
            firstDayOfWeek = java.util.Calendar.MONDAY
        }
        val cal2 = java.util.Calendar.getInstance().apply {
            time = today
            firstDayOfWeek = java.util.Calendar.MONDAY
        }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.WEEK_OF_YEAR) == cal2.get(java.util.Calendar.WEEK_OF_YEAR)
    }

    private fun Boolean?.isTrue(): Boolean {
        return this == true
    }
}