package com.example.disiplinpro.data.repository

import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getUser(): User? {
        return if (userId != null) {
            db.collection("users").document(userId)
                .get().await().toObject(User::class.java)
        } else {
            null
        }
    }

    suspend fun getTasks(): List<Task> {
        return if (userId != null) {
            db.collection("users").document(userId).collection("tasks")
                .get().await().documents.mapNotNull { it.toObject(Task::class.java) }
        } else {
            emptyList()
        }
    }

    fun listenToTasks(onDataChanged: (List<Task>) -> Unit, onError: (Exception) -> Unit) {
        if (userId == null) {
            onError(Exception("User not logged in"))
            return
        }
        db.collection("users").document(userId).collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val taskList = snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) } ?: emptyList()
                onDataChanged(taskList)
            }
    }

    suspend fun addTask(task: Task): Boolean {
        return try {
            if (userId != null) {
                val newTaskRef = db.collection("users").document(userId).collection("tasks").document()
                val taskWithId = task.copy(id = newTaskRef.id)
                newTaskRef.set(taskWithId).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateTask(taskId: String, updatedTask: Task): Boolean {
        return try {
            if (userId != null) {
                db.collection("users").document(userId).collection("tasks").document(taskId)
                    .set(updatedTask).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean): Boolean {
        return try {
            if (userId != null) {
                val updates = hashMapOf<String, Any>(
                    "isCompleted" to isCompleted,
                    "completed" to isCompleted
                )
                db.collection("users").document(userId).collection("tasks").document(taskId)
                    .update(updates).await()
                println("Task $taskId updated to isCompleted = $isCompleted")
                true
            } else {
                println("User ID is null, cannot update task")
                false
            }
        } catch (e: Exception) {
            println("Error updating task completion: ${e.message}")
            false
        }
    }

    suspend fun deleteTask(taskId: String): Boolean {
        return try {
            if (userId != null) {
                db.collection("users").document(userId).collection("tasks").document(taskId)
                    .delete().await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun listenToSchedules(onDataChanged: (List<Schedule>) -> Unit, onError: (Exception) -> Unit) {
        if (userId == null) {
            onError(Exception("User not logged in"))
            return
        }
        db.collection("users").document(userId).collection("schedules")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val scheduleList = snapshot?.documents?.mapNotNull { it.toObject(Schedule::class.java) } ?: emptyList()
                onDataChanged(scheduleList)
            }
    }

    suspend fun addSchedule(schedule: Schedule): Boolean {
        return try {
            if (userId != null) {
                val newScheduleRef = db.collection("users").document(userId).collection("schedules").document()
                val scheduleWithId = schedule.copy(id = newScheduleRef.id)
                newScheduleRef.set(scheduleWithId).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateSchedule(scheduleId: String, updatedSchedule: Schedule): Boolean {
        return try {
            if (userId != null) {
                db.collection("users").document(userId).collection("schedules").document(scheduleId)
                    .set(updatedSchedule).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteSchedule(scheduleId: String): Boolean {
        return try {
            if (userId != null) {
                db.collection("users").document(userId).collection("schedules").document(scheduleId)
                    .delete().await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}