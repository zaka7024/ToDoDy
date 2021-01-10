package com.zaka7024.todody.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {

    @Insert
    fun insert(todo: Todo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubitems(vararg subitems: Subitem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(todo: Todo)

    @Query("select * from todos")
    fun getAllTodos(): LiveData<List<Todo>>

    @Transaction
    @Query("SELECT * FROM todos")
    fun getTodosWithSubitems(): LiveData<List<TodosWithSubitems>>
}