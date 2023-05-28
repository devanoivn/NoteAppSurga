package com.example.noteappsurga

data class Note(
    var id: String? = null,
    var title: String? = null,
    var content: String? = null,
    var priority: Int = 0
)