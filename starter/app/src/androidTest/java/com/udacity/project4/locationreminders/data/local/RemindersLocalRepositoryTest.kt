package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var db: RemindersDatabase
    private lateinit var reminderRepository: RemindersLocalRepository

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
    fun setupDatabase() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java).allowMainThreadQueries().build()
        reminderRepository = RemindersLocalRepository(db.reminderDao(), Dispatchers.IO)
    }

    @After
    fun closeDatabase() {
        db.close()
    }


    @Test
    fun getDataByRepository() = runBlocking {
        reminderRepository.saveReminder(testData)
        val result = reminderRepository.getReminder(testData.id)
        result as Result.Success
        assertThat(result.data).isNotNull()
        assertThat(result.data.location).isEqualTo(testData.location)
        assertThat(result.data.latitude).isEqualTo(testData.latitude)
        assertThat(result.data.title).isEqualTo(testData.title)
        assertThat(result.data.description).isEqualTo(testData.description)
        assertThat(result.data.longitude).isEqualTo(testData.longitude)
    }


    @Test
    fun insertDataByRepository() = runBlocking {
        reminderRepository.saveReminder(testData)
        val result = reminderRepository.getReminders()
        result as Result.Success
        assertThat(result.data).isNotEmpty()
        assertThat(result.data).hasSize(1)
    }

    @Test
    fun deleteRemindersSucceeds() = runBlocking {
        reminderRepository.saveReminder(testData)
        reminderRepository.deleteAllReminders()
        val result = reminderRepository.getReminders()
        result as Result.Success
        assertThat(result.data).isEmpty()
    }

    @Test
    fun whenRemindersNotExistTest() = runBlocking {
        val result = reminderRepository.getReminder(testData.id)
        assertThat(result).isInstanceOf(Result.Error::class.java)
        result as Result.Error
        assertThat(result.message).isEqualTo("Reminder not found!")
        assertThat(result.statusCode).isNull()
    }

}