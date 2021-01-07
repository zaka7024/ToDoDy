package com.zaka7024.todody.data

data class Todo(val title: String, val subItems: MutableList<String> = mutableListOf())