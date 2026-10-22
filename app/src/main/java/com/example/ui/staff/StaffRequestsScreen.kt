package com.example.ui.staff

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun StaffRequestsScreen(
    user: UserAccount,
    requests: List<ProjectRequest>,
    problems: List<ProjectProblem>,
    onReviewRequest: (requestId: String, approve: Boolean, staffNotes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<RequestStatus?>(RequestStatus.PENDING) }
    var reviewingRequest by remember { mutableStateOf<ProjectRequest?>(null) }
    var isApprovingDecision by remember { mutableStateOf(true) }

    val myProblems = problems.filter { it.staffId == user.id || it.staffName.equals(user.name, ignoreCase = true) }
    val myRequests = requests.filter { req ->
        myProblems.any { it.id == req.problemId } || req.projectTitle.isNotEmpty()
    }

    val filteredRequests = remember(myRequests, selectedFilter) {
        if (selectedFilter == null) myRequests
        else myRequests.filter { it.status == selectedFilter }
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
                    text = "Student & Team Applications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StaffMarbleDark
                )
                Text(
                    text = "Review incoming team proposals, evaluate skill match metrics, and approve projects",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = selectedFilter == RequestStatus.PENDING,
                        onClick = { selectedFilter = RequestStatus.PENDING },
                        label = { Text("Pending (${myRequests.count { it.status == RequestStatus.PENDING }})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEF3C7),
                            selectedLabelColor = Color(0xFFB45309)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == RequestStatus.APPROVED,
                        onClick = { selectedFilter = RequestStatus.APPROVED },
                        label = { Text("Approved (${myRequests.count { it.status == RequestStatus.APPROVED }})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryGreenContainer,
                            selectedLabelColor = SecondaryGreenVariant
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == RequestStatus.DECLINED,
                        onClick = { selectedFilter = RequestStatus.DECLINED },
                        label = { Text("Declined", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All", fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filteredRequests.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Inbox,
                title = "No Applications in this Category",
                message = "When students discover your published problems and submit team applications, they will appear here for review.",
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
                items(filteredRequests) { request ->
                    StaffRequestCard(
                        request = request,
                        onApprove = {
                            reviewingRequest = request
                            isApprovingDecision = true
                        },
                        onDecline = {
                            reviewingRequest = request
                            isApprovingDecision = false
                        }
                    )
                }
            }
        }
    }

    // Review Decision Dialog with Feedback Notes
    reviewingRequest?.let { req ->
        var staffNotes by remember {
            mutableStateOf(
                if (isApprovingDecision) "Strong team profile. Request approved! Please prepare and submit your project proposal."
                else "Thank you for applying. We are looking for candidates with prior coursework in this specialized domain."
            )
        }

        AlertDialog(
            onDismissRequest = { reviewingRequest = null },
            title = {
                Text(
                    text = if (isApprovingDecision) "Approve Team Application" else "Decline Team Application",
                    fontWeight = FontWeight.Bold,
                    color = if (isApprovingDecision) SecondaryGreenVariant else Color(0xFFDC2626)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Project: ${req.projectTitle}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = PrimaryBlueVariant
                    )
                    Text(
                        text = "Team: ${req.teamName} (Lead: ${req.studentName})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Mentor Feedback / Instructions to Students:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = staffNotes,
                        onValueChange = { staffNotes = it },
                        placeholder = { Text("Enter your guidance, conditional terms, or reason...") },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("staff_review_notes_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReviewRequest(req.id, isApprovingDecision, staffNotes)
                        reviewingRequest = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApprovingDecision) SecondaryGreen else Color(0xFFDC2626)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_review_decision_button")
                ) {
                    Text(
                        text = if (isApprovingDecision) "Confirm Approval" else "Confirm Decline",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewingRequest = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StaffRequestCard(
    request: ProjectRequest,
    onApprove: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StaffMarbleVein.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DomainChip(domain = request.domain)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MatchScoreBadge(score = request.matchPercentage)
                    Spacer(modifier = Modifier.width(6.dp))
                    RequestStatusBadge(status = request.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = request.projectTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StaffMarbleDark
            )

            Text(
                text = "Lead Applicant: ${request.studentName} (${request.studentDept}, ${request.studentYear})",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Team Roster Box
            Surface(
                color = SurfaceVariantLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Team: ${request.teamName} (${request.teamMembers.size} students)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    request.teamMembers.forEach { member ->
                        Text(
                            text = "• ${member.name} (${member.roleInTeam}) - ${member.email}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Statement of intent
            Text(
                text = "Student Statement of Intent:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = request.statementOfIntent,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (request.staffNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Prior Mentor Feedback: ${request.staffNotes}",
                    fontSize = 11.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            if (request.status == RequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderStroke)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("decline_request_button_${request.id}")
                    ) {
                        Text("Decline", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("approve_request_button_${request.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve Team", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
