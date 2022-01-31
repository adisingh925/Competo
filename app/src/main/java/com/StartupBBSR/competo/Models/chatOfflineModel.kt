package com.StartupBBSR.competo.Models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messageData")
data class chatOfflineModel(
    @PrimaryKey val id : String,
    @ColumnInfo(name = "senderId") val senderId : String,
    @ColumnInfo(name = "receiverId") val receiverId : String,
    @ColumnInfo(name = "msg") val msg : String,
    @ColumnInfo(name = "receiveTime") val receiveTime : String,
    @ColumnInfo(name = "sendTime") val sendTime : String,
    @ColumnInfo(name = "isSeen") val isSeen : Boolean,
    @ColumnInfo(name = "needsResend") val needsResend : Boolean,
)