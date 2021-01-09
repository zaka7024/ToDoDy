package com.zaka7024.todody.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.util.*

@Entity(tableName = "todos")
@Parcelize
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val subItems: MutableList<String> = mutableListOf(),
    var date: LocalDate? = null,
    var time: Date? = null,
    var reminderTime: Date? = null
): Parcelable