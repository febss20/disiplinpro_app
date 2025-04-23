package com.example.disiplinpro.data.repository

import android.util.Log
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.data.model.Schedule
import com.example.disiplinpro.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepository"
private const val USERS_COLLECTION = "users"
private const val TASKS_COLLECTION = "tasks"
private const val SCHEDULES_COLLECTION = "schedules"

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private fun getUserDocRef() = db.collection(USERS_COLLECTION).document(userId ?: "")
    private fun getTasksCollectionRef() = getUserDocRef().collection(TASKS_COLLECTION)
    private fun getSchedulesCollectionRef() = getUserDocRef().collection(SCHEDULES_COLLECTION)

    suspend fun getUser(): User? {
        return if (userId != null) {
            try {
                getUserDocRef().get().await().toObject(User::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user: ${e.message}")
                null
            }
        } else {
            Log.w(TAG, "Cannot fetch user: User not logged in")
            null
        }
    }

    suspend fun getTasks(): List<Task> {
        return if (userId != null) {
            try {
                getTasksCollectionRef().get().await().documents
                    .mapNotNull { it.toObject(Task::class.java) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks: ${e.message}")
                emptyList()
            }
        } else {
            Log.w(TAG, "Cannot fetch tasks: User not logged in")
            emptyList()
        }
    }

    suspend fun getSchedules(): List<Schedule> {
        return if (userId != null) {
            try {
                getSchedulesCollectionRef().get().await().documents
                    .mapNotNull { it.toObject(Schedule::class.java) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching schedules: ${e.message}")
                emptyList()
            }
        } else {
            Log.w(TAG, "Cannot fetch schedules: User not logged in")
            emptyList()
        }
    }

    fun listenToTasks(onDataChanged: (List<Task>) -> Unit, onError: (Exception) -> Unit) {
        if (userId == null) {
            val exception = Exception("User not logged in")
            Log.w(TAG, exception.message ?: "Unknown error")
            onError(exception)
            return
        }
        getTasksCollectionRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to tasks: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            val taskList = snapshot?.documents?.mapNotNull {
                it.toObject(Task::class.java)
            } ?: emptyList()

            Log.d(TAG, "Received ${taskList.size} tasks from Firestore")
            onDataChanged(taskList)
        }
    }

    suspend fun addTask(task: Task): Boolean {
        return try {
            if (userId != null) {
                val newTaskRef = getTasksCollectionRef().document()
                val taskWithId = task.copy(
                    id = newTaskRef.id,
                    isCompleted = task.isCompleted,
                    completed = task.isCompleted
                )
                newTaskRef.set(taskWithId).await()
                Log.d(TAG, "Task added successfully: ${newTaskRef.id}")
                true
            } else {
                Log.w(TAG, "Cannot add task: User not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding task: ${e.message}")
            false
        }
    }

    suspend fun updateTask(taskId: String, updatedTask: Task): Boolean {
        return try {
            if (userId != null) {
                val synchronizedTask = updatedTask.copy(
                    isCompleted = updatedTask.isCompleted,
                    completed = updatedTask.isCompleted
                )
                getTasksCollectionRef().document(taskId).set(synchronizedTask).await()
                Log.d(TAG, "Task updated successfully: $taskId")
                true
            } else {
                Log.w(TAG, "Cannot update task: User not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task: ${e.message}")
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
                getTasksCollectionRef().document(taskId).update(updates).await()
                Log.d(TAG, "Task $taskId completion status updated to: $isCompleted")
                true
            } else {
                Log.w(TAG, "Cannot update task completion: User not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task completion: ${e.message}")
            false
        }
    }

    suspend fun deleteTask(taskId: String): Boolean {
        return try {
            if (userId != null) {
                getTasksCollectionRef().document(taskId).delete().await()
                Log.d(TAG, "Task deleted successfully: $taskId")
                true
            } else {
                Log.w(TAG, "Cannot delete task: User not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task: ${e.message}")
            false
        }
    }

    fun listenToSchedules(onDataChanged: (List<Schedule>) -> Unit, onError: (Exception) -> Unit) {
        if (userId == null) {
            val exception = Exception("User not logged in")
            Log.w(TAG, exception.message ?: "Unknown error")
            onError(exception)
            return
        }
        getSchedulesCollectionRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to schedules: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            val scheduleList = snapshot?.documents?.mapNotNull {
                it.toObject(Schedule::class.java)
            } ?: emptyList()

            Log.d(TAG, "Received ${scheduleList.size} schedules from Firestore")
            onDataChanged(scheduleList)
        }
    }

    suspend fun addSchedule(schedule: Schedule): Boolean {
        return try {
            if (userId != null) {
                val newScheduleRef = getSchedulesCollectionRef().document()
                val scheduleWithId = schedule.copy(id = newScheduleRef.id)
                newScheduleRef.set(scheduleWithId).await()
                Log.d(TAG, "Schedule added successfully: ${newScheduleRef.id}")
                true
            } else {
                Log.w(TAG, "Cannot add schedule: User not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding schedule: ${e.message}")
            false
        }
    }

    suspend fun updateSchedule(scheduleId: String, updatedSchedule: Schedule): Boolean {
        return try {
            if (userId != null) {
                getSchedulesCollectionRef().document(scheduleId).set(updatedSchedule).await()
                Log.d(TAG, "Schedule updated successfully: $scheduleId")
                true
            } else {
                Log.w(TAG, "Cannot update schedule: User not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating schedule: ${e.message}")
            false
        }
    }

    suspend fun deleteSchedule(scheduleId: String): Boolean {
        return try {
            if (userId != null) {
                getSchedulesCollectionRef().document(scheduleId).delete().await()
                Log.d(TAG, "Schedule deleted successfully: $scheduleId")
                true
            } else {
                Log.w(TAG, "Cannot delete schedule: User not logged in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting schedule: ${e.message}")
            false
        }
    }
}