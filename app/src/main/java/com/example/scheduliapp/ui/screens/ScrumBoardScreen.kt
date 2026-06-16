package com.example.scheduliapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.scheduliapp.data.ActivityLog
import com.example.scheduliapp.data.Task
import com.example.scheduliapp.theme.*
import com.example.scheduliapp.ui.main.MainScreenViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrumBoardScreen(
    viewModel: MainScreenViewModel,
    tasks: List<Task>,
    activityLogs: List<ActivityLog>,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    
    val columns = listOf("To Do", "In Progress", "In Review", "Done")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scrum Board",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${tasks.size} active tasks across stages",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Task", fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            val isPortrait = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 600
            
            if (isPortrait) {
                var selectedColumnIndex by remember { mutableIntStateOf(0) }
                
                TabRow(
                    selectedTabIndex = selectedColumnIndex,
                    containerColor = SurfaceContainer,
                    contentColor = IndigoAccent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp))
                ) {
                    columns.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedColumnIndex == index,
                            onClick = { selectedColumnIndex = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val columnName = columns[selectedColumnIndex]
                val columnTasks = tasks.filter { it.status == columnName }
                
                ScrumColumn(
                    name = columnName,
                    tasks = columnTasks,
                    onTaskClick = { selectedTask = it },
                    onMoveTask = { taskId, targetStatus ->
                        viewModel.updateTaskStatus(taskId, targetStatus)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Scrum Columns Row (Horizontal Scrolling)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    columns.forEach { columnName ->
                        val columnTasks = tasks.filter { it.status == columnName }
                        ScrumColumn(
                            name = columnName,
                            tasks = columnTasks,
                            onTaskClick = { selectedTask = it },
                            onMoveTask = { taskId, targetStatus ->
                                viewModel.updateTaskStatus(taskId, targetStatus)
                            },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }
        
        // Dialogs
        if (showCreateDialog) {
            CreateTaskDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, desc, priority, date ->
                    viewModel.createTask(title, desc, priority, date)
                    showCreateDialog = false
                }
            )
        }
        
        selectedTask?.let { task ->
            // Reload the task from list to get live status/log changes
            val liveTask = tasks.find { it.id == task.id } ?: task
            val taskLogs = activityLogs.filter { it.taskId == task.id }.sortedByDescending { it.timestamp }
            
            TaskDetailsDialog(
                task = liveTask,
                logs = taskLogs,
                onDismiss = { selectedTask = null },
                onStatusChange = { newStatus ->
                    viewModel.updateTaskStatus(liveTask.id, newStatus)
                },
                onAddComment = { comment ->
                    viewModel.addCommentToTask(liveTask.id, comment)
                },
                onToggleSubTask = { subTaskId, isCompleted ->
                    viewModel.toggleSubTask(liveTask.id, subTaskId, isCompleted)
                },
                onAddSubTask = { title ->
                    viewModel.addSubTask(liveTask.id, title)
                }
            )
        }
    }
}

@Composable
fun ScrumColumn(
    name: String,
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onMoveTask: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnHeaderColor = when (name) {
        "To Do" -> TextSecondary
        "In Progress" -> TealAccent
        "In Review" -> IndigoAccent
        "Done" -> ColorDone
        else -> TextPrimary
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
            .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Column Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(columnHeaderColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tasks.size.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tasks List (Vertical Scrolling inside Column)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task) },
                    onMoveLeft = if (name != "To Do") {
                        val prevStatus = when (name) {
                            "In Progress" -> "To Do"
                            "In Review" -> "In Progress"
                            "Done" -> "In Review"
                            else -> "To Do"
                        }
                        { onMoveTask(task.id, prevStatus) }
                    } else null,
                    onMoveRight = if (name != "Done") {
                        val nextStatus = when (name) {
                            "To Do" -> "In Progress"
                            "In Progress" -> "In Review"
                            "In Review" -> "Done"
                            else -> "Done"
                        }
                        { onMoveTask(task.id, nextStatus) }
                    } else null
                )
            }
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks in this lane",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onMoveLeft: (() -> Unit)?,
    onMoveRight: (() -> Unit)?
) {
    val priorityColor = when (task.priority) {
        "High" -> ColorHigh
        "Medium" -> ColorMedium
        "Low" -> ColorLow
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Task Header (Priority badge and Source brand)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority Label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(priorityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = task.priority,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor
                    )
                }
                
                // Account Sync Source Indicator
                if (task.source != "Local") {
                    val badgeColor = if (task.source == "Gmail") GoogleColor else OutlookColor
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.source,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Description
            Text(
                text = task.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Checklist progress
            if (task.subTasks.isNotEmpty()) {
                val completedCount = task.subTasks.count { it.isCompleted }
                val totalCount = task.subTasks.size
                val progress = completedCount.toFloat() / totalCount.toFloat()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Subtasks progress",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Subtasks",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        
                        Text(
                            text = "$completedCount/$totalCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedCount == totalCount) ColorDone else TealAccent
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (completedCount == totalCount) ColorDone else TealAccent,
                        trackColor = BorderColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer (Due Date and Fast Shifts)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Due Date",
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.dueDate,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                
                // Left & Right Shift controls
                Row {
                    if (onMoveLeft != null) {
                        IconButton(
                            onClick = onMoveLeft,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Move Left",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (onMoveRight != null) {
                        IconButton(
                            onClick = onMoveRight,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Move Right",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    
    val priorities = listOf("Low", "Medium", "High")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainer,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Create Scrum Task",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = IndigoAccent,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = TextSecondary) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = IndigoAccent,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority Selector
                Text("Priority", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { p ->
                        val selected = priority == p
                        val color = when (p) {
                            "High" -> ColorHigh
                            "Medium" -> ColorMedium
                            "Low" -> ColorLow
                            else -> TextSecondary
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) color.copy(alpha = 0.2f) else SurfaceCard)
                                .border(1.dp, if (selected) color else BorderColor, RoundedCornerShape(8.dp))
                                .clickable { priority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) color else TextSecondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Due Date Input
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = IndigoAccent,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (title.isNotBlank()) onCreate(title, description, priority, dueDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskDetailsDialog(
    task: Task,
    logs: List<ActivityLog>,
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit,
    onAddComment: (String) -> Unit,
    onToggleSubTask: (String, Boolean) -> Unit,
    onAddSubTask: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val statuses = listOf("To Do", "In Progress", "In Review", "Done")
    
    val priorityColor = when (task.priority) {
        "High" -> ColorHigh
        "Medium" -> ColorMedium
        "Low" -> ColorLow
        else -> TextSecondary
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainer,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header (Title & Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Synced via: ", fontSize = 11.sp, color = TextMuted)
                            val sourceColor = when (task.source) {
                                "Gmail" -> GoogleColor
                                "Outlook" -> OutlookColor
                                else -> TealAccent
                            }
                            Text(task.source, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = sourceColor)
                        }
                    }
                    
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = IndigoAccent)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Inner content (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Task Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Priority Badge
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PRIORITY", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(priorityColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(task.priority, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = priorityColor)
                            }
                        }
                        // Due Date
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DUE DATE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.dueDate, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status Selector
                    Text("STAGE STATUS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        statuses.forEach { s ->
                            val isSelected = task.status == s
                            val statusColor = when (s) {
                                "To Do" -> TextSecondary
                                "In Progress" -> TealAccent
                                "In Review" -> IndigoAccent
                                "Done" -> ColorDone
                                else -> TextSecondary
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) statusColor.copy(alpha = 0.2f) else SurfaceCard)
                                    .border(1.dp, if (isSelected) statusColor else BorderColor, RoundedCornerShape(6.dp))
                                    .clickable { onStatusChange(s) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) statusColor else TextSecondary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Task Description
                    Text("DESCRIPTION", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (task.description.isNotBlank()) task.description else "No description provided.",
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Sub-tasks / Checklist Section
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Checklist",
                            tint = TealAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CHECKLIST ITEMS / SUB-TASKS",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Add Sub-task row
                    var newSubTaskTitle by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubTaskTitle,
                            onValueChange = { newSubTaskTitle = it },
                            placeholder = { Text("Add checklist item...", fontSize = 12.sp, color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = BorderColor
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newSubTaskTitle.isNotBlank()) {
                                    onAddSubTask(newSubTaskTitle)
                                    newSubTaskTitle = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(46.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Add", fontSize = 12.sp, color = DarkBackground, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // List of sub-tasks
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        task.subTasks.forEach { subTask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCard)
                                    .clickable { onToggleSubTask(subTask.id, !subTask.isCompleted) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subTask.isCompleted,
                                    onCheckedChange = { onToggleSubTask(subTask.id, it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ColorDone,
                                        uncheckedColor = TextSecondary,
                                        checkmarkColor = DarkBackground
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subTask.title,
                                    fontSize = 13.sp,
                                    color = if (subTask.isCompleted) TextMuted else TextPrimary,
                                    style = if (subTask.isCompleted) androidx.compose.ui.text.TextStyle(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    ) else androidx.compose.ui.text.TextStyle.Default
                                )
                            }
                        }
                        if (task.subTasks.isEmpty()) {
                            Text("No checklist items added yet.", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Update/Activity Timeline (Tracking Changes)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = "Activity", tint = IndigoAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ACTIVITY LOG & UPDATE HISTORY", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        logs.forEach { log ->
                            val isComment = log.message.startsWith("Comment:")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isComment) SurfaceCard else Color.Transparent)
                                    .padding(if (isComment) 8.dp else 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = if (isComment) Icons.Default.Comment else Icons.Default.Done,
                                    contentDescription = "Log Type",
                                    tint = if (isComment) TealAccent else TextMuted,
                                    modifier = Modifier.size(12.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = log.message,
                                        fontSize = 12.sp,
                                        color = if (isComment) TextPrimary else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatTime(log.timestamp),
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                        if (logs.isEmpty()) {
                            Text("No history updates recorded.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Add Comment Box at the bottom of the Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Log update or add comment...", fontSize = 12.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = BorderColor
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Log", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
