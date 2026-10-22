package com.example.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun StudentProjectsScreen(
    user: UserAccount,
    workspaces: List<ProjectWorkspace>,
    requests: List<ProjectRequest>,
    onOpenWorkspace: (String) -> Unit,
    onNavigateToIdeas: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active Workspaces", "Track Requests", "Completed Showcase")

    val studentWorkspaces = workspaces.filter { ws ->
        ws.teamLeader.equals(user.name, ignoreCase = true) ||
        ws.teamMembers.any { it.email.equals(user.email, ignoreCase = true) }
    }
    val activeWorkspaces = studentWorkspaces.filter { it.status != ProjectStatus.COMPLETED }
    val completedWorkspaces = workspaces.filter { it.status == ProjectStatus.COMPLETED }

    val myRequests = requests.filter { it.studentId == user.id || it.teamLeader.equals(user.name, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudentMarbleBg)
    ) {
        // Tab Header
        Surface(
            color = SurfaceLight,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "My Projects & Collaborations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StudentMarbleDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Track your project proposals, milestones, documents, and mentor sign-offs",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceLight,
                    contentColor = StudentMarblePrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = StudentMarblePrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Active Workspaces
                if (activeWorkspaces.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.FolderSpecial,
                        title = "No Active Project Workspace Yet",
                        message = "Once faculty approves your project request, a dedicated workspace will unlock with milestones, document uploads, and proposal reviews.",
                        actionButtonText = "Browse Project Ideas",
                        onActionClick = onNavigateToIdeas
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeWorkspaces) { ws ->
                            WorkspaceItemCard(
                                workspace = ws,
                                onOpen = { onOpenWorkspace(ws.id) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Track Requests
                if (myRequests.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.HourglassEmpty,
                        title = "No Project Requests Submitted",
                        message = "You haven't requested any faculty projects yet. Browse available topics and apply with your team.",
                        actionButtonText = "Find Topics to Request",
                        onActionClick = onNavigateToIdeas
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myRequests) { req ->
                            RequestTrackerCard(request = req)
                        }
                    }
                }
            }
            2 -> {
                // Completed Showcase
                if (completedWorkspaces.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.MilitaryTech,
                        title = "Completed Projects Archive",
                        message = "Completed capstone projects that have met all faculty milestone criteria appear here.",
                        actionButtonText = null,
                        onActionClick = null
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(completedWorkspaces) { ws ->
                            WorkspaceItemCard(
                                workspace = ws,
                                onOpen = { onOpenWorkspace(ws.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceItemCard(
    workspace: ProjectWorkspace,
    onOpen: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudentMarbleVein.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DomainChip(domain = workspace.domain)
                StatusBadge(status = workspace.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = workspace.projectTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudentMarbleDark
            )

            Text(
                text = "Team: ${workspace.teamName} (${workspace.teamMembers.size} members) • Mentor: ${workspace.staffName}",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Milestones Completed: ${workspace.milestones.count { it.isCompleted }} of ${workspace.milestones.size}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${workspace.progressPercentage}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGreen
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { workspace.progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SecondaryGreen,
                trackColor = SecondaryGreenContainer.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderStroke)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submissions: ${workspace.submissions.size} • Feedback: ${workspace.feedbackList.size}",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = StudentBlueAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Open Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RequestTrackerCard(
    request: ProjectRequest
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DomainChip(domain = request.domain)
                RequestStatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = request.projectTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlueVariant
            )

            Text(
                text = "Team: ${request.teamName} (Leader: ${request.teamLeader})",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = SurfaceVariantLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Statement of Intent:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = request.statementOfIntent,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            if (request.staffNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (request.status == RequestStatus.APPROVED) SecondaryGreenContainer.copy(alpha = 0.5f) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (request.status == RequestStatus.APPROVED) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (request.status == RequestStatus.APPROVED) SecondaryGreen else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Faculty Mentor Response:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (request.status == RequestStatus.APPROVED) SecondaryGreenVariant else Color.Red
                            )
                            Text(
                                text = request.staffNotes,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
