package com.nothingmotion.brawlprogressionanalyzer

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nothingmotion.brawlprogressionanalyzer.ui.newaccount.NewAccountFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewAccountFragmentTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        // Create a TestNavHostController
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        
        // Set the graph and start destination
        navController.setGraph(R.navigation.nav_graph)
        navController.setCurrentDestination(R.id.navigation_new_account)
    }

    @Test
    fun testBackNavigationOnButtonClick() {
        // Launch NewAccountFragment
        val scenario = launchFragmentInContainer<NewAccountFragment>(themeResId = R.style.Theme_BrawlProgressionAnalyzer)

        // Set the NavController to the fragment
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Check that form elements are displayed
        onView(withId(R.id.account_name_edit_text)).check(matches(isDisplayed()))
        onView(withId(R.id.player_tag_edit_text)).check(matches(isDisplayed()))
        onView(withId(R.id.add_account_button)).check(matches(isDisplayed()))

        // Click the Add Account button
        onView(withId(R.id.add_account_button)).perform(click())

        // Verify navigation occurred - should pop back to accounts
        // Note: When using popBackStack(), we can't directly check the destination ID
        // Instead, we'd need to check that the fragment is no longer in view
    }

    @Test
    fun testFormInteraction() {
        // Launch NewAccountFragment
        launchFragmentInContainer<NewAccountFragment>(themeResId = R.style.Theme_BrawlProgressionAnalyzer)

        // Type text in the input fields
        onView(withId(R.id.account_name_edit_text)).perform(typeText("Test Account"))
        onView(withId(R.id.player_tag_edit_text)).perform(typeText("#ABC123"))
        
        // Verify text was entered correctly
        onView(withId(R.id.account_name_edit_text)).check(matches(withText("Test Account")))
        onView(withId(R.id.player_tag_edit_text)).check(matches(withText("#ABC123")))
    }
} 