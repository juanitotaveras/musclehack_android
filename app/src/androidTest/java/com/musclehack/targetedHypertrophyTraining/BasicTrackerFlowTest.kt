package com.musclehack.targetedHypertrophyTraining

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BasicTrackerFlowTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.POST_NOTIFICATIONS"
        )

    @Test
    fun basicTrackerFlowTest() {
        // Ensure DataBinding is properly initialized on the UI thread
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val floatingActionButton = onView(
            allOf(
                withId(R.id.fab),
                childAtPosition(
                    allOf(
                        withId(R.id.container),
                        childAtPosition(
                            withId(R.id.drawer_layout),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        floatingActionButton.perform(click())

        val actionMenuItemView = onView(
            allOf(
                withId(R.id.createNewCycleNext), withText("Next"),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.action_bar),
                        2
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView.perform(click())

        val actionMenuItemView2 = onView(
            allOf(
                withId(R.id.createNewCycleNext), withText("Next"),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.action_bar),
                        2
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView2.perform(click())

        val actionMenuItemView3 = onView(
            allOf(
                withId(R.id.createNewCycleNext), withText("Next"),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.action_bar),
                        2
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView3.perform(click())

        val actionMenuItemView4 = onView(
            allOf(
                withId(R.id.createCycleButton), withContentDescription("Create"),
                childAtPosition(
                    childAtPosition(
                        withId(androidx.appcompat.R.id.action_bar),
                        2
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView4.perform(click())

        val relativeLayout = onView(
            allOf(
                withId(R.id.cycleListItemContainer),
                childAtPosition(
                    allOf(
                        withId(R.id.cycleListItemBackground),
                        childAtPosition(
                            withId(R.id.cycle_tracker_home_list),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        relativeLayout.perform(click())

        val relativeLayout2 = onView(
            allOf(
                withId(R.id.workoutListItemContainer),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.tracker_workouts_list),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        relativeLayout2.perform(click())

        val appCompatButton = onView(
            allOf(
                withId(R.id.restTimerButton), withText("3:00"),
                childAtPosition(
                    allOf(
                        withId(R.id.cardContainer),
                        childAtPosition(
                            withId(R.id.cardView),
                            0
                        )
                    ),
                    16
                ),
                isDisplayed()
            )
        )
        /**
         * TODO: Figure out issue
         * java.lang.IllegalStateException: DataBinding must be created in view's UI Thread
         * at androidx.databinding.ViewDataBinding.<init>(ViewDataBinding.java:313)
         * at androidx.databinding.ViewDataBinding.<init>(ViewDataBinding.java:333)
         * at com.musclehack.targetedHypertrophyTraining.databinding.LogCardBinding.<init>(LogCardBinding.java:111)
         * at com.musclehack.targetedHypertrophyTraining.databinding.LogCardBindingImpl.<init>(LogCardBindingImpl.java:42)
         * at com.musclehack.targetedHypertrophyTraining.databinding.LogCardBindingImpl.<init>(LogCardBindingImpl.java:39)
         * at com.musclehack.targetedHypertrophyTraining.DataBinderMapperImpl.getDataBinder(DataBinderMapperImpl.java:286)
         */
//        appCompatButton.perform(click())
//
//        val appCompatImageButton = onView(
//            allOf(
//                withId(R.id.stopIcon),
//                childAtPosition(
//                    allOf(
//                        withId(R.id.timerBox),
//                        childAtPosition(
//                            withId(R.id.container),
//                            2
//                        )
//                    ),
//                    3
//                ),
//                isDisplayed()
//            )
//        )
//        appCompatImageButton.perform(click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
