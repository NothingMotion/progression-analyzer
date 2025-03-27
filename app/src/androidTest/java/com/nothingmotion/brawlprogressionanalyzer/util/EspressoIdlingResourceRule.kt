package com.nothingmotion.brawlprogressionanalyzer.util

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit Rule that registers and unregisters an idling resource for testing asynchronous operations.
 * Use this when your code needs to wait for an asynchronous operation to complete.
 */
class EspressoIdlingResourceRule : TestWatcher() {
    
    private val idlingResource = CountingIdlingResource("GLOBAL")

    override fun starting(description: Description) {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    override fun finished(description: Description) {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    /**
     * Call this when an asynchronous operation starts
     */
    fun increment() {
        idlingResource.increment()
    }

    /**
     * Call this when an asynchronous operation ends
     */
    fun decrement() {
        if (!idlingResource.isIdleNow) {
            idlingResource.decrement()
        }
    }
} 