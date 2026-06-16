package com.example.scheduliapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduliapp.data.DataRepository
import com.example.scheduliapp.data.Meeting
import com.example.scheduliapp.data.ScheduliData
import com.example.scheduliapp.data.Task
import com.example.scheduliapp.ui.main.MainScreenUiState.Success
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainScreenViewModel(private val dataRepository: DataRepository) : ViewModel() {
    val uiState: StateFlow<MainScreenUiState> =
        dataRepository.scheduliData
            .map<ScheduliData, MainScreenUiState>(::Success)
            .catch { emit(MainScreenUiState.Error(it)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)

    fun updateTaskStatus(taskId: String, newStatus: String) {
        viewModelScope.launch {
            dataRepository.updateTaskStatus(taskId, newStatus)
        }
    }

    fun addCommentToTask(taskId: String, comment: String) {
        viewModelScope.launch {
            dataRepository.addCommentToTask(taskId, comment)
        }
    }

    fun toggleSubTask(taskId: String, subTaskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            dataRepository.toggleSubTask(taskId, subTaskId, isCompleted)
        }
    }

    fun addSubTask(taskId: String, title: String) {
        viewModelScope.launch {
            dataRepository.addSubTask(taskId, title)
        }
    }

    fun createTask(title: String, description: String, priority: String, dueDate: String) {
        viewModelScope.launch {
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                status = "To Do",
                priority = priority,
                dueDate = dueDate,
                source = "Local",
                lastUpdated = System.currentTimeMillis()
            )
            dataRepository.addTask(task)
        }
    }

    fun createMeeting(title: String, description: String, date: String, startTime: String, endTime: String, location: String) {
        viewModelScope.launch {
            val meeting = Meeting(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                date = date,
                startTime = startTime,
                endTime = endTime,
                source = "Local",
                location = location
            )
            dataRepository.addMeeting(meeting)
        }
    }

    fun connectAccount(email: String, type: String) {
        viewModelScope.launch {
            dataRepository.connectAccount(email, type)
        }
    }

    fun disconnectAccount(accountId: String) {
        viewModelScope.launch {
            dataRepository.disconnectAccount(accountId)
        }
    }

    fun syncAccount(accountId: String) {
        viewModelScope.launch {
            dataRepository.syncAccount(accountId)
        }
    }
}

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val data: ScheduliData) : MainScreenUiState
}
