package com.StartupBBSR.competo.repository

import com.StartupBBSR.competo.Dao.teamosDao
import com.StartupBBSR.competo.Models.chatOfflineModel
import com.StartupBBSR.competo.Models.userOfflineModel

class repository(private val teamosDao: teamosDao) {

    val readAllMessagesForUser = teamosDao.readAllMessagesForUser()

    val readAllMessages = teamosDao.readAllMessages()

    val readAllDataForUser = teamosDao.readAllDataForUser()

    val readAllUsers = teamosDao.readAllUsers()

    /**All functions for chat data related operations*/

    fun insertMessageData(data : chatOfflineModel)
    {
        teamosDao.insertMessageData(data)
    }

    fun deleteMessageData(data : chatOfflineModel)
    {
        teamosDao.deleteMessageData(data)
    }

    fun updateMessageData(data : chatOfflineModel)
    {
        teamosDao.updateMessageData(data)
    }

    fun deleteAllMessagesForUser()
    {
        teamosDao.deleteAllMessagesForUser()
    }

    fun deleteAllMessages()
    {
        teamosDao.deleteAllMessages()
    }

    /**All functions for user data related operations*/


    fun insertUserData(data : userOfflineModel)
    {
        teamosDao.insertUserData(data)
    }

    fun deleteUserData(data : userOfflineModel)
    {
        teamosDao.deleteUserData(data)
    }

    fun updateUserData(data : userOfflineModel)
    {
        teamosDao.updateUserData(data)
    }

    fun deleteAllDataForUser(data : userOfflineModel)
    {
        teamosDao.deleteAllDataForUser(data)
    }

    fun deleteAllUsers()
    {
        teamosDao.deleteAllUsers()
    }
}