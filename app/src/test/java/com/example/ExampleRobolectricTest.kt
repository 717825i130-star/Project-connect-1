package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.data.repository.ProjectConnectRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ProjectConnect", appName)
    }

    @Test
    fun `test matching algorithm calculates score`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = ProjectConnectRepository.getInstance(context)

        val student = ProjectConnectRepository.defaultStudent
        val recommendations = repository.getRecommendationsForStudent(student)

        assertTrue("Should have recommendations", recommendations.isNotEmpty())
        val topMatch = recommendations.first()
        assertTrue("Top match percentage should be > 50%", topMatch.matchPercentage >= 50)
        assertTrue("Explanation should not be empty", topMatch.explanation.isNotBlank())
    }

    @Test
    fun `test workflow progress calculation`() {
        val milestones = listOf(
            Milestone("m1", "p1", "M1", "desc", "Nov 1", 1, isCompleted = true),
            Milestone("m2", "p1", "M2", "desc", "Nov 15", 2, isCompleted = false)
        )
        val ws = ProjectWorkspace(
            id = "w1",
            problemId = "p1",
            projectTitle = "Title",
            domain = ProjectDomain.AI_ML,
            staffId = "s1",
            staffName = "Dr. Test",
            staffDept = "CS",
            teamName = "Team A",
            teamLeader = "Alex",
            teamMembers = emptyList(),
            milestones = milestones,
            status = ProjectStatus.IN_PROGRESS
        )
        assertEquals(50, ws.progressPercentage)
    }
}
