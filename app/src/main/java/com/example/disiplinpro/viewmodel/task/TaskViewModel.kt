package com.example.disiplinpro.viewmodel.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.model.Task
import com.example.disiplinpro.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        viewModelScope.launch {
            val newTasks = repository.getTasks()
            _tasks.value = newTasks
            println("Fetched ${newTasks.size} tasks")
        }
    }
    fun addTask(task: Task) {
        viewModelScope.launch {
            val success = repository.addTask(task)
            if (success) {
                fetchTasks()
            }
        }
    }

    fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val success = repository.updateTaskCompletion(taskId, isCompleted)
            if (success) {
                fetchTasks()
                println("Tasks refreshed after update")
            } else {
                println("Failed to update task completion for taskId: $taskId")
            }
        }
    }

    fun updateTask(taskId: String, updatedTask: Task) {
        viewModelScope.launch {
            val success = repository.updateTask(taskId, updatedTask)
            if (success) {
                fetchTasks()
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val success = repository.deleteTask(taskId)
            if (success) {
                fetchTasks()
            }
        }
    }
}