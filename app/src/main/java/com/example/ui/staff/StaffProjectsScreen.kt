package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
fun StaffProjectsScreen(
    user: UserAccount,
    workspaces: List<ProjectWorkspace>,
    onOpenWorkspace: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val myWorkspaces = workspaces.filter { ws ->
        ws.staffId == user.id || ws.staffName.equals(user.name, ignoreCase = true)
    }

    var selectedFilter by remember { mutableStateOf<ProjectStatus?>(null) }

    val filtered = remember(myWorkspaces, selectedFilter) {
        if (selectedFilter == null) myWorkspaces
        else myWorkspaces.filter { it.status == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StaffMarbleBg)
    ) {
        // Header
        Surface(
            color = SurfaceLight,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Supervised Projects & Teams",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StaffMarbleDark
                )
                Text(
                    text = "Track active student research cohorts, proposals, milestone deliveries, and defense readiness",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${myWorkspaces.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == ProjectStatus.IN_PROGRESS,
                        onClick = { selectedFilter = if (selectedFilter == ProjectStatus.IN_PROGRESS) null else ProjectStatus.IN_PROGRESS },
                        label = { Text("In Progress", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == ProjectStatus.PROPOSAL_NEEDED,
                        onClick = { selectedFilter = if (selectedFilter == ProjectStatus.PROPOSAL_NEEDED) null else ProjectStatus.PROPOSAL_NEEDED },
                        label = { Text("Proposal Needed", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == ProjectStatus.COMPLETED,
                        onClick = { selectedFilter = if (selectedFilter == ProjectStatus.COMPLETED) null else ProjectStatus.COMPLETED },
                        label = { Text("Completed", fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Groups,
                title = "No Supervised Projects Found",
                message = "When you approve student requests from the Requests tab, project workspaces will appear here for milestone tracking.",
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
                items(filtered) { ws ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StaffMarbleVein.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenWorkspace(ws.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DomainChip(domain = ws.domain)
                                StatusBadge(status = ws.status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = ws.projectTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StaffMarbleDark
                            )

                            Text(
                                text = "Supervised Team: ${ws.teamName} (Leader: ${ws.teamLeader})",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )

                            // Team Members row
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Members: " + ws.teamMembers.joinToString(", ") { "${it.name} (${it.roleInTeam})" },
                                fontSize = 11.sp,
                                color = TextTertiary,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Proposal status
                            val proposal = ws.proposal
                            Surface(
                                color = if (proposal == null) Color(0xFFFEF3C7)
                                        else if (proposal.status == SubmissionStatus.APPROVED) SecondaryGreenContainer.copy(alpha = 0.5f)
                                        else Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (proposal == null) "Formal Proposal Pending Submission"
                                               else "Proposal: ${proposal.title}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    if (proposal != null) {
                                        SubmissionStatusBadge(status = proposal.status)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Milestones Progress
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Milestones: ${ws.milestones.count { it.isCompleted }} of ${ws.milestones.size} approved",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${ws.progressPercentage}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { ws.progressPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SecondaryGreen,
                                trackColor = SecondaryGreenContainer.copy(alpha = 0.5f)
                            )

                            // Submissions waiting review badge
                            val unreviewedSubmissions = ws.submissions.count { it.status == SubmissionStatus.UNDER_REVIEW }
                            if (unreviewedSubmissions > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$unreviewedSubmissions milestone deliverable waiting for your review",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = BorderStroke)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onOpenWorkspace(ws.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StaffMarblePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("open_staff_workspace_${ws.id}")
                                ) {
                                    Text("Open Project Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
