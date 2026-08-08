package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActionConfig
import com.example.data.FlowEntity
import com.example.data.JsonUtils
import com.example.data.TriggerConfig
import com.example.data.TriggerType
import com.example.viewmodel.ZenFlowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ZenFlowViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var viewModel: ZenFlowViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = ZenFlowViewModel(application)
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.onClearedForTest()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateLoadsDefaultFlows() = runTest(testDispatcher) {
        advanceUntilIdle()
        val flows = viewModel.userFlows.first { it.isNotEmpty() }
        assertNotNull(flows)
        assertTrue("Initial flows should be loaded from default templates", flows.isNotEmpty())
    }

    @Test
    fun testCreateAndSaveNewFlow() = runTest(testDispatcher) {
        advanceUntilIdle()
        val initialFlows = viewModel.userFlows.first { it.isNotEmpty() }
        val initialCount = initialFlows.size

        viewModel.saveFlow(
            id = null,
            title = "Test Morning Routine",
            description = "Turns on lights and sets volume",
            category = "Lifestyle",
            iconName = "Schedule",
            colorHex = "#818CF8",
            triggerType = TriggerType.SCHEDULE,
            triggerConfig = TriggerConfig(timeStart = "07:30"),
            actionConfig = ActionConfig(ringtoneMode = "SILENT"),
            scheduledTime = "07:30",
            tags = "Morning, Test"
        )

        advanceUntilIdle()
        val updatedFlows = viewModel.userFlows.first { it.size > initialCount }
        assertEquals(initialCount + 1, updatedFlows.size)

        val created = updatedFlows.find { it.title == "Test Morning Routine" }
        assertNotNull(created)
        assertEquals("Lifestyle", created?.category)
        assertTrue(created?.isEnabled == true)
    }

    @Test
    fun testToggleFlowEnabledState() = runTest(testDispatcher) {
        advanceUntilIdle()
        val initialFlows = viewModel.userFlows.first { it.isNotEmpty() }
        val flow = initialFlows.first()
        val initialState = flow.isEnabled

        viewModel.toggleFlowEnabled(flow)
        advanceUntilIdle()

        val updatedFlows = viewModel.userFlows.first { list -> list.find { it.id == flow.id }?.isEnabled != initialState }
        val updatedFlow = updatedFlows.find { it.id == flow.id }
        assertEquals(!initialState, updatedFlow?.isEnabled)
    }

    @Test
    fun testManualFlowExecutionLogsAndPulse() = runTest(testDispatcher) {
        advanceUntilIdle()
        val initialFlows = viewModel.userFlows.first { it.isNotEmpty() }
        val flow = initialFlows.first()

        viewModel.runFlowManually(flow)
        advanceUntilIdle()

        val logs = viewModel.historyLogs.first { list -> list.any { it.flowId == flow.id && it.status == "MANUAL" } }
        val recentLog = logs.find { it.flowId == flow.id && it.status == "MANUAL" }
        assertNotNull("Manual execution should produce a history log entry with status MANUAL", recentLog)
        assertEquals("MANUAL", recentLog?.status)

        val updatedFlows = viewModel.userFlows.first { list -> list.find { it.id == flow.id }?.runCount == flow.runCount + 1 }
        val updatedFlow = updatedFlows.find { it.id == flow.id }
        assertEquals(flow.runCount + 1, updatedFlow?.runCount)
    }

    @Test
    fun testDeleteFlowAndBatchDelete() = runTest(testDispatcher) {
        advanceUntilIdle()
        val flowsBefore = viewModel.userFlows.first { it.isNotEmpty() }
        val targetFlow = flowsBefore.first()

        viewModel.deleteFlow(targetFlow)
        advanceUntilIdle()

        val flowsAfterDelete = viewModel.userFlows.first { list -> list.none { it.id == targetFlow.id } }
        assertFalse(flowsAfterDelete.any { it.id == targetFlow.id })

        if (flowsAfterDelete.size >= 2) {
            val idsToDelete = flowsAfterDelete.take(2).map { it.id }.toSet()
            viewModel.batchDeleteFlows(idsToDelete)
            advanceUntilIdle()

            val flowsAfterBatch = viewModel.userFlows.first { list -> idsToDelete.none { id -> list.any { flow -> flow.id == id } } }
            assertFalse(flowsAfterBatch.any { it.id in idsToDelete })
        }
    }

    @Test
    fun testFilterByCategoryAndSearchQuery() = runTest(testDispatcher) {
        advanceUntilIdle()
        val initialFlows = viewModel.userFlows.first { it.isNotEmpty() }
        viewModel.setCategory("Work")
        advanceUntilIdle()

        val filteredWork = viewModel.filteredUserFlows.value
        assertTrue("Work category filter should return matching items", filteredWork.all { it.category == "Work" })

        viewModel.setCategory("All")
        val targetTitle = initialFlows.first().title.take(4)
        viewModel.setSearchQuery(targetTitle)
        advanceUntilIdle()

        val searchResults = viewModel.filteredUserFlows.value
        assertTrue("Search query filter should return matching items", searchResults.all { it.title.contains(targetTitle, ignoreCase = true) || it.description.contains(targetTitle, ignoreCase = true) })
    }

    @Test
    fun testActivateTemplateValidations() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.userFlows.first { it.isNotEmpty() }
        val sampleTemplate = FlowEntity(
            id = "template_123",
            title = "New Custom Template Routine",
            description = "Template description",
            category = "Health",
            iconName = "FitnessCenter",
            colorHex = "#10B981",
            triggerType = TriggerType.BATTERY,
            actionConfigJson = JsonUtils.serializeAction(ActionConfig(announceTts = true)),
            isTemplate = true
        )

        viewModel.activateTemplate(sampleTemplate)
        advanceUntilIdle()

        val userFlows = viewModel.userFlows.first { list -> list.any { it.title == "New Custom Template Routine" } }
        assertTrue(userFlows.any { it.title == "New Custom Template Routine" })
    }
}
