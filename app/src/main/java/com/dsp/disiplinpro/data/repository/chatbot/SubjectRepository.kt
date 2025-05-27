package com.dsp.disiplinpro.data.repository.chatbot

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepository @Inject constructor(
    private val taskRepository: TaskRepository,
    private val scheduleRepository: ScheduleRepository
) {
    suspend fun getSubjects(): Set<String> {
        val tasks = taskRepository.getAllTasks()
        val schedules = scheduleRepository.getUserSchedules()
        val subjects = mutableSetOf<String>()

        tasks.forEach { task ->
            if (task.matkul.isNotEmpty()) {
                subjects.add(task.matkul)
            }
        }

        schedules.forEach { schedule ->
            if (schedule.matkul.isNotEmpty()) {
                subjects.add(schedule.matkul)
            }
        }

        return subjects
    }

    suspend fun getTasksBySubject(subject: String): List<com.dsp.disiplinpro.data.model.Task> {
        val allTasks = taskRepository.getAllTasks()
        return allTasks.filter {
            it.matkul.equals(subject, ignoreCase = true)
        }
    }

    suspend fun getSchedulesBySubject(subject: String): List<com.dsp.disiplinpro.data.model.Schedule> {
        val allSchedules = scheduleRepository.getUserSchedules()
        return allSchedules.filter {
            it.matkul.contains(subject, ignoreCase = true)
        }
    }

    suspend fun getSubjectStats(): Map<String, Map<String, Any>> {
        val subjects = getSubjects()
        val allTasks = taskRepository.getAllTasks()
        val result = mutableMapOf<String, Map<String, Any>>()

        subjects.forEach { subject ->
            val subjectTasks = allTasks.filter { it.matkul.equals(subject, ignoreCase = true) }
            val completedTasks = subjectTasks.count { it.completed == true }
            val incompleteTasks = subjectTasks.size - completedTasks
            val schedules = getSchedulesBySubject(subject)

            result[subject] = mapOf(
                "totalTasks" to subjectTasks.size,
                "completedTasks" to completedTasks,
                "incompleteTasks" to incompleteTasks,
                "completionRate" to if (subjectTasks.isNotEmpty()) {
                    completedTasks.toFloat() / subjectTasks.size
                } else 0f,
                "scheduleCount" to schedules.size
            )
        }

        return result
    }
}