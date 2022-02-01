package com.StartupBBSR.competo.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.StartupBBSR.competo.Models.chatOfflineModel
import com.StartupBBSR.competo.Models.userOfflineModel

@Dao
interface teamosDao {

    /**All functions for chat data related operations*/

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessageData(data : chatOfflineModel)

    @Delete
    fun deleteMessageData(data : chatOfflineModel)

    @Update
    fun updateMessageData(data : chatOfflineModel)

    @Query("select * from messageData")
    fun readAllMessagesForUser() : LiveData<List<chatOfflineModel>>

    @Query("delete from messageData")
    fun deleteAllMessagesForUser()

    @Query("delete from messageData")
    fun deleteAllMessages()

    @Query("select * from messageData")
    fun readAllMessages() : LiveData<List<chatOfflineModel>>

    /**All functions for user data related operations*/

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUserData(data : userOfflineModel)

    @Delete
    fun deleteUserData(data : userOfflineModel)

    @Update
    fun updateUserData(data : userOfflineModel)

    @Query("select * from userData")
    fun readAllDataForUser() : LiveData<List<userOfflineModel>>

    @Delete
    fun deleteAllDataForUser(data : userOfflineModel)

    @Query("delete from userData")
    fun deleteAllUsers()

    @Query("select * from userData")
    fun readAllUsers() : LiveData<List<userOfflineModel>>
}