package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initialize() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun resultNotEmptyWithReminders() = runBlockingTest {
        fakeDataSource.saveReminder(
            ReminderDTO(
                latitude = 29.933130,
                longitude = -95.414880,
                title = "Alexandria",
                description = "Shopping",
                location = "GreenPlaza",
            )
        )
        viewModel.loadReminders()
        assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty()).isFalse()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(viewModel.showNoData.getOrAwaitValue()).isFalse()
    }

    @Test
    fun showLoadingTest() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
    }

    @Test
    fun showErrorTest() = runBlockingTest {
        fakeDataSource.setError(true)
        viewModel.loadReminders()
        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isNotEmpty()
    }

    @Test
    fun hideLoadingTest() = runBlockingTest {
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }
}