package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ProjectConnectRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("project_connect_store_v1", Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // State flows
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _problems = MutableStateFlow<List<ProjectProblem>>(emptyList())
    val problems: StateFlow<List<ProjectProblem>> = _problems.asStateFlow()

    private val _requests = MutableStateFlow<List<ProjectRequest>>(emptyList())
    val requests: StateFlow<List<ProjectRequest>> = _requests.asStateFlow()

    private val _workspaces = MutableStateFlow<List<ProjectWorkspace>>(emptyList())
    val workspaces: StateFlow<List<ProjectWorkspace>> = _workspaces.asStateFlow()

    private val _registeredUsers = MutableStateFlow<List<UserAccount>>(emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        // Load registered users
        val usersJson = prefs.getString(KEY_USERS, null)
        val userListType = Types.newParameterizedType(List::class.java, UserAccount::class.java)
        val usersAdapter = moshi.adapter<List<UserAccount>>(userListType)
        val loadedUsers: List<UserAccount>? = if (!usersJson.isNullOrEmpty()) {
            try { usersAdapter.fromJson(usersJson) } catch (e: Exception) { null }
        } else null

        val defaultUsers = loadedUsers ?: createDefaultUsers()
        _registeredUsers.value = defaultUsers

        // Load logged in user
        val currentUserId = prefs.getString(KEY_CURRENT_USER_ID, null)
        if (currentUserId != null) {
            _currentUser.value = defaultUsers.find { it.id == currentUserId }
        }

        // Load problems
        val problemsJson = prefs.getString(KEY_PROBLEMS, null)
        val problemsType = Types.newParameterizedType(List::class.java, ProjectProblem::class.java)
        val problemsAdapter = moshi.adapter<List<ProjectProblem>>(problemsType)
        val loadedProblems: List<ProjectProblem>? = if (!problemsJson.isNullOrEmpty()) {
            try { problemsAdapter.fromJson(problemsJson) } catch (e: Exception) { null }
        } else null
        _problems.value = loadedProblems ?: createInitialSampleProblems()

        // Load requests
        val requestsJson = prefs.getString(KEY_REQUESTS, null)
        val requestsType = Types.newParameterizedType(List::class.java, ProjectRequest::class.java)
        val requestsAdapter = moshi.adapter<List<ProjectRequest>>(requestsType)
        val loadedRequests: List<ProjectRequest>? = if (!requestsJson.isNullOrEmpty()) {
            try { requestsAdapter.fromJson(requestsJson) } catch (e: Exception) { null }
        } else null
        _requests.value = loadedRequests ?: createInitialSampleRequests()

        // Load workspaces
        val workspacesJson = prefs.getString(KEY_WORKSPACES, null)
        val workspacesType = Types.newParameterizedType(List::class.java, ProjectWorkspace::class.java)
        val workspacesAdapter = moshi.adapter<List<ProjectWorkspace>>(workspacesType)
        val loadedWorkspaces: List<ProjectWorkspace>? = if (!workspacesJson.isNullOrEmpty()) {
            try { workspacesAdapter.fromJson(workspacesJson) } catch (e: Exception) { null }
        } else null
        _workspaces.value = loadedWorkspaces ?: createInitialSampleWorkspaces()

        persistAll()
    }

    private fun persistAll() {
        val editor = prefs.edit()
        try {
            val userListType = Types.newParameterizedType(List::class.java, UserAccount::class.java)
            editor.putString(KEY_USERS, moshi.adapter<List<UserAccount>>(userListType).toJson(_registeredUsers.value))

            val problemsType = Types.newParameterizedType(List::class.java, ProjectProblem::class.java)
            editor.putString(KEY_PROBLEMS, moshi.adapter<List<ProjectProblem>>(problemsType).toJson(_problems.value))

            val requestsType = Types.newParameterizedType(List::class.java, ProjectRequest::class.java)
            editor.putString(KEY_REQUESTS, moshi.adapter<List<ProjectRequest>>(requestsType).toJson(_requests.value))

            val workspacesType = Types.newParameterizedType(List::class.java, ProjectWorkspace::class.java)
            editor.putString(KEY_WORKSPACES, moshi.adapter<List<ProjectWorkspace>>(workspacesType).toJson(_workspaces.value))

            if (_currentUser.value != null) {
                editor.putString(KEY_CURRENT_USER_ID, _currentUser.value!!.id)
            } else {
                editor.remove(KEY_CURRENT_USER_ID)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        editor.apply()
    }

    // AUTHENTICATION
    fun login(email: String, role: UserRole): Result<UserAccount> {
        val normalized = email.trim().lowercase()
        val user = _registeredUsers.value.find { it.email.lowercase() == normalized && it.role == role }
        return if (user != null) {
            _currentUser.value = user
            persistAll()
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("No account found for $email with role ${role.name}. Please check credentials or register."))
        }
    }

    fun quickDemoLogin(role: UserRole): UserAccount {
        val targetEmail = if (role == UserRole.STUDENT) "alex.student@campus.edu" else "dr.sharma@campus.edu"
        val user = _registeredUsers.value.firstOrNull { it.email.equals(targetEmail, ignoreCase = true) }
            ?: if (role == UserRole.STUDENT) defaultStudent else defaultStaff
        _currentUser.value = user
        persistAll()
        return user
    }

    fun register(
        name: String,
        email: String,
        role: UserRole,
        department: String,
        yearOrTitle: String,
        skillsOrExpertise: List<String>,
        interests: List<String>
    ): Result<UserAccount> {
        val normalizedEmail = email.trim().lowercase()
        if (_registeredUsers.value.any { it.email.lowercase() == normalizedEmail }) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }
        val newUser = UserAccount(
            id = "user_" + UUID.randomUUID().toString().take(8),
            email = normalizedEmail,
            role = role,
            name = name.trim(),
            department = department.trim(),
            yearOrTitle = yearOrTitle.trim(),
            skills = skillsOrExpertise,
            interests = interests,
            bio = if (role == UserRole.STUDENT) "Student enthusiastic about applied research and practical team projects."
                  else "Faculty mentor guiding innovative student capstone and research initiatives."
        )
        val updated = _registeredUsers.value + newUser
        _registeredUsers.value = updated
        _currentUser.value = newUser
        persistAll()
        return Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
        persistAll()
    }

    fun updateProfile(
        name: String,
        department: String,
        yearOrTitle: String,
        skills: List<String>,
        interests: List<String>,
        bio: String
    ) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            name = name.trim(),
            department = department.trim(),
            yearOrTitle = yearOrTitle.trim(),
            skills = skills,
            interests = interests,
            bio = bio.trim()
        )
        _currentUser.value = updated
        _registeredUsers.value = _registeredUsers.value.map { if (it.id == updated.id) updated else it }
        persistAll()
    }

    // PROBLEMS (Staff)
    fun createProblem(
        title: String,
        domain: ProjectDomain,
        description: String,
        expectedSolution: String,
        requiredSkills: List<String>,
        difficulty: DifficultyLevel,
        maxTeamSize: Int,
        deadline: String
    ): ProjectProblem {
        val staff = _currentUser.value ?: defaultStaff
        val newProblem = ProjectProblem(
            id = "prob_" + UUID.randomUUID().toString().take(8),
            title = title.trim(),
            domain = domain,
            description = description.trim(),
            expectedSolution = expectedSolution.trim(),
            requiredSkills = requiredSkills,
            difficulty = difficulty,
            maxTeamSize = maxTeamSize.coerceIn(1, 6),
            deadline = deadline.trim(),
            staffId = staff.id,
            staffName = staff.name,
            staffDept = staff.department,
            status = ProjectStatus.AVAILABLE,
            createdAt = System.currentTimeMillis(),
            isSample = false
        )
        _problems.value = listOf(newProblem) + _problems.value
        persistAll()
        return newProblem
    }

    fun updateProblem(updated: ProjectProblem) {
        _problems.value = _problems.value.map { if (it.id == updated.id) updated else it }
        persistAll()
    }

    fun toggleArchiveProblem(problemId: String) {
        _problems.value = _problems.value.map {
            if (it.id == problemId) it.copy(isArchived = !it.isArchived) else it
        }
        persistAll()
    }

    // REQUESTS (Student -> Staff)
    fun submitProjectRequest(
        problem: ProjectProblem,
        teamName: String,
        teamMembers: List<TeamMember>,
        statementOfIntent: String
    ): ProjectRequest {
        val student = _currentUser.value ?: defaultStudent
        val matchResult = calculateMatch(student, problem)
        val newRequest = ProjectRequest(
            id = "req_" + UUID.randomUUID().toString().take(8),
            problemId = problem.id,
            projectTitle = problem.title,
            domain = problem.domain,
            studentId = student.id,
            studentName = student.name,
            studentDept = student.department,
            studentYear = student.yearOrTitle,
            teamName = teamName.ifBlank { "${student.name.split(" ").first()}'s Team" },
            teamLeader = student.name,
            teamMembers = teamMembers.ifEmpty {
                listOf(TeamMember(student.name, student.email, "Team Leader"))
            },
            matchPercentage = matchResult.matchPercentage,
            statementOfIntent = statementOfIntent.trim(),
            status = RequestStatus.PENDING,
            requestedAt = System.currentTimeMillis()
        )

        // Update problem status if needed
        _problems.value = _problems.value.map {
            if (it.id == problem.id) it.copy(status = ProjectStatus.REQUEST_PENDING) else it
        }
        _requests.value = listOf(newRequest) + _requests.value
        persistAll()
        return newRequest
    }

    fun reviewRequest(requestId: String, approve: Boolean, staffNotes: String) {
        val request = _requests.value.find { it.id == requestId } ?: return
        val newStatus = if (approve) RequestStatus.APPROVED else RequestStatus.DECLINED
        val updatedRequest = request.copy(status = newStatus, staffNotes = staffNotes.trim())
        _requests.value = _requests.value.map { if (it.id == requestId) updatedRequest else it }

        if (approve) {
            // Update problem status
            _problems.value = _problems.value.map {
                if (it.id == request.problemId) it.copy(status = ProjectStatus.PROPOSAL_NEEDED) else it
            }
            // Create or unlock Project Workspace
            val problem = _problems.value.find { it.id == request.problemId }
            val existingWorkspace = _workspaces.value.find { it.problemId == request.problemId }
            if (existingWorkspace == null && problem != null) {
                val newWorkspace = ProjectWorkspace(
                    id = "ws_" + UUID.randomUUID().toString().take(8),
                    problemId = problem.id,
                    projectTitle = problem.title,
                    domain = problem.domain,
                    staffId = problem.staffId,
                    staffName = problem.staffName,
                    staffDept = problem.staffDept,
                    teamName = request.teamName,
                    teamLeader = request.teamLeader,
                    teamMembers = request.teamMembers,
                    proposal = null,
                    milestones = generateDefaultMilestones(problem.id),
                    submissions = emptyList(),
                    feedbackList = listOf(
                        StaffFeedback(
                            id = "fb_" + UUID.randomUUID().toString().take(6),
                            projectId = problem.id,
                            staffName = problem.staffName,
                            milestoneOrTopic = "Project Request Approved",
                            comment = if (staffNotes.isNotBlank()) staffNotes else "Your request to work on ${problem.title} has been accepted! Please prepare and submit your team's project proposal.",
                            actionType = "Approved",
                            timestamp = System.currentTimeMillis()
                        )
                    ),
                    status = ProjectStatus.PROPOSAL_NEEDED
                )
                _workspaces.value = listOf(newWorkspace) + _workspaces.value
            }
        }
        persistAll()
    }

    // PROPOSALS (Student -> Staff)
    fun submitProposal(
        workspaceId: String,
        title: String,
        objective: String,
        methodology: String,
        techStack: List<String>,
        timelineWeeks: Int
    ) {
        val ws = _workspaces.value.find { it.id == workspaceId } ?: return
        val proposal = ProjectProposal(
            id = "prop_" + UUID.randomUUID().toString().take(8),
            problemId = ws.problemId,
            title = title.trim(),
            objective = objective.trim(),
            methodology = methodology.trim(),
            techStack = techStack,
            timelineWeeks = timelineWeeks,
            status = SubmissionStatus.UNDER_REVIEW,
            submittedAt = System.currentTimeMillis()
        )
        val updatedWorkspace = ws.copy(
            proposal = proposal,
            status = ProjectStatus.IN_PROGRESS
        )
        _workspaces.value = _workspaces.value.map { if (it.id == workspaceId) updatedWorkspace else it }
        _problems.value = _problems.value.map {
            if (it.id == ws.problemId) it.copy(status = ProjectStatus.IN_PROGRESS) else it
        }
        persistAll()
    }

    fun reviewProposal(workspaceId: String, approved: Boolean, feedback: String) {
        val ws = _workspaces.value.find { it.id == workspaceId } ?: return
        val proposal = ws.proposal ?: return
        val updatedProposal = proposal.copy(
            status = if (approved) SubmissionStatus.APPROVED else SubmissionStatus.NEEDS_REVISION,
            staffFeedback = feedback
        )
        val newStatus = if (approved) ProjectStatus.IN_PROGRESS else ProjectStatus.NEEDS_REVISION
        val newFeedback = StaffFeedback(
            id = "fb_" + UUID.randomUUID().toString().take(6),
            projectId = ws.problemId,
            staffName = ws.staffName,
            milestoneOrTopic = "Proposal Review",
            comment = feedback.ifBlank { if (approved) "Proposal approved without reservations." else "Please refine methodology and tech stack." },
            actionType = if (approved) "Approved" else "Revision Requested",
            timestamp = System.currentTimeMillis()
        )
        val updatedWorkspace = ws.copy(
            proposal = updatedProposal,
            status = newStatus,
            feedbackList = listOf(newFeedback) + ws.feedbackList
        )
        _workspaces.value = _workspaces.value.map { if (it.id == workspaceId) updatedWorkspace else it }
        _problems.value = _problems.value.map {
            if (it.id == ws.problemId) it.copy(status = newStatus) else it
        }
        persistAll()
    }

    // MILESTONES & SUBMISSIONS
    fun addMilestone(workspaceId: String, title: String, description: String, deadline: String) {
        val ws = _workspaces.value.find { it.id == workspaceId } ?: return
        val nextOrder = (ws.milestones.maxOfOrNull { it.order } ?: 0) + 1
        val newMilestone = Milestone(
            id = "ms_" + UUID.randomUUID().toString().take(8),
            projectId = ws.problemId,
            title = title.trim(),
            description = description.trim(),
            deadline = deadline.trim(),
            order = nextOrder,
            isCompleted = false,
            status = SubmissionStatus.UNDER_REVIEW
        )
        val updated = ws.copy(milestones = ws.milestones + newMilestone)
        _workspaces.value = _workspaces.value.map { if (it.id == workspaceId) updated else it }
        persistAll()
    }

    fun submitMilestoneWork(
        workspaceId: String,
        milestoneId: String,
        documentTitle: String,
        notes: String,
        fileName: String,
        fileSizeBytes: Long = 1024 * 450
    ) {
        val ws = _workspaces.value.find { it.id == workspaceId } ?: return
        val milestone = ws.milestones.find { it.id == milestoneId } ?: return
        val user = _currentUser.value ?: defaultStudent

        val newSubmission = MilestoneSubmission(
            id = "sub_" + UUID.randomUUID().toString().take(8),
            milestoneId = milestoneId,
            milestoneTitle = milestone.title,
            documentTitle = documentTitle.trim(),
            notes = notes.trim(),
            fileName = fileName.trim().ifBlank { "Project_Artifact_${milestone.order}.pdf" },
            fileSizeBytes = fileSizeBytes,
            submittedBy = user.name,
            submittedAt = System.currentTimeMillis(),
            status = SubmissionStatus.UNDER_REVIEW
        )

        val updatedMilestones = ws.milestones.map {
            if (it.id == milestoneId) it.copy(status = SubmissionStatus.UNDER_REVIEW) else it
        }

        val updatedWorkspace = ws.copy(
            milestones = updatedMilestones,
            submissions = listOf(newSubmission) + ws.submissions,
            status = ProjectStatus.IN_PROGRESS
        )
        _workspaces.value = _workspaces.value.map { if (it.id == workspaceId) updatedWorkspace else it }
        persistAll()
    }

    fun reviewMilestoneSubmission(
        workspaceId: String,
        submissionId: String,
        approve: Boolean,
        feedback: String
    ) {
        val ws = _workspaces.value.find { it.id == workspaceId } ?: return
        val submission = ws.submissions.find { it.id == submissionId } ?: return

        val newSubmissionStatus = if (approve) SubmissionStatus.APPROVED else SubmissionStatus.NEEDS_REVISION
        val updatedSubmission = submission.copy(
            status = newSubmissionStatus,
            staffFeedback = feedback
        )

        val updatedMilestones = ws.milestones.map { ms ->
            if (ms.id == submission.milestoneId) {
                ms.copy(
                    isCompleted = approve,
                    status = newSubmissionStatus
                )
            } else ms
        }

        // Check if all milestones are completed
        val allDone = updatedMilestones.isNotEmpty() && updatedMilestones.all { it.isCompleted }
        val overallStatus = if (allDone) ProjectStatus.COMPLETED
                            else if (!approve) ProjectStatus.NEEDS_REVISION
                            else ProjectStatus.IN_PROGRESS

        val feedbackItem = StaffFeedback(
            id = "fb_" + UUID.randomUUID().toString().take(6),
            projectId = ws.problemId,
            staffName = ws.staffName,
            milestoneOrTopic = submission.milestoneTitle,
            comment = feedback.ifBlank { if (approve) "Milestone deliverables verified and approved." else "Revisions needed before sign-off." },
            actionType = if (approve) "Approved" else "Revision Requested",
            timestamp = System.currentTimeMillis()
        )

        val updatedWorkspace = ws.copy(
            milestones = updatedMilestones,
            submissions = ws.submissions.map { if (it.id == submissionId) updatedSubmission else it },
            feedbackList = listOf(feedbackItem) + ws.feedbackList,
            status = overallStatus
        )

        _workspaces.value = _workspaces.value.map { if (it.id == workspaceId) updatedWorkspace else it }
        _problems.value = _problems.value.map {
            if (it.id == ws.problemId) it.copy(status = overallStatus) else it
        }
        persistAll()
    }

    // RECOMMENDATION LOGIC (Real calculation)
    fun calculateMatch(student: UserAccount, project: ProjectProblem): MatchRecommendation {
        val studentSkillsLower = student.skills.map { it.trim().lowercase() }
        val projectSkillsLower = project.requiredSkills.map { it.trim().lowercase() }

        val matchedSkills = project.requiredSkills.filter { req ->
            val reqLower = req.trim().lowercase()
            studentSkillsLower.any { userSkill ->
                userSkill == reqLower || userSkill.contains(reqLower) || reqLower.contains(userSkill)
            }
        }

        val skillRatio = if (project.requiredSkills.isNotEmpty()) {
            matchedSkills.size.toFloat() / project.requiredSkills.size.toFloat()
        } else 0.5f

        val domainInterestMatch = student.interests.any { interest ->
            interest.equals(project.domain.displayName, ignoreCase = true) ||
            project.domain.name.contains(interest.replace(" ", "_"), ignoreCase = true)
        }

        val domainBonus = if (domainInterestMatch) 0.35f else 0.05f
        val rawScore = (skillRatio * 0.65f + domainBonus) * 100f
        val matchPercentage = rawScore.toInt().coerceIn(25, 98)

        val explanation = buildString {
            if (matchedSkills.isNotEmpty()) {
                append("Matches ${matchedSkills.size} of ${project.requiredSkills.size} required skills (${matchedSkills.joinToString(", ")}). ")
            } else {
                append("Foundational skill set applicable with mentor guidance. ")
            }
            if (domainInterestMatch) {
                append("Directly aligns with your stated passion for ${project.domain.displayName}.")
            } else {
                append("Broadens your practical multidisciplinary experience.")
            }
        }

        return MatchRecommendation(
            project = project,
            matchPercentage = matchPercentage,
            matchedSkills = matchedSkills,
            explanation = explanation
        )
    }

    fun getRecommendationsForStudent(student: UserAccount): List<MatchRecommendation> {
        return _problems.value
            .filter { !it.isArchived }
            .map { calculateMatch(student, it) }
            .sortedByDescending { it.matchPercentage }
    }

    // Default Seed Data
    companion object {
        private const val KEY_USERS = "users_json"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_PROBLEMS = "problems_json"
        private const val KEY_REQUESTS = "requests_json"
        private const val KEY_WORKSPACES = "workspaces_json"

        @Volatile
        private var instance: ProjectConnectRepository? = null

        fun getInstance(context: Context): ProjectConnectRepository {
            return instance ?: synchronized(this) {
                instance ?: ProjectConnectRepository(context).also { instance = it }
            }
        }

        val defaultStudent = UserAccount(
            id = "student_alex_01",
            email = "alex.student@campus.edu",
            role = UserRole.STUDENT,
            name = "Alex Rivera",
            department = "Computer Science & Engineering",
            yearOrTitle = "3rd Year B.Tech",
            skills = listOf("Python", "Machine Learning", "Kotlin", "SQL", "TensorFlow", "React", "OpenCV"),
            interests = listOf("AI & Machine Learning", "Healthcare", "Agriculture", "Software"),
            bio = "Passionate about applied AI models and full-stack software development for impactful social solutions."
        )

        val defaultStaff = UserAccount(
            id = "staff_dr_sharma_01",
            email = "dr.sharma@campus.edu",
            role = UserRole.STAFF,
            name = "Dr. Ramesh Sharma",
            department = "Health Informatics & AI",
            yearOrTitle = "Associate Professor",
            skills = listOf("Healthcare Systems", "Predictive Analytics", "Deep Learning", "Clinical Protocols"),
            interests = listOf("Healthcare", "AI & Machine Learning", "Cybersecurity"),
            bio = "Guiding student capstone projects with focus on high-reliability healthcare decision systems and AI models."
        )

        private fun createDefaultUsers(): List<UserAccount> {
            return listOf(defaultStudent, defaultStaff)
        }

        private fun createInitialSampleProblems(): List<ProjectProblem> {
            return listOf(
                ProjectProblem(
                    id = "sample_prob_01",
                    title = "Smart Crop Disease Detection",
                    domain = ProjectDomain.AGRICULTURE,
                    description = "Design and develop an on-device computer vision mobile pipeline that identifies common leaf fungal and bacterial blight diseases from smartphone photos in low-connectivity rural farm environments.",
                    expectedSolution = "A lightweight quantized CNN or MobileNet classification model integrated into a reactive Android client with actionable pesticide and organic treatment recommendations.",
                    requiredSkills = listOf("Python", "TensorFlow", "Computer Vision", "OpenCV", "Kotlin"),
                    difficulty = DifficultyLevel.INTERMEDIATE,
                    maxTeamSize = 4,
                    deadline = "Nov 30, 2026",
                    staffId = "staff_dr_sharma_01",
                    staffName = "Dr. Sarah Jenkins",
                    staffDept = "Agricultural & Biological Engineering",
                    status = ProjectStatus.AVAILABLE,
                    createdAt = System.currentTimeMillis() - 86400000L * 5,
                    isSample = true
                ),
                ProjectProblem(
                    id = "sample_prob_02",
                    title = "Patient Appointment No-Show Prediction",
                    domain = ProjectDomain.HEALTHCARE,
                    description = "Build a privacy-preserving predictive model using anonymized historical clinic records to forecast patient missed appointments and dynamically optimize outpatient scheduling queues.",
                    expectedSolution = "Ensemble classification algorithm with feature explainability (SHAP values) and an interactive dashboard for hospital administrative staff.",
                    requiredSkills = listOf("Python", "Machine Learning", "Scikit-learn", "SQL", "Pandas"),
                    difficulty = DifficultyLevel.INTERMEDIATE,
                    maxTeamSize = 3,
                    deadline = "Dec 15, 2026",
                    staffId = "staff_dr_sharma_01",
                    staffName = "Dr. Ramesh Sharma",
                    staffDept = "Health Informatics & AI",
                    status = ProjectStatus.IN_PROGRESS,
                    createdAt = System.currentTimeMillis() - 86400000L * 10,
                    isSample = true
                ),
                ProjectProblem(
                    id = "sample_prob_03",
                    title = "Smart Waste Collection Route Planner",
                    domain = ProjectDomain.ENVIRONMENT,
                    description = "Formulate a dynamic Vehicle Routing Problem (VRP) optimization system that ingests ultrasonic IoT bin fill-level telemetry to compute fuel-efficient collection paths for campus municipal trucks.",
                    expectedSolution = "Heuristic route optimization engine with live map visualization, turn-by-turn routing, and carbon-reduction analytics.",
                    requiredSkills = listOf("Kotlin", "Graph Algorithms", "GIS", "Python", "Mobile Dev"),
                    difficulty = DifficultyLevel.ADVANCED,
                    maxTeamSize = 4,
                    deadline = "Jan 20, 2027",
                    staffId = "staff_dr_sharma_01",
                    staffName = "Prof. Elena Rostova",
                    staffDept = "Environmental Sciences & Urban Planning",
                    status = ProjectStatus.AVAILABLE,
                    createdAt = System.currentTimeMillis() - 86400000L * 3,
                    isSample = true
                ),
                ProjectProblem(
                    id = "sample_prob_04",
                    title = "Student Learning Recommendation Platform",
                    domain = ProjectDomain.EDUCATION,
                    description = "Develop an adaptive knowledge-tracing recommendation engine that evaluates student quiz mastery levels and generates customized revision paths, flashcards, and peer study groups.",
                    expectedSolution = "Collaborative filtering and knowledge graph platform supporting student mastery dashboards and automated remedial recommendations.",
                    requiredSkills = listOf("Python", "Natural Language Processing", "Recommendation Systems", "React", "SQL"),
                    difficulty = DifficultyLevel.INTERMEDIATE,
                    maxTeamSize = 3,
                    deadline = "Dec 22, 2026",
                    staffId = "staff_dr_sharma_01",
                    staffName = "Dr. Ramesh Sharma",
                    staffDept = "Computer Science & Education Technology",
                    status = ProjectStatus.AVAILABLE,
                    createdAt = System.currentTimeMillis() - 86400000L * 2,
                    isSample = true
                ),
                ProjectProblem(
                    id = "sample_prob_05",
                    title = "Zero-Trust Campus Network Anomaly Detector",
                    domain = ProjectDomain.CYBERSECURITY,
                    description = "Analyze NetFlow traffic telemetry on university subnetworks to detect lateral privilege escalation and distributed denial-of-service attempts without decrypting payload packets.",
                    expectedSolution = "Isolation forest anomaly detection microservice with real-time alerting webhook integration.",
                    requiredSkills = listOf("Python", "Cybersecurity", "Network Protocols", "Docker", "Machine Learning"),
                    difficulty = DifficultyLevel.ADVANCED,
                    maxTeamSize = 3,
                    deadline = "Feb 10, 2027",
                    staffId = "staff_dr_sharma_01",
                    staffName = "Prof. Marcus Vance",
                    staffDept = "Information Security",
                    status = ProjectStatus.AVAILABLE,
                    createdAt = System.currentTimeMillis() - 86400000L * 1,
                    isSample = true
                ),
                ProjectProblem(
                    id = "sample_prob_06",
                    title = "Community Micro-Grant Transparency Ledger",
                    domain = ProjectDomain.SOCIAL_IMPACT,
                    description = "Build a verifiable ledger application enabling non-profit student societies to publish itemized receipts, community voting, and audit trails for grassroots philanthropic initiatives.",
                    expectedSolution = "Auditable web/mobile interface with cryptographic hashing of fund disbursement milestones.",
                    requiredSkills = listOf("Software", "React", "Kotlin", "SQL", "Cryptography"),
                    difficulty = DifficultyLevel.BEGINNER,
                    maxTeamSize = 2,
                    deadline = "Nov 15, 2026",
                    staffId = "staff_dr_sharma_01",
                    staffName = "Dr. Anita Desai",
                    staffDept = "Social Sciences & Policy",
                    status = ProjectStatus.AVAILABLE,
                    createdAt = System.currentTimeMillis() - 86400000L * 4,
                    isSample = true
                )
            )
        }

        private fun createInitialSampleRequests(): List<ProjectRequest> {
            return listOf(
                ProjectRequest(
                    id = "sample_req_01",
                    problemId = "sample_prob_02",
                    projectTitle = "Patient Appointment No-Show Prediction",
                    domain = ProjectDomain.HEALTHCARE,
                    studentId = "student_alex_01",
                    studentName = "Alex Rivera",
                    studentDept = "Computer Science & Engineering",
                    studentYear = "3rd Year B.Tech",
                    teamName = "HealthPulse Analytics",
                    teamLeader = "Alex Rivera",
                    teamMembers = listOf(
                        TeamMember("Alex Rivera", "alex.student@campus.edu", "Team Leader / ML Lead"),
                        TeamMember("Priya Patel", "priya.p@campus.edu", "Data Pipeline Engineer"),
                        TeamMember("Jordan Lee", "jordan.l@campus.edu", "Full-Stack Dev")
                    ),
                    matchPercentage = 94,
                    statementOfIntent = "Our team has completed coursework in Biostatistics and Machine Learning. We aim to construct an XGBoost + SHAP prediction model evaluated against standard MIMIC clinical datasets.",
                    status = RequestStatus.APPROVED,
                    staffNotes = "Strong academic background and well-rounded team composition. Request approved! Please submit your formal project proposal.",
                    requestedAt = System.currentTimeMillis() - 86400000L * 8
                )
            )
        }

        private fun createInitialSampleWorkspaces(): List<ProjectWorkspace> {
            val problem = createInitialSampleProblems()[1] // Patient Appointment No-Show Prediction
            val milestones = listOf(
                Milestone(
                    id = "ms_sample_1",
                    projectId = problem.id,
                    title = "Milestone 1: Literature Review & Dataset Preprocessing",
                    description = "Perform exploratory data analysis on anonymized historical outpatient visits, address class imbalance, and clean demographic data.",
                    deadline = "Oct 30, 2026",
                    order = 1,
                    isCompleted = true,
                    status = SubmissionStatus.APPROVED
                ),
                Milestone(
                    id = "ms_sample_2",
                    projectId = problem.id,
                    title = "Milestone 2: Baseline Model Training & Cross-Validation",
                    description = "Train Logistic Regression, Random Forest, and LightGBM models. Document ROC-AUC and F1 scores with 5-fold cross validation.",
                    deadline = "Nov 15, 2026",
                    order = 2,
                    isCompleted = false,
                    status = SubmissionStatus.UNDER_REVIEW
                ),
                Milestone(
                    id = "ms_sample_3",
                    projectId = problem.id,
                    title = "Milestone 3: Explainability Pipeline & Dashboard UI",
                    description = "Compute SHAP feature attribution graphs and construct administrative review screen for clinic schedulers.",
                    deadline = "Dec 05, 2026",
                    order = 3,
                    isCompleted = false,
                    status = SubmissionStatus.UNDER_REVIEW
                ),
                Milestone(
                    id = "ms_sample_4",
                    projectId = problem.id,
                    title = "Milestone 4: Final Capstone Defense & Technical Report",
                    description = "Complete peer-reviewed manuscript draft, reproduce test results, and deliver 15-minute committee presentation.",
                    deadline = "Dec 15, 2026",
                    order = 4,
                    isCompleted = false,
                    status = SubmissionStatus.UNDER_REVIEW
                )
            )

            val submissions = listOf(
                MilestoneSubmission(
                    id = "sub_sample_1",
                    milestoneId = "ms_sample_1",
                    milestoneTitle = "Milestone 1: Literature Review & Dataset Preprocessing",
                    documentTitle = "EDA_Report_and_Data_Cleaning_v1.pdf",
                    notes = "Included correlation matrix, outlier handling for transit distance, and SMOTE synthesis summary.",
                    fileName = "EDA_Report_and_Data_Cleaning_v1.pdf",
                    fileSizeBytes = 1024 * 720,
                    submittedBy = "Alex Rivera",
                    submittedAt = System.currentTimeMillis() - 86400000L * 6,
                    status = SubmissionStatus.APPROVED,
                    staffFeedback = "Excellent exploratory analysis. The rationale for SMOTE over-sampling is sound. Approved to proceed with model training."
                ),
                MilestoneSubmission(
                    id = "sub_sample_2",
                    milestoneId = "ms_sample_2",
                    milestoneTitle = "Milestone 2: Baseline Model Training & Cross-Validation",
                    documentTitle = "LightGBM_CrossValidation_Weights.pdf",
                    notes = "Achieved 0.88 ROC-AUC on holdout validation. Model weights and confusion matrices attached.",
                    fileName = "LightGBM_CrossValidation_Weights.pdf",
                    fileSizeBytes = 1024 * 1250,
                    submittedBy = "Alex Rivera",
                    submittedAt = System.currentTimeMillis() - 86400000L * 1,
                    status = SubmissionStatus.UNDER_REVIEW,
                    staffFeedback = ""
                )
            )

            val feedback = listOf(
                StaffFeedback(
                    id = "fb_sample_1",
                    projectId = problem.id,
                    staffName = "Dr. Ramesh Sharma",
                    milestoneOrTopic = "Milestone 1 Review",
                    comment = "Excellent data preparation. The visual distributions highlight the weekend clinic appointment variances clearly. Proceed to baseline model benchmarking.",
                    actionType = "Approved",
                    timestamp = System.currentTimeMillis() - 86400000L * 5
                ),
                StaffFeedback(
                    id = "fb_sample_2",
                    projectId = problem.id,
                    staffName = "Dr. Ramesh Sharma",
                    milestoneOrTopic = "Formal Proposal Approval",
                    comment = "The proposed methodology and 8-week timeline look thorough and feasible. Proposal officially approved.",
                    actionType = "Approved",
                    timestamp = System.currentTimeMillis() - 86400000L * 7
                )
            )

            return listOf(
                ProjectWorkspace(
                    id = "ws_sample_01",
                    problemId = problem.id,
                    projectTitle = problem.title,
                    domain = problem.domain,
                    staffId = problem.staffId,
                    staffName = problem.staffName,
                    staffDept = problem.staffDept,
                    teamName = "HealthPulse Analytics",
                    teamLeader = "Alex Rivera",
                    teamMembers = listOf(
                        TeamMember("Alex Rivera", "alex.student@campus.edu", "Team Leader / ML Lead"),
                        TeamMember("Priya Patel", "priya.p@campus.edu", "Data Pipeline Engineer"),
                        TeamMember("Jordan Lee", "jordan.l@campus.edu", "Full-Stack Dev")
                    ),
                    proposal = ProjectProposal(
                        id = "prop_sample_01",
                        problemId = problem.id,
                        title = "Predictive Clinical Attendance Optimization",
                        objective = "Reduce clinic revenue loss and outpatient wait times by forecasting non-attendance 48 hours prior to appointment.",
                        methodology = "Feature engineering on weather, transit proximity, and prior appointment compliance, coupled with LightGBM classification and SHAP explanation.",
                        techStack = listOf("Python", "Scikit-learn", "LightGBM", "FastAPI", "PostgreSQL", "SHAP"),
                        timelineWeeks = 8,
                        status = SubmissionStatus.APPROVED,
                        staffFeedback = "Solid methodology. Make sure you adhere to HIPAA-compliant anonymization throughout all pipeline stages.",
                        submittedAt = System.currentTimeMillis() - 86400000L * 7
                    ),
                    milestones = milestones,
                    submissions = submissions,
                    feedbackList = feedback,
                    status = ProjectStatus.IN_PROGRESS
                )
            )
        }

        fun generateDefaultMilestones(problemId: String): List<Milestone> {
            return listOf(
                Milestone(
                    id = "ms_" + UUID.randomUUID().toString().take(6),
                    projectId = problemId,
                    title = "Phase 1: Requirements & Architecture Specification",
                    description = "Detailed system architecture diagram, schema definition, and literature survey.",
                    deadline = "In 2 weeks",
                    order = 1,
                    isCompleted = false,
                    status = SubmissionStatus.UNDER_REVIEW
                ),
                Milestone(
                    id = "ms_" + UUID.randomUUID().toString().take(6),
                    projectId = problemId,
                    title = "Phase 2: Core Algorithm & Prototype Implementation",
                    description = "Working core algorithm or alpha module with unit tests and synthetic benchmark validation.",
                    deadline = "In 4 weeks",
                    order = 2,
                    isCompleted = false,
                    status = SubmissionStatus.UNDER_REVIEW
                ),
                Milestone(
                    id = "ms_" + UUID.randomUUID().toString().take(6),
                    projectId = problemId,
                    title = "Phase 3: Integration, Testing & User Evaluation",
                    description = "End-to-end integration with client app or API and evaluation report against success metrics.",
                    deadline = "In 6 weeks",
                    order = 3,
                    isCompleted = false,
                    status = SubmissionStatus.UNDER_REVIEW
                ),
                Milestone(
                    id = "ms_" + UUID.randomUUID().toString().take(6),
                    projectId = problemId,
                    title = "Phase 4: Final Capstone Documentation & Demo",
                    description = "Complete capstone thesis document, video demonstration, and code repository audit.",
                    deadline = "In 8 weeks",
                    order = 4,
                    isCompleted = false,
                    status = SubmissionStatus.UNDER_REVIEW
                )
            )
        }
    }
}
