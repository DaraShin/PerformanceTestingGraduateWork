package com.shinkevich.benchmarkable

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Person::class], version = 1)
abstract class BenchmarkableDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao

    companion object{
        private var dbInstance: BenchmarkableDatabase? = null

        fun getDBInstance(context:Context): BenchmarkableDatabase{
            if (dbInstance == null){
                dbInstance = Room.databaseBuilder(
                    context.applicationContext,
                    BenchmarkableDatabase::class.java, "benchmarkable-database"
                ).build()
            }
            return dbInstance!!
        }
    }
}