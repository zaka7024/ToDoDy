package com.zaka7024.todody.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Todo::class, Subitem::class, Category::class], version = 1)
@TypeConverters(Converters::class)
abstract class TodoDatabase: RoomDatabase() {
    abstract fun daysDao(): TodoDao

    companion object {
        private var instance: TodoDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): TodoDatabase {
            if(instance == null) {
                synchronized(TodoDatabase::class){
                    instance = Room.databaseBuilder(ctx.applicationContext, TodoDatabase::class.java,
                        "todos_database")
                        .build()
                }
            }
            return instance!!
        }
    }
}