package com.zaka7024.todody.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.*

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val subItems: MutableList<String> = mutableListOf(),
    val date: LocalDate? = null,
    val time: Date? = null
)