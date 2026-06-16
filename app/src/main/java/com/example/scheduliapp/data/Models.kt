package com.example.scheduliapp.data

import kotlinx.serialization.Serializable

@Serializable
data class SubTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: String, // "To Do", "In Progress", "In Review", "Done"
    val priority: String, // "Low", "Medium", "High"
    val dueDate: String, // "YYYY-MM-DD"
    val source: String, // "Local", "Gmail", "Outlook"
    val lastUpdated: Long,
    val subTasks: List<SubTask> = emptyList()
)

@Serializable
data class Meeting(
    val id: String,
    val title: String,
    val description: String,
    val date: String, // "YYYY-MM-DD"
    val startTime: String, // "HH:MM"
    val endTime: String, // "HH:MM"
    val source: String, // "Local", "Gmail", "Outlook"
    val location: String
)

@Serializable
data class ActivityLog(
    val id: String,
    val taskId: String,
    val timestamp: Long,
    val message: String
)

@Serializable
data class ConnectedAccount(
    val id: String,
    val email: String,
    val type: String, // "Gmail", "Outlook"
    val isConnected: Boolean,
    val syncTasks: Boolean = true,
    val syncCalendar: Boolean = true
)

@Serializable
data class ScheduliData(
    val tasks: List<Task> = emptyList(),
    val meetings: List<Meeting> = emptyList(),
    val activityLogs: List<ActivityLog> = emptyList(),
    val accounts: List<ConnectedAccount> = emptyList()
)
