package com.example.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun StudentHomeScreen(
    user: UserAccount,
    problems: List<ProjectProblem>,
    workspaces: List<ProjectWorkspace>,
    requests: List<ProjectRequest>,
    recommendations: List<MatchRecommendation>,
    onNavigateToIdeas: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onOpenWorkspace: (String) -> Unit,
    onSelectProblem: (ProjectProblem) -> Unit,
    modifier: Modifier = Modifier
) {
    val studentWorkspaces = workspaces.filter { ws ->
        ws.teamLeader.equals(user.name, ignoreCase = true) ||
        ws.teamMembers.any { it.email.equals(user.email, ignoreCase = true) }
    }
    val myPendingRequests = requests.filter { it.studentId == user.id && it.status == RequestStatus.PENDING }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudentMarbleBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Hero Welcome Header in Marble Green with Mixed Blue Swirls
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(StudentMarbleHeroBrush)
                    .border(1.dp, StudentMarbleVein.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .testTag("student_hero_card")
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
                                        text = "STUDENT HUB",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StudentMarbleVein,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = StudentBlueAccent.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "RESEARCH COHORT",
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
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Metric Row with Marble & Mixed Blue accents
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "Active Projects",
                            value = studentWorkspaces.size.toString(),
                            bgColor = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Pending Requests",
                            value = myPendingRequests.size.toString(),
                            bgColor = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "Smart Fit",
                            value = recommendations.count { it.matchPercentage >= 70 }.toString(),
                            bgColor = StudentBlueAccent.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Quick Navigation Shortcuts (Marble Green + Mixed Blue)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    title = "Find Problems",
                    subtitle = "${problems.count { !it.isArchived }} available",
                    icon = Icons.Default.Search,
                    containerBrush = StudentMarbleCardBrush,
                    borderColor = StudentMarbleVein,
                    iconColor = StudentMarblePrimary,
                    iconBgColor = StudentMarbleVein.copy(alpha = 0.6f),
                    onClick = onNavigateToIdeas,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Skill Matches",
                    subtitle = "AI calculated fit",
                    icon = Icons.Default.AutoAwesome,
                    containerBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Color(0xFFEFF6FF), Color(0xFFF0FDF4))
                    ),
                    borderColor = StudentBlueHighlight.copy(alpha = 0.5f),
                    iconColor = StudentBlueAccent,
                    iconBgColor = StudentBlueContainer,
                    onClick = onNavigateToMatches,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Workspaces Section
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
                            .background(StudentMarblePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "My Active Projects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StudentMarbleDark
                    )
                }
                if (studentWorkspaces.isNotEmpty()) {
                    TextButton(onClick = onNavigateToProjects) {
                        Text("View all", color = StudentBlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (studentWorkspaces.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudentMarbleVein.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(StudentMarbleVein.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = StudentMarblePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Active Projects Yet",
                            fontWeight = FontWeight.Bold,
                            color = StudentMarbleDark
                        )
                        Text(
                            text = "Browse problem statements from mentors and submit your proposal to get started.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToIdeas,
                            colors = ButtonDefaults.buttonColors(containerColor = StudentMarblePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Browse Problem Ideas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(studentWorkspaces) { ws ->
                ActiveWorkspaceCard(
                    workspace = ws,
                    onClick = { onOpenWorkspace(ws.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Top Recommended Projects Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = StudentMarblePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recommended For You",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StudentMarbleDark
                    )
                }
                TextButton(onClick = onNavigateToMatches) {
                    Text("See all fit", color = StudentBlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        val topRecommendations = recommendations.take(3)
        if (topRecommendations.isEmpty()) {
            item {
                Text(
                    text = "Add more skills in your profile to unlock tailored recommendations.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(topRecommendations) { rec ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudentMarbleVein.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { onSelectProblem(rec.project) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DomainChip(domain = rec.project.domain)
                            MatchScoreBadge(score = rec.matchPercentage)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = rec.project.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = StudentMarbleDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rec.project.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Match reason explanation in marble green pill with mixed blue accent
                        Surface(
                            color = StudentMarbleCardBrush.let { StudentMarbleVein.copy(alpha = 0.25f) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudentMarbleVein.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = StudentBlueAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = rec.explanation,
                                    fontSize = 11.sp,
                                    color = StudentMarbleDark,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mentor: ${rec.project.staffName}",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { onSelectProblem(rec.project) },
                                colors = ButtonDefaults.buttonColors(containerColor = StudentBlueAccent),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("View & Request", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBadge(
    label: String,
    value: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerBrush: androidx.compose.ui.graphics.Brush,
    borderColor: Color,
    iconColor: Color,
    iconBgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerBrush)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = StudentMarbleDark)
                Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ActiveWorkspaceCard(
    workspace: ProjectWorkspace,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudentMarbleVein.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = workspace.projectTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = StudentMarbleDark
            )
            Text(
                text = "Team: ${workspace.teamName} • Mentor: ${workspace.staffName}",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress (${workspace.milestones.count { it.isCompleted }}/${workspace.milestones.size} milestones)",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${workspace.progressPercentage}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudentMarblePrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { workspace.progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StudentMarblePrimary,
                trackColor = StudentMarbleVein.copy(alpha = 0.5f)
            )
        }
    }
}
