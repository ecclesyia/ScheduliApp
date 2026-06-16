package com.example.scheduliapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.scheduliapp.data.ConnectedAccount
import com.example.scheduliapp.theme.*
import com.example.scheduliapp.ui.main.MainScreenViewModel

@Composable
fun AccountsScreen(
    viewModel: MainScreenViewModel,
    accounts: List<ConnectedAccount>,
    modifier: Modifier = Modifier
) {
    var showConnectDialog by remember { mutableStateOf<String?>(null) } // "Gmail" or "Outlook" or null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header
            Text(
                text = "Integrations Center",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Link your work accounts to sync schedules",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gmail Section
                item {
                    val gmailAccount = accounts.find { it.type == "Gmail" }
                    AccountSyncCard(
                        name = "Google Workspace / Gmail",
                        brandColor = GoogleColor,
                        account = gmailAccount,
                        onConnectClick = { showConnectDialog = "Gmail" },
                        onDisconnectClick = { 
                            gmailAccount?.let { viewModel.disconnectAccount(it.id) }
                        },
                        onSyncClick = { 
                            gmailAccount?.let { viewModel.syncAccount(it.id) }
                        }
                    )
                }
                
                // Outlook Section
                item {
                    val outlookAccount = accounts.find { it.type == "Outlook" }
                    AccountSyncCard(
                        name = "Microsoft 365 / Outlook",
                        brandColor = OutlookColor,
                        account = outlookAccount,
                        onConnectClick = { showConnectDialog = "Outlook" },
                        onDisconnectClick = { 
                            outlookAccount?.let { viewModel.disconnectAccount(it.id) }
                        },
                        onSyncClick = { 
                            outlookAccount?.let { viewModel.syncAccount(it.id) }
                        }
                    )
                }
                
                // Guide section explaining developer setup
                item {
                    DeveloperSetupGuide()
                }
            }
        }
        
        // Sim Connect Dialog
        showConnectDialog?.let { type ->
            ConnectSimulationDialog(
                accountType = type,
                onDismiss = { showConnectDialog = null },
                onConnect = { email ->
                    viewModel.connectAccount(email, type)
                    showConnectDialog = null
                }
            )
        }
    }
}

@Composable
fun AccountSyncCard(
    name: String,
    brandColor: Color,
    account: ConnectedAccount?,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    val isConnected = account != null && account.isConnected
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(brandColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                // Connection Status Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isConnected) ColorDone.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isConnected) "Active" else "Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) ColorDone else TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isConnected && account != null) {
                // Connected Info
                Text(
                    text = "Connected email:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = account.email,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = BorderColor)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sync Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDisconnectClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = ColorHigh)
                    ) {
                        Icon(Icons.Default.LinkOff, contentDescription = "Disconnect")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Disconnect", fontWeight = FontWeight.SemiBold)
                    }
                    
                    Button(
                        onClick = onSyncClick,
                        colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Now", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
            } else {
                // Disconnected description
                Text(
                    text = "Sync meetings, calendar events, and assign automated scrum task due dates to keep your workflow synchronized.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onConnectClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Link, contentDescription = "Link Account", tint = IndigoAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link Simulator", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DeveloperSetupGuide() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = "Code integration info",
                    tint = IndigoAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Live API Integration Guide",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "To move from this simulation mode to a live sync, compile and add OAuth configurations in Android Studio:",
                fontSize = 12.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground)
                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "1. Google Cloud console (Gmail):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealAccent
                    )
                    Text(
                        text = "Create OAuth Client ID (Android platform) and generate SHA-1 hash. Implement via Credential Manager SDK with Calendar scopes.",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "2. Azure Portal / Entra (Outlook):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealAccent
                    )
                    Text(
                        text = "Create App Registration in Microsoft Entra. Setup MSAL configurations in 'app/src/main/res/raw/auth_config.json'. AcquireToken via PublicClientApplication.",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectSimulationDialog(
    accountType: String,
    onDismiss: () -> Unit,
    onConnect: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val brandColor = if (accountType == "Gmail") GoogleColor else OutlookColor

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
                    text = "Link $accountType (Simulated)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "Connecting will pull calendar events and due dates into Scheduli to simulate real sync workflows.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter account email", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = brandColor,
                        unfocusedBorderColor = BorderColor
                    ),
                    singleLine = true,
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
                        onClick = { 
                            if (email.isNotBlank() && email.contains("@")) {
                                onConnect(email)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect", color = TextPrimary)
                    }
                }
            }
        }
    }
}
