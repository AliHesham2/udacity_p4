package com.udacity.project4.locationreminders.data.local


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var db: RemindersDatabase
    private lateinit var dbDao: RemindersDao

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
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java).build()
        dbDao = db.reminderDao()
    }

    @After
    fun closeDatabase() {
        db.close()
    }


    @Test
    fun getFromDBTest() = runBlockingTest {
        dbDao.saveReminder(testData)
        val result = dbDao.getReminderById(testData.id)
        assertThat(result).isNotNull()
        assertThat(result?.location).isEqualTo(testData.location)
        assertThat(result?.latitude).isEqualTo(testData.latitude)
        assertThat(result?.title).isEqualTo(testData.title)
        assertThat(result?.longitude).isEqualTo(testData.longitude)
        assertThat(result?.description).isEqualTo(testData.description)
    }

    @Test
    fun insertToDBTest() = runBlockingTest {
        dbDao.saveReminder(testData)
        assertThat(dbDao.getReminders()).hasSize(1)
        assertThat(dbDao.getReminders().contains(testData))
    }

    fun deleteFromDBTest() = runBlockingTest {
        dbDao.deleteAllReminders()
        assertThat(dbDao.getReminders()).isEmpty()
    }

}