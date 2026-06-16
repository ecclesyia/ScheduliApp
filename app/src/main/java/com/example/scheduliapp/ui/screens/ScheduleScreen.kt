package com.example.scheduliapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.scheduliapp.data.Meeting
import com.example.scheduliapp.data.Task
import com.example.scheduliapp.theme.*
import com.example.scheduliapp.ui.main.MainScreenViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen(
    viewModel: MainScreenViewModel,
    meetings: List<Meeting>,
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    var selectedDateStr by remember { 
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        mutableStateOf(today) 
    }
    
    var showCreateMeetingDialog by remember { mutableStateOf(false) }

    // Generate dates for the current week (7 days starting from today)
    val weekDates = remember {
        val list = mutableListOf<Pair<String, String>>() // Pair of "Day Name (Mon)", "Date String (2026-06-16)"
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        for (i in 0..6) {
            list.add(Pair(dayFormat.format(cal.time), dateFormat.format(cal.time)))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val selectedDayMeetings = meetings.filter { it.date == selectedDateStr }
    val selectedDayTasks = tasks.filter { it.dueDate == selectedDateStr }

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
                        text = "Calendar Schedule",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Manage meetings & sync due dates",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                
                Button(
                    onClick = { showCreateMeetingDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Meeting", tint = DarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Event", fontWeight = FontWeight.SemiBold, color = DarkBackground)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Week Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekDates.forEach { (dayName, dateStr) ->
                    val isSelected = selectedDateStr == dateStr
                    val dayNum = dateStr.substringAfterLast("-")
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) IndigoAccent else SurfaceContainer)
                            .border(1.dp, if (isSelected) IndigoAccent else BorderColor, RoundedCornerShape(12.dp))
                            .clickable { selectedDateStr = dateStr }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayName.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayNum,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Selected Day Lists
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Synced Meetings
                item {
                    Text(
                        text = "MEETINGS & EVENTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealAccent,
                        letterSpacing = 1.sp
                    )
                }
                
                if (selectedDayMeetings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = TextMuted)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "No meetings scheduled for this day.",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(selectedDayMeetings) { meeting ->
                        MeetingCard(meeting)
                    }
                }
                
                // Section 2: Tasks Due
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SCRUM TASKS DUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoAccent,
                        letterSpacing = 1.sp
                    )
                }
                
                if (selectedDayTasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = TextMuted)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "No Scrum tasks are due on this day.",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(selectedDayTasks) { task ->
                        CalendarTaskCard(task)
                    }
                }
            }
        }
        
        // Dialog to create meeting
        if (showCreateMeetingDialog) {
            CreateMeetingDialog(
                defaultDate = selectedDateStr,
                onDismiss = { showCreateMeetingDialog = false },
                onCreate = { title, desc, date, start, end, location ->
                    viewModel.createMeeting(title, desc, date, start, end, location)
                    showCreateMeetingDialog = false
                }
            )
        }
    }
}

@Composable
fun MeetingCard(meeting: Meeting) {
    val sourceColor = when (meeting.source) {
        "Gmail" -> GoogleColor
        "Outlook" -> OutlookColor
        else -> IndigoAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Source Vertical Line indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(sourceColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Header Row (Time and Source Pill)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${meeting.startTime} - ${meeting.endTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(sourceColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = meeting.source,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = sourceColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Title
                Text(
                    text = meeting.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Description
                if (meeting.description.isNotBlank()) {
                    Text(
                        text = meeting.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Location/Link
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = meeting.location,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarTaskCard(task: Task) {
    val statusColor = when (task.status) {
        "Done" -> ColorDone
        "In Progress" -> TealAccent
        "In Review" -> IndigoAccent
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = "Task Due",
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Priority: ${task.priority}  •  Stage: ${task.status}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            
            // status tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = task.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMeetingDialog(
    defaultDate: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var location by remember { mutableStateOf("Google Meet") }

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
                    text = "Add Calendar Event",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = IndigoAccent,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = IndigoAccent,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = IndigoAccent,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Times
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Meet Link", color = TextSecondary) },
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
                        onClick = { if (title.isNotBlank()) onCreate(title, description, date, startTime, endTime, location) },
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Event", color = DarkBackground)
                    }
                }
            }
        }
    }
}
