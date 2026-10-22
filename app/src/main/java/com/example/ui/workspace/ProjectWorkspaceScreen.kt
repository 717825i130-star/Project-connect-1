package com.example.ui.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWorkspaceScreen(
    user: UserAccount,
    workspace: ProjectWorkspace,
    onBack: () -> Unit,
    onSubmitProposal: (
        title: String,
        objective: String,
        methodology: String,
        techStack: List<String>,
        timelineWeeks: Int
    ) -> Unit,
    onReviewProposal: (approved: Boolean, feedback: String) -> Unit,
    onAddMilestone: (title: String, description: String, deadline: String) -> Unit,
    onSubmitMilestoneWork: (
        milestoneId: String,
        documentTitle: String,
        notes: String,
        fileName: String
    ) -> Unit,
    onReviewMilestoneSubmission: (
        submissionId: String,
        approve: Boolean,
        feedback: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val isStaff = user.role == UserRole.STAFF
    var selectedSection by remember { mutableIntStateOf(0) }
    val sectionTabs = listOf("Overview", "Proposal", "Milestones", "Submissions", "Feedback")

    // Dialog states
    var showSubmitProposalDialog by remember { mutableStateOf(false) }
    var showReviewProposalDialog by remember { mutableStateOf(false) }
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var submittingMilestone by remember { mutableStateOf<Milestone?>(null) }
    var reviewingSubmission by remember { mutableStateOf<MilestoneSubmission?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val activeTitleColor = if (isStaff) StaffMarbleDark else StudentMarbleDark
                    val activeIndicatorColor = if (isStaff) StaffMarblePrimary else StudentMarblePrimary
                    Column {
                        Text(
                            text = workspace.projectTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = activeTitleColor
                        )
                        Text(
                            text = "Workspace • Team ${workspace.teamName}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("workspace_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isStaff) StaffMarblePrimary else StudentMarblePrimary
                        )
                    }
                },
                actions = {
                    StatusBadge(
                        status = workspace.status,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = if (isStaff) StaffMarbleBg else StudentMarbleBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val activeTabIndicatorColor = if (isStaff) StaffMarblePrimary else StudentMarblePrimary
            // Workspace Sub-Navigation Tab Bar
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                containerColor = SurfaceLight,
                contentColor = activeTabIndicatorColor,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = activeTabIndicatorColor,
                        height = 3.dp
                    )
                }
            ) {
                sectionTabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontSize = 12.sp,
                                fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedSection) {
                0 -> WorkspaceOverviewSection(
                    workspace = workspace,
                    isStaff = isStaff,
                    onOpenProposal = { selectedSection = 1 },
                    onOpenMilestones = { selectedSection = 2 }
                )
                1 -> WorkspaceProposalSection(
                    proposal = workspace.proposal,
                    isStaff = isStaff,
                    workspaceStatus = workspace.status,
                    onSubmitProposalClick = { showSubmitProposalDialog = true },
                    onReviewProposalClick = { showReviewProposalDialog = true }
                )
                2 -> WorkspaceMilestonesSection(
                    milestones = workspace.milestones,
                    isStaff = isStaff,
                    onAddMilestoneClick = { showAddMilestoneDialog = true },
                    onSubmitMilestoneClick = { ms -> submittingMilestone = ms }
                )
                3 -> WorkspaceSubmissionsSection(
                    submissions = workspace.submissions,
                    isStaff = isStaff,
                    onReviewSubmission = { sub -> reviewingSubmission = sub }
                )
                4 -> WorkspaceFeedbackSection(
                    feedbackList = workspace.feedbackList
                )
            }
        }
    }

    // 1. Submit Proposal Dialog (Student)
    if (showSubmitProposalDialog) {
        var propTitle by remember { mutableStateOf("${workspace.projectTitle} - Technical Architecture") }
        var propObjective by remember { mutableStateOf("") }
        var propMethodology by remember { mutableStateOf("") }
        var propTechStack by remember { mutableStateOf("Kotlin, Python, TensorFlow, Room, REST") }
        var propTimelineWeeks by remember { mutableIntStateOf(8) }
        var propError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSubmitProposalDialog = false },
            title = { Text("Submit Project Proposal", fontWeight = FontWeight.Bold, color = PrimaryBlueVariant) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    item {
                        OutlinedTextField(
                            value = propTitle,
                            onValueChange = { propTitle = it },
                            label = { Text("Proposal Title") },
                            colors = appTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = propObjective,
                            onValueChange = { propObjective = it },
                            label = { Text("Project Objective & Scope") },
                            placeholder = { Text("Explain core problem addressed and metrics of success...") },
                            colors = appTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = propMethodology,
                            onValueChange = { propMethodology = it },
                            label = { Text("Technical Methodology & Architecture") },
                            placeholder = { Text("System components, algorithms, and validation strategy...") },
                            colors = appTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = propTechStack,
                            onValueChange = { propTechStack = it },
                            label = { Text("Tech Stack (comma-separated)") },
                            colors = appTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimated Timeline:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (propTimelineWeeks > 2) propTimelineWeeks -= 2 }) {
                                    Icon(Icons.Default.Remove, contentDescription = null)
                                }
                                Text("$propTimelineWeeks weeks", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                IconButton(onClick = { if (propTimelineWeeks < 24) propTimelineWeeks += 2 }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }

                        if (propError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(propError!!, color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (propObjective.length < 15) {
                            propError = "Please describe the project objective (min 15 chars)."
                            return@Button
                        }
                        if (propMethodology.length < 15) {
                            propError = "Please detail the methodology (min 15 chars)."
                            return@Button
                        }
                        val stack = propTechStack.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onSubmitProposal(propTitle, propObjective, propMethodology, stack, propTimelineWeeks)
                        showSubmitProposalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.testTag("confirm_submit_proposal_button")
                ) {
                    Text("Submit to Faculty Mentor", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitProposalDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // 2. Review Proposal Dialog (Staff)
    if (showReviewProposalDialog && workspace.proposal != null) {
        val prop = workspace.proposal!!
        var staffFeedbackNotes by remember { mutableStateOf("Methodology and timeline align well with project goals. Approved!") }
        var isApproval by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showReviewProposalDialog = false },
            title = {
                Text(
                    text = "Review Team Proposal",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
            },
            text = {
                Column {
                    Text(prop.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Objective: ${prop.objective}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Methodology: ${prop.methodology}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Decision & Guidance to Students:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isApproval,
                            onClick = {
                                isApproval = true
                                staffFeedbackNotes = "Proposal approved without reservations. Proceed to Milestone 1."
                            },
                            label = { Text("Approve Proposal", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryGreenContainer,
                                selectedLabelColor = SecondaryGreenVariant
                            )
                        )
                        FilterChip(
                            selected = !isApproval,
                            onClick = {
                                isApproval = false
                                staffFeedbackNotes = "Please refine methodology and clarify architectural risks before approval."
                            },
                            label = { Text("Request Revisions", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEE2E2),
                                selectedLabelColor = Color(0xFFDC2626)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = staffFeedbackNotes,
                        onValueChange = { staffFeedbackNotes = it },
                        placeholder = { Text("Enter constructive feedback or conditions...") },
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReviewProposal(isApproval, staffFeedbackNotes)
                        showReviewProposalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApproval) SecondaryGreen else Color(0xFFDC2626)
                    ),
                    modifier = Modifier.testTag("confirm_proposal_decision_button")
                ) {
                    Text(if (isApproval) "Confirm Approval" else "Request Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewProposalDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // 3. Add Milestone Dialog (Staff)
    if (showAddMilestoneDialog) {
        var msTitle by remember { mutableStateOf("") }
        var msDesc by remember { mutableStateOf("") }
        var msDeadline by remember { mutableStateOf("In 3 weeks") }

        AlertDialog(
            onDismissRequest = { showAddMilestoneDialog = false },
            title = { Text("Add Project Milestone", fontWeight = FontWeight.Bold, color = PrimaryBlueVariant) },
            text = {
                Column {
                    OutlinedTextField(
                        value = msTitle,
                        onValueChange = { msTitle = it },
                        label = { Text("Milestone Title") },
                        placeholder = { Text("e.g. Phase 2: Algorithm Benchmarking") },
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = msDesc,
                        onValueChange = { msDesc = it },
                        label = { Text("Deliverables & Evaluation Criteria") },
                        placeholder = { Text("Specify what code, dataset, or report must be submitted...") },
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = msDeadline,
                        onValueChange = { msDeadline = it },
                        label = { Text("Due Date / Deadline") },
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (msTitle.isNotBlank()) {
                            onAddMilestone(msTitle, msDesc, msDeadline)
                            showAddMilestoneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Create Milestone", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMilestoneDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // 4. Submit Milestone Work Dialog (Student)
    submittingMilestone?.let { ms ->
        var docTitle by remember { mutableStateOf("${ms.title.take(25)}_Deliverable.pdf") }
        var notes by remember { mutableStateOf("") }
        var simulatedFileName by remember { mutableStateOf("Deliverable_Artifact_${ms.order}.pdf") }

        AlertDialog(
            onDismissRequest = { submittingMilestone = null },
            title = {
                Text("Submit Milestone Deliverable", fontWeight = FontWeight.Bold, color = PrimaryBlueVariant)
            },
            text = {
                Column {
                    Text("Milestone: ${ms.title}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                    Text("Due: ${ms.deadline}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document / Report Title") },
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Summary Notes / Deliverable Link") },
                        placeholder = { Text("Describe methodology, test outputs, or GitHub repository commit...") },
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().height(85.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated File Attachment Card
                    Surface(
                        color = SurfaceVariantLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(simulatedFileName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("PDF Document • 640 KB (Encrypted Upload)", fontSize = 10.sp, color = TextSecondary)
                            }
                            IconButton(onClick = {
                                simulatedFileName = if (simulatedFileName.endsWith(".pdf")) "Deliverable_Code_Package.zip" else "Deliverable_Artifact.pdf"
                            }) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Change file type", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitMilestoneWork(ms.id, docTitle, notes, simulatedFileName)
                        submittingMilestone = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    modifier = Modifier.testTag("confirm_submit_milestone_button")
                ) {
                    Text("Upload & Submit for Review", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { submittingMilestone = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // 5. Review Milestone Submission Dialog (Staff)
    reviewingSubmission?.let { sub ->
        var staffFeedbackNotes by remember { mutableStateOf("Deliverables meet all technical requirements and test coverage metrics.") }
        var isApproval by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { reviewingSubmission = null },
            title = {
                Text(
                    text = "Review Milestone Deliverable",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
            },
            text = {
                Column {
                    Text(sub.milestoneTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                    Text("Document: ${sub.documentTitle}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Submitted by: ${sub.submittedBy} • File: ${sub.fileName}", fontSize = 10.sp, color = TextSecondary)
                    if (sub.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Notes: ${sub.notes}", fontSize = 11.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Mentor Evaluation Decision:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isApproval,
                            onClick = {
                                isApproval = true
                                staffFeedbackNotes = "Deliverables verified. Milestone marked as completed."
                            },
                            label = { Text("Approve Milestone", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryGreenContainer,
                                selectedLabelColor = SecondaryGreenVariant
                            )
                        )
                        FilterChip(
                            selected = !isApproval,
                            onClick = {
                                isApproval = false
                                staffFeedbackNotes = "Please revise the documentation and re-upload with edge-case validation."
                            },
                            label = { Text("Request Revision", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEE2E2),
                                selectedLabelColor = Color(0xFFDC2626)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = staffFeedbackNotes,
                        onValueChange = { staffFeedbackNotes = it },
                        placeholder = { Text("Enter evaluation notes, review comments, or next steps...") },
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReviewMilestoneSubmission(sub.id, isApproval, staffFeedbackNotes)
                        reviewingSubmission = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApproval) SecondaryGreen else Color(0xFFDC2626)
                    ),
                    modifier = Modifier.testTag("confirm_review_milestone_button")
                ) {
                    Text(if (isApproval) "Approve Work" else "Request Revisions", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewingSubmission = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ---------------- TAB 0: OVERVIEW ----------------
@Composable
private fun WorkspaceOverviewSection(
    workspace: ProjectWorkspace,
    isStaff: Boolean,
    onOpenProposal: () -> Unit,
    onOpenMilestones: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overall Progress Card
        item {
            ProjectProgressCard(
                progressPercentage = workspace.progressPercentage,
                status = workspace.status,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mentor & Faculty Information
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Faculty Supervision",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(workspace.staffName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${workspace.staffDept} • Primary Project Guide", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Team Roster
        item {
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
                        Text(
                            text = "Project Team: ${workspace.teamName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueVariant
                        )
                        Surface(
                            color = PrimaryBlueContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${workspace.teamMembers.size} Members",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    workspace.teamMembers.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (member.name == workspace.teamLeader) Icons.Default.Stars else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (member.name == workspace.teamLeader) SecondaryGreen else TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(member.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("${member.roleInTeam} • ${member.email}", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Quick Stage Action Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenProposal,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Proposal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenMilestones,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Milestones (${workspace.milestones.count { it.isCompleted }}/${workspace.milestones.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- TAB 1: PROPOSAL ----------------
@Composable
private fun WorkspaceProposalSection(
    proposal: ProjectProposal?,
    isStaff: Boolean,
    workspaceStatus: ProjectStatus,
    onSubmitProposalClick: () -> Unit,
    onReviewProposalClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (proposal == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Formal Proposal Required", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(
                            text = "Now that the project request is approved, the team must draft and submit their project proposal outlining objectives, methodology, and tech stack.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        if (!isStaff) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onSubmitProposalClick,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("submit_proposal_button")
                            ) {
                                Text("Draft & Submit Proposal", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Awaiting student team submission.",
                                fontSize = 11.sp,
                                color = PrimaryBlueLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = proposal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueVariant,
                                modifier = Modifier.weight(1f)
                            )
                            SubmissionStatusBadge(status = proposal.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Objective & Scope:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(proposal.objective, fontSize = 12.sp, color = TextSecondary, lineHeight = 17.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Technical Methodology:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(proposal.methodology, fontSize = 12.sp, color = TextSecondary, lineHeight = 17.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Tech Stack:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            proposal.techStack.forEach { tech ->
                                Surface(
                                    color = SurfaceVariantLight,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = tech,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Estimated Duration: ${proposal.timelineWeeks} Weeks", fontSize = 11.sp, color = TextTertiary)

                        if (proposal.staffFeedback.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = if (proposal.status == SubmissionStatus.APPROVED) SecondaryGreenContainer.copy(alpha = 0.5f) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Mentor Feedback:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(proposal.staffFeedback, fontSize = 11.sp, color = TextPrimary, lineHeight = 15.sp)
                                }
                            }
                        }

                        if (isStaff && proposal.status == SubmissionStatus.UNDER_REVIEW) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onReviewProposalClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("review_proposal_button")
                            ) {
                                Text("Review & Sign Off Proposal", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else if (!isStaff && proposal.status == SubmissionStatus.NEEDS_REVISION) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onSubmitProposalClick,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit Revised Proposal", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 2: MILESTONES ----------------
@Composable
private fun WorkspaceMilestonesSection(
    milestones: List<Milestone>,
    isStaff: Boolean,
    onAddMilestoneClick: () -> Unit,
    onSubmitMilestoneClick: (Milestone) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Project Milestones (${milestones.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
                if (isStaff) {
                    TextButton(onClick = onAddMilestoneClick) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Milestone", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        items(milestones.sortedBy { it.order }) { ms ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (ms.isCompleted) SecondaryGreen else SurfaceVariantLight),
                                contentAlignment = Alignment.Center
                            ) {
                                if (ms.isCompleted) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("${ms.order}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ms.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueVariant
                            )
                        }
                        SubmissionStatusBadge(status = ms.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(ms.description, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Deadline: ${ms.deadline}", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)

                        if (!isStaff && !ms.isCompleted) {
                            Button(
                                onClick = { onSubmitMilestoneClick(ms) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("submit_milestone_btn_${ms.id}")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (ms.status == SubmissionStatus.NEEDS_REVISION) "Resubmit Work" else "Submit Work", fontSize = 11.sp, color = Color.White)
                            }
                        } else if (ms.isCompleted) {
                            Surface(
                                color = SecondaryGreenContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Completed", color = SecondaryGreenVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 3: SUBMISSIONS ----------------
@Composable
private fun WorkspaceSubmissionsSection(
    submissions: List<MilestoneSubmission>,
    isStaff: Boolean,
    onReviewSubmission: (MilestoneSubmission) -> Unit
) {
    if (submissions.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.CloudUpload,
            title = "No Work Uploaded Yet",
            message = "Uploaded milestone code deliverables, research manuscripts, and test summaries will appear here in chronological order.",
            actionButtonText = null,
            onActionClick = null
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Milestone Document Submissions (${submissions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
            }

            items(submissions) { sub ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sub.milestoneTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                            SubmissionStatusBadge(status = sub.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(sub.documentTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        if (sub.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(sub.notes, fontSize = 11.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // File badge
                        Surface(
                            color = SurfaceVariantLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(sub.fileName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text("${sub.fileSizeBytes / 1024} KB", fontSize = 10.sp, color = TextTertiary)
                            }
                        }

                        if (sub.staffFeedback.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (sub.status == SubmissionStatus.APPROVED) SecondaryGreenContainer.copy(alpha = 0.5f) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Mentor Feedback:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(sub.staffFeedback, fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("By ${sub.submittedBy}", fontSize = 10.sp, color = TextTertiary)
                            if (isStaff && sub.status == SubmissionStatus.UNDER_REVIEW) {
                                Button(
                                    onClick = { onReviewSubmission(sub) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("review_submission_${sub.id}")
                                ) {
                                    Text("Review Deliverable", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 4: FEEDBACK ----------------
@Composable
private fun WorkspaceFeedbackSection(
    feedbackList: List<StaffFeedback>
) {
    if (feedbackList.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.RateReview,
            title = "No Mentor Feedback Yet",
            message = "Mentor comments, approval notes, and technical guidance will appear here.",
            actionButtonText = null,
            onActionClick = null
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Faculty Guidance & Feedback Log (${feedbackList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
            }

            items(feedbackList) { fb ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (fb.actionType == "Approved") SecondaryGreenContainer else Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (fb.actionType == "Approved") Icons.Default.CheckCircle else Icons.Default.Comment,
                                        contentDescription = null,
                                        tint = if (fb.actionType == "Approved") SecondaryGreen else PrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(fb.staffName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(fb.actionType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (fb.actionType == "Approved") SecondaryGreenVariant else PrimaryBlue)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Topic: ${fb.milestoneOrTopic}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlueVariant
                        )
                        Text(
                            text = fb.comment,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
