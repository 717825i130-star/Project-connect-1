package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun StaffDashboardScreen(
    user: UserAccount,
    problems: List<ProjectProblem>,
    requests: List<ProjectRequest>,
    workspaces: List<ProjectWorkspace>,
    onNavigateToProblems: () -> Unit,
    onNavigateToRequests: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onOpenWorkspace: (String) -> Unit,
    onCreateProblemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myProblems = problems.filter { it.staffId == user.id || it.staffName.equals(user.name, ignoreCase = true) }
    val myPendingRequests = requests.filter { req ->
        req.status == RequestStatus.PENDING &&
        (myProblems.any { it.id == req.problemId } || req.projectTitle.isNotEmpty())
    }
    val myActiveWorkspaces = workspaces.filter { ws ->
        ws.staffId == user.id || ws.staffName.equals(user.name, ignoreCase = true)
    }

    val pendingSubmissionsCount = myActiveWorkspaces.sumOf { ws ->
        ws.submissions.count { it.status == SubmissionStatus.UNDER_REVIEW }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StaffMarbleBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Staff Hero Card in Marble Blue with Mixed Green Swirls
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(StaffMarbleHeroBrush)
                    .border(1.dp, StaffMarbleVein.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .testTag("staff_hero_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "FACULTY PORTAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StaffMarbleVein,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = StaffGreenAccent.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "RESEARCH ADVISOR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "${user.department} • ${user.yearOrTitle}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupervisedUserCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics row with Marble Blue and Green Accents
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricItem(label = "Published", value = myProblems.size.toString(), bgColor = Color.White.copy(alpha = 0.16f), modifier = Modifier.weight(1f))
                        MetricItem(label = "Pending Req", value = myPendingRequests.size.toString(), bgColor = StaffGreenAccent.copy(alpha = 0.35f), modifier = Modifier.weight(1f))
                        MetricItem(label = "Active Teams", value = myActiveWorkspaces.size.toString(), bgColor = Color.White.copy(alpha = 0.16f), modifier = Modifier.weight(1f))
                        MetricItem(label = "Deliverables", value = pendingSubmissionsCount.toString(), bgColor = StaffGreenAccent.copy(alpha = 0.35f), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Action Quick Bar (Marble Blue + Green)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCreateProblemClick,
                    colors = ButtonDefaults.buttonColors(containerColor = StaffMarblePrimary),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StaffMarbleOcean),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("publish_new_problem_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Problem", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onNavigateToRequests,
                    colors = ButtonDefaults.buttonColors(containerColor = StaffGreenAccent),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StaffGreenHighlight),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("review_requests_button")
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Review Requests", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Pending Student Requests Requiring Approval
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StaffGreenAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pending Student Requests (${myPendingRequests.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StaffMarbleDark
                    )
                }
                if (myPendingRequests.isNotEmpty()) {
                    TextButton(onClick = onNavigateToRequests) {
                        Text("View all", color = StaffGreenAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (myPendingRequests.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StaffMarbleVein.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StaffGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = StaffGreenAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("All Requests Handled", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StaffMarbleDark)
                            Text("No student team requests currently waiting for your decision.", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        } else {
            items(myPendingRequests.take(3)) { req ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StaffMarbleVein.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToRequests() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DomainChip(domain = req.domain)
                            MatchScoreBadge(score = req.matchPercentage)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = req.projectTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = StaffMarbleDark
                        )
                        Text(
                            text = "Applicant: ${req.studentName} (${req.studentDept}, ${req.studentYear}) • Team: ${req.teamName}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Active Supervised Projects & Milestone Progress
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = StaffMarbleOcean,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Supervised Teams (${myActiveWorkspaces.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StaffMarbleDark
                    )
                }
                TextButton(onClick = onNavigateToProjects) {
                    Text("See teams", color = StaffGreenAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (myActiveWorkspaces.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StaffMarbleVein.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No active teams currently in progress.", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(onClick = onNavigateToProblems) {
                            Text("Manage Problem Statements", color = StaffMarblePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(myActiveWorkspaces) { ws ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StaffMarbleVein.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ws.projectTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = StaffMarbleDark
                        )
                        Text(
                            text = "Team ${ws.teamName} (Lead: ${ws.teamLeader}) • ${ws.milestones.count { it.isCompleted }}/${ws.milestones.size} Milestones Approved",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { ws.progressPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = StaffGreenAccent,
                            trackColor = StaffGreenContainer.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, bgColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1)
        }
    }
}
