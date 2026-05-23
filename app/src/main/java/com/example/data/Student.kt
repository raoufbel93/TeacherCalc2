package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val score: Double,
    val maxScore: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val semester: String,
    val academicYear: String,
    val subject: String,
    val institution: String
)
