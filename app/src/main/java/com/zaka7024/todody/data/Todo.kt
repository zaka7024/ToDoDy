package com.zaka7024.todody.data

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.util.*

@Entity(tableName = "todos")
@Parcelize
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val todoId: Long? = null,
    val title: String,
    var date: LocalDate? = null,
    var time: Date? = null,
    var reminderTime: Date? = null
): Parcelable

@Entity
@Parcelize
data class Subitem(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    var todoOwnerId: Long? = null,
    val item: String
): Parcelable

@Parcelize
data class TodosWithSubitems(
    @Embedded val todo: Todo,
    @Relation(
        parentColumn = "todoId",
        entityColumn = "todoOwnerId"
    )
    val subitems: MutableList<Subitem>
) : Parcelable