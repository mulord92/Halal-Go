package com.example

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivityCrashTest {

    @Test
    fun testActivityLaunch() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assert(activity != null)
            }
        }
    }

    @Test
    fun testFareCalculations() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        
        // Test base fares
        assertEquals(55.0, viewModel.getBaseFareForType("Economy"), 0.01)
        assertEquals(65.0, viewModel.getBaseFareForType("Female"), 0.01)
        assertEquals(75.0, viewModel.getBaseFareForType("Family"), 0.01)
        assertEquals(165.0, viewModel.getBaseFareForType("Luxury"), 0.01)

        // Test getFareBreakdown
        val econBreakdown = viewModel.getFareBreakdown("Economy", 5.0, 15, 1.2)
        assertNotNull(econBreakdown)
        assertEquals(55.0, econBreakdown.baseFare, 0.01)
        assertEquals(75.0, econBreakdown.distanceFare, 0.01) // 5km * 15 PHP/km
        assertEquals(30.0, econBreakdown.timeFare, 0.01) // 15 mins * 2 PHP/min
        assertEquals(160.0, econBreakdown.standardFare, 0.01) // 55 + 75 + 30
        assertEquals(32.0, econBreakdown.surgeComponent, 0.01) // 160 * 0.2
        assertEquals(192.0, econBreakdown.totalFare, 0.01) // 160 * 1.2
        
        val luxuryBreakdown = viewModel.getFareBreakdown("Luxury", 10.0, 30, 1.5)
        assertEquals(165.0, luxuryBreakdown.baseFare, 0.01)
        assertEquals(150.0, luxuryBreakdown.distanceFare, 0.01) // 10km * 15 PHP/km
        assertEquals(120.0, luxuryBreakdown.timeFare, 0.01) // 30 mins * 4 PHP/min
        assertEquals(435.0, luxuryBreakdown.standardFare, 0.01) // 165 + 150 + 120
    }
}
