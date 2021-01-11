package com.zaka7024.todody.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDate

@Dao
interface TodoDao {

    @Insert
    fun insert(todo: Todo): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(category: Category): Long

    @Query("select * from category where categoryName=:name limit 1")
    fun getCategory(name: String): Category

    @Query("select * from category")
    fun getAllCategory(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubitems(vararg subitems: Subitem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(todo: Todo)

    @Transaction
    @Query("SELECT * FROM todos")
    fun getAllTodosWithSubitems(): LiveData<List<TodosWithSubitems>>

    @Transaction
    @Query("SELECT * FROM category")
    fun getAllTodosCategory(): CategoryWithTodos

    @Transaction
    @Query("SELECT * FROM todos where date = :localDate")
    fun getAllTodosWithinDate(localDate: LocalDate): List<TodosWithSubitems>

    @Transaction
    @Query("SELECT * FROM category where categoryName=:name")
    fun getAllTodosWithinCategory(name: String): CategoryWithTodos
}
