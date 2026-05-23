package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY timestamp DESC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE semester = :semester AND academicYear = :academicYear")
    suspend fun deleteStudentsBySemester(semester: String, academicYear: String)

    @Query("DELETE FROM students WHERE academicYear = :academicYear")
    suspend fun deleteStudentsByYear(academicYear: String)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<Student>)
}
