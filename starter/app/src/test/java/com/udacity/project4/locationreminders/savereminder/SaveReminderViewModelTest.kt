package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    private val testData = ReminderDataItem(
        latitude = 29.933130,
        longitude = -95.414880,
        title = "Alexandria",
        description = "Shopping",
        location = "GreenPlaza",
    )
    private val testDataWithoutTitle = ReminderDataItem(
        latitude = 29.933130,
        longitude = -95.414880,
        title = null,
        description = "Shopping",
        location = "GreenPlaza",
    )

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initialize() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

  /*  @Test
    fun showAndHideLoadingAtStartTest() {
        mainCoroutineRule.pauseDispatcher()

        viewModel.validateAndSaveReminder(testData)
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()

        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }

   */

    @Test
    fun showErrorWhenNoTitleTest() {
        viewModel.validateAndSaveReminder(testDataWithoutTitle)
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun navigationBackAfterSaveItemTest() {
        viewModel.validateAndSaveReminder(testData)
        assertThat(viewModel.navigationCommand.getOrAwaitValue()).isEqualTo(NavigationCommand.Back)
    }


}