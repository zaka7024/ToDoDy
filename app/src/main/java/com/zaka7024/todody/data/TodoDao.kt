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

    @Query("select * from category where categoryId=:categoryId limit 1")
    fun getCategory(categoryId: Long): Category

    @Query("select * from category")
    fun getAllCategory(): LiveData<List<Category>>

    @Query("select * from category")
    fun getAllCategories(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubitems(vararg subitems: Subitem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubitem(subitems: Subitem) : Long

    @Delete
    fun removeSubitem(subitems: Subitem)

    @Delete
    fun removeTodo(todo: Todo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(todo: Todo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(subitems: List<Subitem>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(subitem: Subitem)

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
    @Query("SELECT * FROM todos where date = :localDate and categoryOwnerId=:categoryId")
    fun getAllTodosWithinDateCategory(
        localDate: LocalDate,
        categoryId: Long
    ): List<TodosWithSubitems>

    @Transaction
    @Query("SELECT * FROM todos where date <> :localDate and categoryOwnerId =:categoryId")
    fun getAllTodosExceptDateWithinCategory(
        localDate: LocalDate,
        categoryId: Long
    ): List<TodosWithSubitems>

    @Transaction
    @Query("SELECT * FROM category where categoryName=:name")
    fun getAllTodosWithinCategory(name: String): CategoryWithTodos

    @Transaction
    @Query("SELECT count(todoId) FROM todos where completed=:completeCondition")
    fun getAllTodosCount(completeCondition: Boolean): LiveData<Int>
}
