package com.StartupBBSR.competo.Models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userData")
data class userOfflineModel(
    @PrimaryKey val id : String,
    @ColumnInfo(name = "name") val userName : String,
    @ColumnInfo(name = "email") val userEmail : String,
    @ColumnInfo(name = "photo") val userPhoto : String,
    @ColumnInfo(name = "phone") val userPhone : String,
    @ColumnInfo(name = "bio") val userBio : String,
    @ColumnInfo(name = "linkedin") val userLinkedin : String,
    @ColumnInfo(name = "role") val userRole : String,
    @ColumnInfo(name = "organiserRole") val organiserRole : String,
    //@ColumnInfo(name = "chips") val userChips : List<String>,
    //@ColumnInfo(name = "events") val userEvents : List<String>,
)