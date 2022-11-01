package com.udacity.project4.locationreminders.reminderslist


import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    private lateinit var reminderRepository: ReminderDataSource
    private val dataBinding = DataBindingIdlingResource()

    private val testData = ReminderDTO(
        latitude = 29.933130,
        longitude = -95.414880,
        title = "Alexandria",
        description = "Shopping",
        location = "GreenPlaza",
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun registerIdlingResources(): Unit = IdlingRegistry.getInstance().run {
        register(EspressoIdlingResource.countingIdlingResource)
        register(dataBinding)
    }

    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(EspressoIdlingResource.countingIdlingResource)
        unregister(dataBinding)
    }

    @Before
    fun initialization() {
        //Without this koin won`t start
        stopKoin()

        val appModule = module {
            viewModel {
                RemindersListViewModel(ApplicationProvider.getApplicationContext(), get() as ReminderDataSource)
            }
            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(appModule))
        }

        reminderRepository = GlobalContext.get().koin.get()

        runBlocking { reminderRepository.deleteAllReminders() }
    }

    @Test
    fun navigationTest() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        dataBinding.monitorFragment(scenario)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun showNotDataTest() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        dataBinding.monitorFragment(scenario)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        Espresso.onView(withText("No Data")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun showRemindersOnScreenTest() {

        runBlocking {
            reminderRepository.saveReminder(testData)
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        dataBinding.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Espresso.onView(withText(testData.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withText(testData.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withText(testData.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

}