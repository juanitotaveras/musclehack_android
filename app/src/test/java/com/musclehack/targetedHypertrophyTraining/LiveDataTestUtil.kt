package com.musclehack.targetedHypertrophyTraining

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Utility for testing LiveData objects.
 *
 * Gets the value of a [LiveData] or waits for it to have one, with a timeout.
 * Use this extension from host-side (JVM) tests. It's recommended to use it alongside
 * `InstantTaskExecutorRule` or a similar mechanism to execute tasks synchronously.
 */
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    unit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(t: T) {
            data = t
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    observeForever(observer)
    try {
        afterObserve()
        if (!latch.await(time, unit)) {
            throw AssertionError("LiveData value was never set.")
        }
    } finally {
        removeObserver(observer)
    }
    @Suppress("UNCHECKED_CAST")
    return data as T
}
