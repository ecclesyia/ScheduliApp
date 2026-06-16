package com.example.scheduliapp.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

interface DataRepository {
    val scheduliData: StateFlow<ScheduliData>
    suspend fun addTask(task: Task)
    suspend fun updateTaskStatus(taskId: String, newStatus: String)
    suspend fun addCommentToTask(taskId: String, comment: String)
    suspend fun addMeeting(meeting: Meeting)
    suspend fun connectAccount(email: String, type: String)
    suspend fun disconnectAccount(accountId: String)
    suspend fun syncAccount(accountId: String)
    suspend fun toggleSubTask(taskId: String, subTaskId: String, isCompleted: Boolean)
    suspend fun addSubTask(taskId: String, title: String)
}

class LocalJsonDataRepository(private val context: Context) : DataRepository {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val dataFile = File(context.filesDir, "scheduli_data.json")
    
    private val _scheduliData = MutableStateFlow(ScheduliData())
    override val scheduliData: StateFlow<ScheduliData> = _scheduliData.asStateFlow()
    
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    
    init {
        repositoryScope.launch {
            loadData()
        }
    }
    
    private suspend fun loadData() = withContext(Dispatchers.IO) {
        try {
            if (dataFile.exists()) {
                val text = dataFile.readText()
                val parsed = json.decodeFromString<ScheduliData>(text)
                _scheduliData.value = parsed
            } else {
                // Initialize with default tasks and meetings
                val initialData = createDefaultData()
                _scheduliData.value = initialData
                saveDataToFile(initialData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback in case of corruption
            val initialData = createDefaultData()
            _scheduliData.value = initialData
        }
    }
    
    private suspend fun saveDataToFile(data: ScheduliData) = withContext(Dispatchers.IO) {
        try {
            val text = json.encodeToString(ScheduliData.serializer(), data)
            dataFile.writeText(text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateStateAndSave(updateBlock: (ScheduliData) -> ScheduliData) {
        val updated = updateBlock(_scheduliData.value)
        _scheduliData.value = updated
        repositoryScope.launch {
            saveDataToFile(updated)
        }
    }
    
    override suspend fun addTask(task: Task) {
        updateStateAndSave { current ->
            val logs = current.activityLogs.toMutableList()
            logs.add(
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    taskId = task.id,
                    timestamp = System.currentTimeMillis(),
                    message = "Task created: \"${task.title}\" with priority ${task.priority}"
                )
            )
            current.copy(
                tasks = current.tasks + task,
                activityLogs = logs
            )
        }
    }
    
    override suspend fun updateTaskStatus(taskId: String, newStatus: String) {
        updateStateAndSave { current ->
            val tasks = current.tasks.map {
                if (it.id == taskId) {
                    it.copy(status = newStatus, lastUpdated = System.currentTimeMillis())
                } else it
            }
            
            val taskName = current.tasks.find { it.id == taskId }?.title ?: "Unknown Task"
            val logs = current.activityLogs.toMutableList()
            logs.add(
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    taskId = taskId,
                    timestamp = System.currentTimeMillis(),
                    message = "Status updated to \"$newStatus\""
                )
            )
            
            current.copy(tasks = tasks, activityLogs = logs)
        }
    }
    
    override suspend fun addCommentToTask(taskId: String, comment: String) {
        updateStateAndSave { current ->
            val logs = current.activityLogs.toMutableList()
            logs.add(
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    taskId = taskId,
                    timestamp = System.currentTimeMillis(),
                    message = "Comment: \"$comment\""
                )
            )
            current.copy(activityLogs = logs)
        }
    }
    
    override suspend fun addMeeting(meeting: Meeting) {
        updateStateAndSave { current ->
            current.copy(meetings = current.meetings + meeting)
        }
    }
    
    override suspend fun connectAccount(email: String, type: String) {
        updateStateAndSave { current ->
            val accountId = UUID.randomUUID().toString()
            val newAccount = ConnectedAccount(
                id = accountId,
                email = email,
                type = type,
                isConnected = true
            )
            
            // Generate mock synced meetings and tasks based on account type
            val extraMeetings = mutableListOf<Meeting>()
            val extraTasks = mutableListOf<Task>()
            val extraLogs = mutableListOf<ActivityLog>()
            
            val today = getFormattedDate(0)
            val tomorrow = getFormattedDate(1)
            val dayAfter = getFormattedDate(2)
            
            if (type == "Gmail") {
                // Gmail Calendar Sync Simulation
                val m1Id = UUID.randomUUID().toString()
                extraMeetings.add(
                    Meeting(
                        id = m1Id,
                        title = "Sprint Planning (Google Calendar)",
                        description = "Review board backlogs and plan upcoming sprint features.",
                        date = today,
                        startTime = "11:00",
                        endTime = "12:00",
                        source = "Gmail",
                        location = "Google Meet"
                    )
                )
                
                val m2Id = UUID.randomUUID().toString()
                extraMeetings.add(
                    Meeting(
                        id = m2Id,
                        title = "UX Team Sync",
                        description = "Sync on high fidelity screens for the account panel.",
                        date = tomorrow,
                        startTime = "09:30",
                        endTime = "10:00",
                        source = "Gmail",
                        location = "Google Meet"
                    )
                )
                
                // Tasks from Gmail (Simulated Tasks Due)
                val t1Id = UUID.randomUUID().toString()
                extraTasks.add(
                    Task(
                        id = t1Id,
                        title = "Draft API Integration Spec",
                        description = "Review the Google/Outlook calendar API contract and document sync endpoints. (Imported from Gmail Action Item)",
                        status = "To Do",
                        priority = "High",
                        dueDate = tomorrow,
                        source = "Gmail",
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                
                extraLogs.add(
                    ActivityLog(
                        id = UUID.randomUUID().toString(),
                        taskId = t1Id,
                        timestamp = System.currentTimeMillis(),
                        message = "Imported from Gmail account ($email)"
                    )
                )
            } else {
                // Outlook Calendar Sync Simulation
                val m1Id = UUID.randomUUID().toString()
                extraMeetings.add(
                    Meeting(
                        id = m1Id,
                        title = "Stakeholder Review (Outlook)",
                        description = "Present progress demo to product managers.",
                        date = tomorrow,
                        startTime = "15:00",
                        endTime = "16:00",
                        source = "Outlook",
                        location = "Microsoft Teams"
                    )
                )
                
                val m2Id = UUID.randomUUID().toString()
                extraMeetings.add(
                    Meeting(
                        id = m2Id,
                        title = "Weekly Architecture Alignment",
                        description = "Discuss room migrations and serialization updates.",
                        date = dayAfter,
                        startTime = "14:00",
                        endTime = "14:45",
                        source = "Outlook",
                        location = "Teams Meeting Room"
                    )
                )
                
                // Outlook Flagged email tasks
                val t1Id = UUID.randomUUID().toString()
                extraTasks.add(
                    Task(
                        id = t1Id,
                        title = "Resolve Security Audit items",
                        description = "Flagged Email: Please look into security group permissions for the new staging build. (Imported from Outlook)",
                        status = "To Do",
                        priority = "High",
                        dueDate = dayAfter,
                        source = "Outlook",
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                extraLogs.add(
                    ActivityLog(
                        id = UUID.randomUUID().toString(),
                        taskId = t1Id,
                        timestamp = System.currentTimeMillis(),
                        message = "Imported from Outlook account ($email)"
                    )
                )
            }
            
            current.copy(
                accounts = current.accounts + newAccount,
                meetings = current.meetings + extraMeetings,
                tasks = current.tasks + extraTasks,
                activityLogs = current.activityLogs + extraLogs
            )
        }
    }
    
    override suspend fun disconnectAccount(accountId: String) {
        updateStateAndSave { current ->
            val account = current.accounts.find { it.id == accountId }
            val accountType = account?.type ?: ""
            
            // Clean up tasks/meetings synced from this account
            val updatedTasks = current.tasks.filter { it.source != accountType }
            val updatedMeetings = current.meetings.filter { it.source != accountType }
            val updatedAccounts = current.accounts.filter { it.id != accountId }
            
            current.copy(
                accounts = updatedAccounts,
                tasks = updatedTasks,
                meetings = updatedMeetings
            )
        }
    }
    
    override suspend fun syncAccount(accountId: String) {
        // Simulates a manual sync trigger
        updateStateAndSave { current ->
            // Add a log for all tasks synced from this account
            val account = current.accounts.find { it.id == accountId }
            val logs = current.activityLogs.toMutableList()
            if (account != null) {
                val accountTasks = current.tasks.filter { it.source == account.type }
                accountTasks.forEach { task ->
                    logs.add(
                        ActivityLog(
                            id = UUID.randomUUID().toString(),
                            taskId = task.id,
                            timestamp = System.currentTimeMillis(),
                            message = "Synced successfully with ${account.type} servers."
                        )
                    )
                }
            }
            current.copy(activityLogs = logs)
        }
    }
    
    override suspend fun toggleSubTask(taskId: String, subTaskId: String, isCompleted: Boolean) {
        updateStateAndSave { current ->
            val tasks = current.tasks.map { task ->
                if (task.id == taskId) {
                    val subtasks = task.subTasks.map { sub ->
                        if (sub.id == subTaskId) {
                            sub.copy(isCompleted = isCompleted)
                        } else sub
                    }
                    task.copy(subTasks = subtasks, lastUpdated = System.currentTimeMillis())
                } else task
            }
            
            val task = current.tasks.find { it.id == taskId }
            val subTask = task?.subTasks?.find { it.id == subTaskId }
            val subName = subTask?.title ?: "Unknown Subtask"
            val statusWord = if (isCompleted) "Completed" else "Incomplete"
            
            val logs = current.activityLogs.toMutableList()
            logs.add(
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    taskId = taskId,
                    timestamp = System.currentTimeMillis(),
                    message = "Sub-task \"$subName\" marked as $statusWord"
                )
            )
            
            current.copy(tasks = tasks, activityLogs = logs)
        }
    }
    
    override suspend fun addSubTask(taskId: String, title: String) {
        updateStateAndSave { current ->
            val subId = UUID.randomUUID().toString()
            val newSub = SubTask(id = subId, title = title, isCompleted = false)
            
            val tasks = current.tasks.map { task ->
                if (task.id == taskId) {
                    task.copy(subTasks = task.subTasks + newSub, lastUpdated = System.currentTimeMillis())
                } else task
            }
            
            val logs = current.activityLogs.toMutableList()
            logs.add(
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    taskId = taskId,
                    timestamp = System.currentTimeMillis(),
                    message = "Added sub-task checklist: \"$title\""
                )
            )
            
            current.copy(tasks = tasks, activityLogs = logs)
        }
    }
    
    // Helpers
    private fun createDefaultData(): ScheduliData {
        val today = getFormattedDate(0)
        val tomorrow = getFormattedDate(1)
        val in3Days = getFormattedDate(3)
        val in5Days = getFormattedDate(5)
        
        val t1 = UUID.randomUUID().toString()
        val t2 = UUID.randomUUID().toString()
        val t3 = UUID.randomUUID().toString()
        val t4 = UUID.randomUUID().toString()
        val t5 = UUID.randomUUID().toString()
        
        val tasks = listOf(
            Task(
                t1, 
                "Design Scheduli App Architecture", 
                "Define standard package structures, UI screens, local persistence JSON models, and bottom bar navigation.", 
                "Done", 
                "High", 
                today, 
                "Local", 
                System.currentTimeMillis(),
                subTasks = listOf(
                    SubTask(UUID.randomUUID().toString(), "Define Package Structure", true),
                    SubTask(UUID.randomUUID().toString(), "Plan UI Screens", true),
                    SubTask(UUID.randomUUID().toString(), "Design JSON storage models", true)
                )
            ),
            Task(
                t2, 
                "Setup Android Studio Project", 
                "Initialize project files, configure build.gradle dependencies including Compose and serialization libraries.", 
                "Done", 
                "High", 
                today, 
                "Local", 
                System.currentTimeMillis(),
                subTasks = listOf(
                    SubTask(UUID.randomUUID().toString(), "Initialize Gradle template", true),
                    SubTask(UUID.randomUUID().toString(), "Configure build dependencies", true)
                )
            ),
            Task(
                t3, 
                "Implement Scrum Board Layout", 
                "Build dynamic card lists for To Do, In Progress, In Review, Done columns. Allow users to change task lanes.", 
                "In Progress", 
                "High", 
                tomorrow, 
                "Local", 
                System.currentTimeMillis(),
                subTasks = listOf(
                    SubTask(UUID.randomUUID().toString(), "Create Column Lanes", true),
                    SubTask(UUID.randomUUID().toString(), "Design Task Card Component", true),
                    SubTask(UUID.randomUUID().toString(), "Implement Status Shift Buttons", false),
                    SubTask(UUID.randomUUID().toString(), "Implement Detail Bottom Sheet Dialog", false)
                )
            ),
            Task(
                t4, 
                "Mock Google/Outlook Calendar Integrations", 
                "Construct the account connection panel, sync toggle configs, and simulated event pulling flows.", 
                "To Do", 
                "Medium", 
                in3Days, 
                "Local", 
                System.currentTimeMillis(),
                subTasks = listOf(
                    SubTask(UUID.randomUUID().toString(), "Build Accounts Screen Interface", false),
                    SubTask(UUID.randomUUID().toString(), "Design Mock Connection flow", false),
                    SubTask(UUID.randomUUID().toString(), "Document OAuth setup guidelines", false)
                )
            ),
            Task(
                t5, 
                "Refactor Design Theme & Typography", 
                "Apply dark-mode primary style, glassmorphic UI card borders, and smooth transition animations.", 
                "To Do", 
                "Low", 
                in5Days, 
                "Local", 
                System.currentTimeMillis(),
                subTasks = listOf(
                    SubTask(UUID.randomUUID().toString(), "Define Premium Colors", false),
                    SubTask(UUID.randomUUID().toString(), "Configure Typography and fonts", false)
                )
            )
        )
        
        val logs = listOf(
            ActivityLog(UUID.randomUUID().toString(), t1, System.currentTimeMillis(), "Task created: \"Design Scheduli App Architecture\""),
            ActivityLog(UUID.randomUUID().toString(), t1, System.currentTimeMillis(), "Status updated to \"Done\""),
            ActivityLog(UUID.randomUUID().toString(), t2, System.currentTimeMillis(), "Task created: \"Setup Android Studio Project\""),
            ActivityLog(UUID.randomUUID().toString(), t2, System.currentTimeMillis(), "Status updated to \"Done\""),
            ActivityLog(UUID.randomUUID().toString(), t3, System.currentTimeMillis(), "Task created: \"Implement Scrum Board Layout\""),
            ActivityLog(UUID.randomUUID().toString(), t3, System.currentTimeMillis(), "Status updated to \"In Progress\""),
            ActivityLog(UUID.randomUUID().toString(), t4, System.currentTimeMillis(), "Task created: \"Mock Google/Outlook Calendar Integrations\""),
            ActivityLog(UUID.randomUUID().toString(), t5, System.currentTimeMillis(), "Task created: \"Refactor Design Theme & Typography\"")
        )
        
        val meetings = listOf(
            Meeting(UUID.randomUUID().toString(), "Project Kickoff", "Initial project alignment and backlog definition.", today, "09:00", "10:00", "Local", "Conference Room A"),
            Meeting(UUID.randomUUID().toString(), "Scrum Architecture Review", "Review project directory structures and Room database diagrams.", today, "14:00", "15:00", "Local", "Google Meet Link"),
            Meeting(UUID.randomUUID().toString(), "Daily Standup Meeting", "Discuss blocker items, drag & drop updates, and schedule layouts.", tomorrow, "10:00", "10:15", "Local", "Google Meet Link")
        )
        
        return ScheduliData(tasks, meetings, logs, emptyList())
    }
    
    private fun getFormattedDate(daysOffset: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return sdf.format(cal.time)
    }
}
