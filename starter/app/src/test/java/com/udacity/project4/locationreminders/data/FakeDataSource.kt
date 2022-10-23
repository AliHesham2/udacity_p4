package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource (private val reminders: MutableList<ReminderDTO> = mutableListOf<ReminderDTO>()) : ReminderDataSource {

    private var isThereError = false


    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    fun setError(isError: Boolean) {
        this.isThereError = isError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (isThereError) {
            Result.Error("Error occurred")
        } else {
            Result.Success(reminders)
        }
    }


    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders.find { it.id == id }
        return if (isThereError){
            Result.Error("Error occurred")
        }else {
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Not found")
            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}