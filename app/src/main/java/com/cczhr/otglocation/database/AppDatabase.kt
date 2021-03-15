package com.cczhr.otglocation.database

import android.content.Context
import android.view.Display
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.android.material.circularreveal.CircularRevealHelper
import com.scigrace.controller.database.LocationDataDao

@Database(entities = [LocationData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDataDao(): LocationDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
