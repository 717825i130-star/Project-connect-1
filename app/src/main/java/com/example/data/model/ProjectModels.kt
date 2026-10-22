package com.example.data.model

enum class UserRole {
    STUDENT,
    STAFF
}

enum class ProjectDomain(val displayName: String) {
    AI_ML("AI & Machine Learning"),
    HEALTHCARE("Healthcare"),
    AGRICULTURE("Agriculture"),
    EDUCATION("Education"),
    CYBERSECURITY("Cybersecurity"),
    FINANCE("Finance"),
    ENVIRONMENT("Environment"),
    INDUSTRIAL("Industrial"),
    SOFTWARE("Software"),
    SOCIAL_IMPACT("Social Impact");

    companion object {
        fun fromDisplayName(name: String): ProjectDomain {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: SOFTWARE
        }
    }
}

enum class DifficultyLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

enum class ProjectStatus(val label: String) {
    AVAILABLE("Available"),
    REQUEST_PENDING("Request Pending"),
    APPROVED("Approved"),
    PROPOSAL_NEEDED("Proposal Needed"),
    IN_PROGRESS("In Progress"),
    NEEDS_REVISION("Needs Revision"),
    COMPLETED("Completed")
}

enum class RequestStatus(val label: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    DECLINED("Declined")
}

enum class SubmissionStatus(val label: String) {
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    NEEDS_REVISION("Needs Revision")
}

data class UserAccount(
    val id: String,
    val email: String,
    val role: UserRole,
    val name: String,
    val department: String,
    val yearOrTitle: String, // e.g. "3rd Year B.Tech" or "Associate Professor"
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(), // For students: domain interests; For staff: research areas
    val bio: String = ""
)

data class TeamMember(
    val name: String,
    val email: String,
    val roleInTeam: String = "Member" // "Team Leader" or "Developer / Researcher"
)

data class ProjectProblem(
    val id: String,
    val title: String,
    val domain: ProjectDomain,
    val description: String,
    val expectedSolution: String,
    val requiredSkills: List<String>,
    val difficulty: DifficultyLevel,
    val maxTeamSize: Int,
    val deadline: String,
    val staffId: String,
    val staffName: String,
    val staffDept: String,
    val status: ProjectStatus = ProjectStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis(),
    val isSample: Boolean = false,
    val isArchived: Boolean = false
)

data class ProjectRequest(
    val id: String,
    val problemId: String,
    val projectTitle: String,
    val domain: ProjectDomain,
    val studentId: String,
    val studentName: String,
    val studentDept: String,
    val studentYear: String,
    val teamName: String,
    val teamLeader: String,
    val teamMembers: List<TeamMember>,
    val matchPercentage: Int,
    val statementOfIntent: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val staffNotes: String = "",
    val requestedAt: Long = System.currentTimeMillis()
)

data class ProjectProposal(
    val id: String,
    val problemId: String,
    val title: String,
    val objective: String,
    val methodology: String,
    val techStack: List<String>,
    val timelineWeeks: Int,
    val status: SubmissionStatus = SubmissionStatus.UNDER_REVIEW,
    val staffFeedback: String = "",
    val submittedAt: Long = System.currentTimeMillis()
)

data class Milestone(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val deadline: String,
    val order: Int,
    val isCompleted: Boolean = false,
    val status: SubmissionStatus = SubmissionStatus.UNDER_REVIEW
)

data class MilestoneSubmission(
    val id: String,
    val milestoneId: String,
    val milestoneTitle: String,
    val documentTitle: String,
    val notes: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val submittedBy: String,
    val submittedAt: Long = System.currentTimeMillis(),
    val status: SubmissionStatus = SubmissionStatus.UNDER_REVIEW,
    val staffFeedback: String = ""
)

data class StaffFeedback(
    val id: String,
    val projectId: String,
    val staffName: String,
    val milestoneOrTopic: String,
    val comment: String,
    val actionType: String, // "Approved", "Revision Requested", "General Guidance"
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectWorkspace(
    val id: String,
    val problemId: String,
    val projectTitle: String,
    val domain: ProjectDomain,
    val staffId: String,
    val staffName: String,
    val staffDept: String,
    val teamName: String,
    val teamLeader: String,
    val teamMembers: List<TeamMember>,
    val proposal: ProjectProposal? = null,
    val milestones: List<Milestone> = emptyList(),
    val submissions: List<MilestoneSubmission> = emptyList(),
    val feedbackList: List<StaffFeedback> = emptyList(),
    val status: ProjectStatus = ProjectStatus.PROPOSAL_NEEDED
) {
    val progressPercentage: Int
        get() {
            if (milestones.isEmpty()) return if (status == ProjectStatus.COMPLETED) 100 else 10
            val completed = milestones.count { it.isCompleted }
            return (completed * 100) / milestones.size
        }
}

data class MatchRecommendation(
    val project: ProjectProblem,
    val matchPercentage: Int,
    val matchedSkills: List<String>,
    val explanation: String
)
