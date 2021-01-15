package com.zaka7024.todody.data

import android.os.Parcelable
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.util.*

@Entity(indices = arrayOf(Index(value = ["categoryName"], unique = true)))
@Parcelize
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Long? = null,
    @ColumnInfo(name = "categoryName")
    val categoryName: String
) : Parcelable

@Entity(tableName = "todos")
@Parcelize
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val todoId: Long? = null,
    var categoryOwnerId: Long? = null,
    var title: String,
    var date: LocalDate? = null,
    var time: Date? = null,
    var reminderTime: Date? = null,
    var completed: Boolean = false
) : Parcelable

@Entity(foreignKeys = [ForeignKey(entity = Todo::class,
    parentColumns = arrayOf("todoId"),
    childColumns = arrayOf("todoOwnerId"),
    onDelete = ForeignKey.CASCADE)]
)
@Parcelize
data class Subitem(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var todoOwnerId: Long? = null,
    var item: String,
    var completed: Boolean = false
) : Parcelable

data class CategoryWithTodos(
    @Embedded val category: Category,
    @Relation(
        entity = Todo::class,
        parentColumn = "categoryId",
        entityColumn = "categoryOwnerId"
    )
    val todos: MutableList<TodosWithSubitems>
)

@Parcelize
data class TodosWithSubitems(
@Embedded val todo: Todo,
@Relation(
    parentColumn = "todoId",
    entityColumn = "todoOwnerId",
)
val subitems: MutableList<Subitem>
) : Parcelable
