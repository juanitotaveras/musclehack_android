package com.musclehack.targetedHypertrophyTraining

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test to verify basic app navigation works.
 * Tests the main navigation flows to ensure the app is functional.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun bottomNavigationIsVisible() {
        // Check that bottom navigation is displayed
        onView(withId(R.id.bottom_nav_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun canNavigateToTrackerTab() {
        // Click on the tracker navigation tab
        onView(withId(R.id.tracker_nav_graph))
            .perform(click())
            .check(matches(isDisplayed()))
    }

    @Test
    fun canNavigateToExerciseBankTab() {
        // Click on the exercise bank navigation tab
        onView(withId(R.id.exercise_bank_nav_graph))
            .perform(click())
            .check(matches(isDisplayed()))
    }

    @Test
    fun fabIsVisibleOnTrackerTab() {
        // Navigate to tracker tab
        onView(withId(R.id.tracker_nav_graph))
            .perform(click())

        // Check that FAB is visible
        onView(withId(R.id.fab))
            .check(matches(isDisplayed()))
    }
}
