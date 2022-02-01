package com.StartupBBSR.competo.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.StartupBBSR.competo.Models.chatOfflineModel
import com.StartupBBSR.competo.Models.userOfflineModel
import com.StartupBBSR.competo.repository.repository
import com.StartupBBSR.competo.teamosDatabase.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class offlineDatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : repository

    val readAllMessagesForUser : LiveData<List<chatOfflineModel>>

    val readAllMessages : LiveData<List<chatOfflineModel>>

    val readAllDataForUser : LiveData<List<userOfflineModel>>

    val readAllUsers : LiveData<List<userOfflineModel>>

    init {
        val teamosDao = Database.getDatabase(application).teamosDao()
        repository = repository(teamosDao)

        readAllMessagesForUser = repository.readAllMessagesForUser
        readAllMessages = repository.readAllMessages
        readAllDataForUser = repository.readAllDataForUser
        readAllUsers = repository.readAllUsers
    }

    fun insertMessageData(data : chatOfflineModel)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMessageData(data)
        }
    }

    fun deleteMessageData(data : chatOfflineModel)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMessageData(data)
        }
    }

    fun updateMessageData(data : chatOfflineModel)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMessageData(data)
        }
    }

    fun deleteAllMessagesForUser()
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllMessagesForUser()
        }
    }

    fun deleteAllMessages()
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllMessages()
        }
    }

    /**All functions for user data related operations*/


    fun insertUserData(data : userOfflineModel)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertUserData(data)
        }
    }

    fun deleteUserData(data : userOfflineModel)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserData(data)
        }
    }

    fun updateUserData(data : userOfflineModel)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserData(data)
        }
    }

    fun deleteAllDataForUser(data : userOfflineModel)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllDataForUser(data)
        }
    }

    fun deleteAllUsers()
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllUsers()
        }
    }
}