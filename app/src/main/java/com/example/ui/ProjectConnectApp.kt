package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.data.repository.ProjectConnectRepository
import com.example.ui.auth.LoginScreen
import com.example.ui.staff.*
import com.example.ui.student.*
import com.example.ui.theme.*
import com.example.ui.workspace.ProjectWorkspaceScreen

enum class StudentTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    IDEAS("Ideas", Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb),
    MATCHES("Matches", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    MY_PROJECTS("My Projects", Icons.Filled.Folder, Icons.Outlined.Folder),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

enum class StaffTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    MY_PROBLEMS("My Problems", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    REQUESTS("Requests", Icons.Filled.People, Icons.Outlined.People),
    PROJECTS("Projects", Icons.Filled.Work, Icons.Outlined.Work),
    PROFILE("Profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
}

@Composable
fun ProjectConnectApp(
    repository: ProjectConnectRepository,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsStateWithLifecycle()
    val problems by repository.problems.collectAsStateWithLifecycle()
    val requests by repository.requests.collectAsStateWithLifecycle()
    val workspaces by repository.workspaces.collectAsStateWithLifecycle()

    // Navigation states
    var currentStudentTab by remember { mutableStateOf(StudentTab.HOME) }
    var currentStaffTab by remember { mutableStateOf(StaffTab.DASHBOARD) }
    var activeWorkspaceId by remember { mutableStateOf<String?>(null) }
    var selectedProblemForDetail by remember { mutableStateOf<ProjectProblem?>(null) }

    val activeWorkspace = remember(activeWorkspaceId, workspaces) {
        workspaces.find { it.id == activeWorkspaceId }
    }

    // If no user is logged in, show Login Screen
    if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = {
                currentStudentTab = StudentTab.HOME
                currentStaffTab = StaffTab.DASHBOARD
                activeWorkspaceId = null
            },
            onDemoLogin = { role ->
                repository.quickDemoLogin(role)
                currentStudentTab = StudentTab.HOME
                currentStaffTab = StaffTab.DASHBOARD
                activeWorkspaceId = null
            },
            onRegister = { name, email, role, department, yearOrTitle, skills, interests ->
                repository.register(name, email, role, department, yearOrTitle, skills, interests)
            },
            onPerformLogin = { email, role ->
                repository.login(email, role)
            },
            modifier = modifier
        )
        return
    }

    val user = currentUser!!

    // Active Project Workspace view
    if (activeWorkspace != null) {
        BackHandler {
            activeWorkspaceId = null
        }
        ProjectWorkspaceScreen(
            user = user,
            workspace = activeWorkspace,
            onBack = { activeWorkspaceId = null },
            onSubmitProposal = { title, obj, meth, stack, weeks ->
                repository.submitProposal(activeWorkspace.id, title, obj, meth, stack, weeks)
            },
            onReviewProposal = { approved, feedback ->
                repository.reviewProposal(activeWorkspace.id, approved, feedback)
            },
            onAddMilestone = { title, desc, deadline ->
                repository.addMilestone(activeWorkspace.id, title, desc, deadline)
            },
            onSubmitMilestoneWork = { msId, docTitle, notes, fileName ->
                repository.submitMilestoneWork(activeWorkspace.id, msId, docTitle, notes, fileName)
            },
            onReviewMilestoneSubmission = { subId, approved, feedback ->
                repository.reviewMilestoneSubmission(activeWorkspace.id, subId, approved, feedback)
            },
            modifier = modifier
        )
        return
    }

    // Handle back button on secondary tabs
    BackHandler(enabled = (user.role == UserRole.STUDENT && currentStudentTab != StudentTab.HOME) ||
                          (user.role == UserRole.STAFF && currentStaffTab != StaffTab.DASHBOARD)) {
        if (user.role == UserRole.STUDENT) {
            currentStudentTab = StudentTab.HOME
        } else {
            currentStaffTab = StaffTab.DASHBOARD
        }
    }

    // Role-Based Screen Shell with M3 NavigationBar
    Scaffold(
        bottomBar = {
            if (user.role == UserRole.STUDENT) {
                NavigationBar(
                    containerColor = StudentMarbleBg,
                    tonalElevation = 4.dp
                ) {
                    StudentTab.entries.forEach { tab ->
                        val isSelected = currentStudentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentStudentTab = tab
                                selectedProblemForDetail = null
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StudentMarblePrimary,
                                selectedTextColor = StudentMarblePrimary,
                                indicatorColor = StudentMarbleVein.copy(alpha = 0.65f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_student_${tab.name.lowercase()}")
                        )
                    }
                }
            } else {
                NavigationBar(
                    containerColor = StaffMarbleBg,
                    tonalElevation = 4.dp
                ) {
                    StaffTab.entries.forEach { tab ->
                        val isSelected = currentStaffTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentStaffTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StaffMarblePrimary,
                                selectedTextColor = StaffMarblePrimary,
                                indicatorColor = StaffGreenContainer.copy(alpha = 0.8f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_staff_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        containerColor = if (user.role == UserRole.STUDENT) StudentMarbleBg else StaffMarbleBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (user.role == UserRole.STUDENT) {
            val recommendations = remember(user, problems) {
                repository.getRecommendationsForStudent(user)
            }

            when (currentStudentTab) {
                StudentTab.HOME -> StudentHomeScreen(
                    user = user,
                    problems = problems,
                    workspaces = workspaces,
                    requests = requests,
                    recommendations = recommendations,
                    onNavigateToIdeas = { currentStudentTab = StudentTab.IDEAS },
                    onNavigateToMatches = { currentStudentTab = StudentTab.MATCHES },
                    onNavigateToProjects = { currentStudentTab = StudentTab.MY_PROJECTS },
                    onOpenWorkspace = { activeWorkspaceId = it },
                    onSelectProblem = {
                        selectedProblemForDetail = it
                        currentStudentTab = StudentTab.IDEAS
                    },
                    modifier = Modifier.padding(innerPadding)
                )
                StudentTab.IDEAS -> StudentIdeasScreen(
                    user = user,
                    problems = problems,
                    userRequests = requests,
                    selectedProblemFromOutside = selectedProblemForDetail,
                    onSubmitRequest = { problem, teamName, teamMembers, statement ->
                        repository.submitProjectRequest(problem, teamName, teamMembers, statement)
                        currentStudentTab = StudentTab.MY_PROJECTS
                    },
                    modifier = Modifier.padding(innerPadding)
                )
                StudentTab.MATCHES -> StudentMatchesScreen(
                    user = user,
                    recommendations = recommendations,
                    onSelectProblem = {
                        selectedProblemForDetail = it
                        currentStudentTab = StudentTab.IDEAS
                    },
                    onNavigateToProfile = { currentStudentTab = StudentTab.PROFILE },
                    modifier = Modifier.padding(innerPadding)
                )
                StudentTab.MY_PROJECTS -> StudentProjectsScreen(
                    user = user,
                    workspaces = workspaces,
                    requests = requests,
                    onOpenWorkspace = { activeWorkspaceId = it },
                    onNavigateToIdeas = { currentStudentTab = StudentTab.IDEAS },
                    modifier = Modifier.padding(innerPadding)
                )
                StudentTab.PROFILE -> StudentProfileScreen(
                    user = user,
                    onUpdateProfile = { name, dept, year, skills, interests, bio ->
                        repository.updateProfile(name, dept, year, skills, interests, bio)
                    },
                    onSignOut = {
                        repository.logout()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        } else {
            // STAFF ROLE
            when (currentStaffTab) {
                StaffTab.DASHBOARD -> StaffDashboardScreen(
                    user = user,
                    problems = problems,
                    requests = requests,
                    workspaces = workspaces,
                    onNavigateToProblems = { currentStaffTab = StaffTab.MY_PROBLEMS },
                    onNavigateToRequests = { currentStaffTab = StaffTab.REQUESTS },
                    onNavigateToProjects = { currentStaffTab = StaffTab.PROJECTS },
                    onOpenWorkspace = { activeWorkspaceId = it },
                    onCreateProblemClick = { currentStaffTab = StaffTab.MY_PROBLEMS },
                    modifier = Modifier.padding(innerPadding)
                )
                StaffTab.MY_PROBLEMS -> StaffProblemsScreen(
                    user = user,
                    problems = problems,
                    onCreateProblem = { title, domain, desc, expected, skills, diff, maxTeam, deadline ->
                        repository.createProblem(title, domain, desc, expected, skills, diff, maxTeam, deadline)
                    },
                    onToggleArchive = { repository.toggleArchiveProblem(it) },
                    modifier = Modifier.padding(innerPadding)
                )
                StaffTab.REQUESTS -> StaffRequestsScreen(
                    user = user,
                    requests = requests,
                    problems = problems,
                    onReviewRequest = { reqId, approve, notes ->
                        repository.reviewRequest(reqId, approve, notes)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
                StaffTab.PROJECTS -> StaffProjectsScreen(
                    user = user,
                    workspaces = workspaces,
                    onOpenWorkspace = { activeWorkspaceId = it },
                    modifier = Modifier.padding(innerPadding)
                )
                StaffTab.PROFILE -> StaffProfileScreen(
                    user = user,
                    onUpdateProfile = { name, dept, title, skills, interests, bio ->
                        repository.updateProfile(name, dept, title, skills, interests, bio)
                    },
                    onSignOut = {
                        repository.logout()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
