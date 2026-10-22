package com.example.ui.staff

import androidx.compose.foundation.background
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
fun StaffProblemsScreen(
    user: UserAccount,
    problems: List<ProjectProblem>,
    onCreateProblem: (
        title: String,
        domain: ProjectDomain,
        description: String,
        expectedSolution: String,
        requiredSkills: List<String>,
        difficulty: DifficultyLevel,
        maxTeamSize: Int,
        deadline: String
    ) -> Unit,
    onToggleArchive: (String) -> Unit,
    showCreateDialogInitially: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(showCreateDialogInitially) }
    var selectedDomainFilter by remember { mutableStateOf<ProjectDomain?>(null) }
    var showArchived by remember { mutableStateOf(false) }

    val myProblems = problems.filter { it.staffId == user.id || it.staffName.equals(user.name, ignoreCase = true) }
    val displayProblems = myProblems.filter { prob ->
        val matchesArchive = if (showArchived) prob.isArchived else !prob.isArchived
        val matchesDomain = selectedDomainFilter == null || prob.domain == selectedDomainFilter
        matchesArchive && matchesDomain
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = StaffMarblePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = StaffGreenHighlight) },
                text = { Text("Publish Problem", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_publish_problem")
            )
        },
        containerColor = StaffMarbleBg,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header & Filter bar
            Surface(
                color = SurfaceLight,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Problem Statements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = StaffMarbleDark
                            )
                            Text(
                                text = "Faculty curated research & capstone challenges",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        // Active vs Archived toggle
                        FilterChip(
                            selected = showArchived,
                            onClick = { showArchived = !showArchived },
                            label = { Text(if (showArchived) "Archived" else "Active", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (showArchived) Icons.Default.Archive else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Domain filter row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDomainFilter == null,
                                onClick = { selectedDomainFilter = null },
                                label = { Text("All Domains", fontSize = 11.sp) }
                            )
                        }
                        items(ProjectDomain.entries) { domain ->
                            FilterChip(
                                selected = selectedDomainFilter == domain,
                                onClick = { selectedDomainFilter = if (selectedDomainFilter == domain) null else domain },
                                label = { Text(domain.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // List of Problems
            if (displayProblems.isEmpty()) {
                EmptyStateView(
                    icon = if (showArchived) Icons.Default.Archive else Icons.Default.PostAdd,
                    title = if (showArchived) "No Archived Problems" else "No Published Problems Found",
                    message = if (showArchived) "Archived problems will be stored safely here."
                              else "Create your first project problem statement to invite student applications.",
                    actionButtonText = if (!showArchived) "Publish New Problem" else null,
                    onActionClick = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayProblems) { prob ->
                        StaffProblemItemCard(
                            problem = prob,
                            onToggleArchive = { onToggleArchive(prob.id) }
                        )
                    }
                }
            }
        }
    }

    // Create Problem Dialog
    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var selectedDomain by remember { mutableStateOf(ProjectDomain.AI_ML) }
        var description by remember { mutableStateOf("") }
        var expectedSolution by remember { mutableStateOf("") }
        var requiredSkillsInput by remember { mutableStateOf("") }
        var difficulty by remember { mutableStateOf(DifficultyLevel.INTERMEDIATE) }
        var maxTeamSize by remember { mutableIntStateOf(3) }
        var deadline by remember { mutableStateOf("Dec 31, 2026") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Publish Project Problem",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Problem Title *") },
                            placeholder = { Text("e.g. Smart Crop Disease Detection") },
                            colors = appTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_problem_title_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Select Domain:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ProjectDomain.entries) { d ->
                                FilterChip(
                                    selected = selectedDomain == d,
                                    onClick = { selectedDomain = d },
                                    label = { Text(d.displayName, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description & Challenge *") },
                            placeholder = { Text("Describe the context, problem statement, and scope...") },
                            colors = appTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .testTag("create_problem_desc_input"),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = expectedSolution,
                            onValueChange = { expectedSolution = it },
                            label = { Text("Expected Solution & Deliverables *") },
                            placeholder = { Text("e.g. Mobile application with offline inference and dashboard...") },
                            colors = appTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = requiredSkillsInput,
                            onValueChange = { requiredSkillsInput = it },
                            label = { Text("Required Skills (comma-separated) *") },
                            placeholder = { Text("Python, TensorFlow, Kotlin, OpenCV") },
                            colors = appTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_problem_skills_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Difficulty Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    DifficultyLevel.entries.forEach { diff ->
                                        FilterChip(
                                            selected = difficulty == diff,
                                            onClick = { difficulty = diff },
                                            label = { Text(diff.label.take(3), fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Max Team Size:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (maxTeamSize > 1) maxTeamSize-- },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null)
                                    }
                                    Text("$maxTeamSize students", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    IconButton(
                                        onClick = { if (maxTeamSize < 6) maxTeamSize++ },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            label = { Text("Target Completion Deadline") },
                            placeholder = { Text("e.g. Jan 15, 2027") },
                            colors = appTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(errorMessage!!, color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Please enter a problem title."
                            return@Button
                        }
                        if (description.length < 20) {
                            errorMessage = "Description must be at least 20 characters."
                            return@Button
                        }
                        if (expectedSolution.isBlank()) {
                            errorMessage = "Please specify expected deliverables."
                            return@Button
                        }
                        val skillsList = requiredSkillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (skillsList.isEmpty()) {
                            errorMessage = "Please provide at least one required skill."
                            return@Button
                        }
                        onCreateProblem(
                            title,
                            selectedDomain,
                            description,
                            expectedSolution,
                            skillsList,
                            difficulty,
                            maxTeamSize,
                            deadline.ifBlank { "TBD" }
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_publish_problem_button")
                ) {
                    Text("Publish to Students", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StaffProblemItemCard(
    problem: ProjectProblem,
    onToggleArchive: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StaffMarbleVein.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
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
                    StatusBadge(status = problem.status)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = problem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StaffMarbleDark
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
                text = "Expected Deliverables: ${problem.expectedSolution}",
                fontSize = 11.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                problem.requiredSkills.forEach { skill ->
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deadline: ${problem.deadline} • Max ${problem.maxTeamSize} per team",
                    fontSize = 11.sp,
                    color = TextTertiary
                )

                OutlinedButton(
                    onClick = onToggleArchive,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (problem.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (problem.isArchived) "Restore" else "Archive",
                        fontSize = 11.sp,
                        color = if (problem.isArchived) SecondaryGreen else TextSecondary
                    )
                }
            }
        }
    }
}
