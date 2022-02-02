package com.StartupBBSR.competo.teamosDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.StartupBBSR.competo.Dao.teamosDao
import com.StartupBBSR.competo.Models.chatOfflineModel
import com.StartupBBSR.competo.Models.userOfflineModel

@Database(entities = [userOfflineModel::class,chatOfflineModel::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase(){

        abstract fun teamosDao() : teamosDao

    companion object{

        @Volatile
        private var INSTANCE : com.StartupBBSR.competo.teamosDatabase.Database? = null

        fun getDatabase(context: Context): com.StartupBBSR.competo.teamosDatabase.Database
        {
            val tempInstance = INSTANCE
            if(tempInstance!=null)
            {
                return tempInstance
            }
            synchronized(this)
            {
                val instance = Room.databaseBuilder(context.applicationContext,
                    com.StartupBBSR.competo.teamosDatabase.Database::class.java,
                    "teamosDatabase").build()

                INSTANCE = instance
                return instance
            }
        }
    }
}