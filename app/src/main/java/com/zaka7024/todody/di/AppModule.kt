package com.zaka7024.todody.di

import android.content.Context
import com.zaka7024.todody.data.TodoDao
import com.zaka7024.todody.data.TodoDatabase
import com.zaka7024.todody.ui.task.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): TodoDatabase {
        return TodoDatabase.getInstance(appContext)
    }

    @Provides
    @Singleton
    fun provideTodoDao(citiesDatabase: TodoDatabase): TodoDao {
        return citiesDatabase.daysDao()
    }

    @Provides
    @Singleton
    fun provideTodoRepository(todoDao: TodoDao): TodoRepository {
        return TodoRepository(todoDao)
    }
}
