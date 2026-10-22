package com.example.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun StudentIdeasScreen(
    user: UserAccount,
    problems: List<ProjectProblem>,
    userRequests: List<ProjectRequest>,
    selectedProblemFromOutside: ProjectProblem? = null,
    onSubmitRequest: (
        problem: ProjectProblem,
        teamName: String,
        teamMembers: List<TeamMember>,
        statementOfIntent: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf<ProjectDomain?>(null) }
    var selectedDifficulty by remember { mutableStateOf<DifficultyLevel?>(null) }

    var selectedProblemForDetail by remember { mutableStateOf<ProjectProblem?>(selectedProblemFromOutside) }
    var showRequestDialog by remember { mutableStateOf(false) }

    // When outside problem changes
    LaunchedEffect(selectedProblemFromOutside) {
        if (selectedProblemFromOutside != null) {
            selectedProblemForDetail = selectedProblemFromOutside
        }
    }

    val activeProblems = problems.filter { !it.isArchived }

    val filteredProblems = remember(activeProblems, searchQuery, selectedDomain, selectedDifficulty) {
        activeProblems.filter { prob ->
            val matchesQuery = searchQuery.isBlank() ||
                prob.title.contains(searchQuery, ignoreCase = true) ||
                prob.description.contains(searchQuery, ignoreCase = true) ||
                prob.requiredSkills.any { it.contains(searchQuery, ignoreCase = true) } ||
                prob.staffName.contains(searchQuery, ignoreCase = true)

            val matchesDomain = selectedDomain == null || prob.domain == selectedDomain
            val matchesDifficulty = selectedDifficulty == null || prob.difficulty == selectedDifficulty

            matchesQuery && matchesDomain && matchesDifficulty
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudentMarbleBg)
    ) {
        // Search & Filters Header in Marble Style
        Surface(
            color = SurfaceLight,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudentMarbleVein.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Project Opportunities & Ideas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StudentMarbleDark
                )
                Text(
                    text = "Explore verified mentor problems across cutting-edge technology domains",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by domain, skills (Python, Kotlin...), mentor") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ideas_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Domain Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedDomain == null,
                            onClick = { selectedDomain = null },
                            label = { Text("All Domains", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(ProjectDomain.entries) { domain ->
                        FilterChip(
                            selected = selectedDomain == domain,
                            onClick = { selectedDomain = if (selectedDomain == domain) null else domain },
                            label = { Text(domain.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Difficulty Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedDifficulty == null,
                            onClick = { selectedDifficulty = null },
                            label = { Text("All Difficulties", fontSize = 11.sp) }
                        )
                    }
                    items(DifficultyLevel.entries) { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = if (selectedDifficulty == diff) null else diff },
                            label = { Text(diff.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // List of Problems
        if (filteredProblems.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.SearchOff,
                title = "No Projects Found",
                message = "Try clearing your filters or searching for alternative technical terms.",
                actionButtonText = "Reset All Filters",
                onActionClick = {
                    searchQuery = ""
                    selectedDomain = null
                    selectedDifficulty = null
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProblems) { problem ->
                    val existingRequest = userRequests.find { it.problemId == problem.id && it.studentId == user.id }
                    ProjectItemCard(
                        problem = problem,
                        existingRequest = existingRequest,
                        onViewDetails = { selectedProblemForDetail = problem },
                        onRequest = {
                            selectedProblemForDetail = problem
                            showRequestDialog = true
                        }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet / Dialog
    selectedProblemForDetail?.let { prob ->
        if (!showRequestDialog) {
            val existingRequest = userRequests.find { it.problemId == prob.id && it.studentId == user.id }
            AlertDialog(
                onDismissRequest = { selectedProblemForDetail = null },
                title = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DomainChip(domain = prob.domain)
                            DifficultyBadge(difficulty = prob.difficulty)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = prob.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueVariant
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        if (prob.isSample) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = "Sample Project Problem (Curated realistic demonstration)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "Problem Statement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryBlueVariant
                        )
                        Text(
                            text = prob.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Expected Solution",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryBlueVariant
                        )
                        Text(
                            text = prob.expectedSolution,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Required Skills",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryBlueVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            prob.requiredSkills.forEach { skill ->
                                Surface(
                                    color = SurfaceVariantLight,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = skill,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderStroke)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Staff Mentor", fontSize = 10.sp, color = TextTertiary)
                                Text(prob.staffName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(prob.staffDept, fontSize = 10.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Team Limit", fontSize = 10.sp, color = TextTertiary)
                                Text("Max ${prob.maxTeamSize} students", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Deadline: ${prob.deadline}", fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                            }
                        }

                        if (existingRequest != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = if (existingRequest.status == RequestStatus.APPROVED) SecondaryGreenContainer else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Request Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        RequestStatusBadge(status = existingRequest.status)
                                    }
                                    if (existingRequest.staffNotes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Mentor Note: ${existingRequest.staffNotes}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (existingRequest == null && prob.status == ProjectStatus.AVAILABLE) {
                        Button(
                            onClick = { showRequestDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Form Team & Request", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { selectedProblemForDetail = null },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    if (existingRequest == null) {
                        TextButton(onClick = { selectedProblemForDetail = null }) {
                            Text("Close", color = TextSecondary)
                        }
                    }
                }
            )
        }
    }

    // Submit Project Request Dialog
    if (showRequestDialog && selectedProblemForDetail != null) {
        val targetProblem = selectedProblemForDetail!!
        var teamName by remember { mutableStateOf("${user.name.split(" ").first()}'s Team") }
        var statementOfIntent by remember { mutableStateOf("") }

        // Teammates list
        var teammateName by remember { mutableStateOf("") }
        var teammateEmail by remember { mutableStateOf("") }
        var teammateRole by remember { mutableStateOf("Developer") }
        val teamMembers = remember {
            mutableStateListOf(
                TeamMember(user.name, user.email, "Team Leader")
            )
        }

        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = {
                Text(
                    text = "Request Project Mentorship",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = targetProblem.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Mentor: ${targetProblem.staffName} (${targetProblem.staffDept})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name") },
                        singleLine = true,
                        colors = appTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Team Composition (Max ${targetProblem.maxTeamSize})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )

                    // Current members list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        teamMembers.forEachIndexed { index, member ->
                            Surface(
                                color = SurfaceVariantLight,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(member.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("${member.roleInTeam} • ${member.email}", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    if (index > 0) {
                                        IconButton(
                                            onClick = { teamMembers.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add teammate inline form
                    if (teamMembers.size < targetProblem.maxTeamSize) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = teammateName,
                                onValueChange = { teammateName = it },
                                placeholder = { Text("Peer Name", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = appTextFieldColors(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = teammateEmail,
                                onValueChange = { teammateEmail = it },
                                placeholder = { Text("Peer Email", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = appTextFieldColors(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = {
                                    if (teammateName.isNotBlank() && teammateEmail.contains("@")) {
                                        teamMembers.add(TeamMember(teammateName.trim(), teammateEmail.trim(), teammateRole))
                                        teammateName = ""
                                        teammateEmail = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("+Add", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = statementOfIntent,
                        onValueChange = {
                            statementOfIntent = it
                            formError = null
                        },
                        label = { Text("Statement of Intent / Suitability") },
                        placeholder = { Text("Highlight relevant courses, past projects, or proposed approach...") },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )

                    if (formError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(formError!!, color = Color.Red, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isBlank()) {
                            formError = "Please assign a team name."
                            return@Button
                        }
                        if (statementOfIntent.length < 15) {
                            formError = "Please describe why your team is suitable (min 15 characters)."
                            return@Button
                        }
                        onSubmitRequest(targetProblem, teamName, teamMembers.toList(), statementOfIntent)
                        showRequestDialog = false
                        selectedProblemForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_submit_request_button")
                ) {
                    Text("Submit Request to Mentor", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProjectItemCard(
    problem: ProjectProblem,
    existingRequest: ProjectRequest?,
    onViewDetails: () -> Unit,
    onRequest: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudentMarbleVein.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DomainChip(domain = problem.domain)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DifficultyBadge(difficulty = problem.difficulty)
                    Spacer(modifier = Modifier.width(6.dp))
                    if (problem.isSample) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Sample",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = problem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudentMarbleDark
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = problem.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Required Skills:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                problem.requiredSkills.take(4).forEach { skill ->
                    Surface(
                        color = SurfaceVariantLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = skill,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (problem.requiredSkills.size > 4) {
                    Text(
                        text = "+${problem.requiredSkills.size - 4} more",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderStroke)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mentor: ${problem.staffName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Deadline: ${problem.deadline} • Max ${problem.maxTeamSize} members",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                if (existingRequest != null) {
                    RequestStatusBadge(status = existingRequest.status)
                } else if (problem.status == ProjectStatus.AVAILABLE) {
                    Button(
                        onClick = onRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = StudentBlueAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("request_project_button_${problem.id}")
                    ) {
                        Text("Request", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    StatusBadge(status = problem.status)
                }
            }
        }
    }
}
