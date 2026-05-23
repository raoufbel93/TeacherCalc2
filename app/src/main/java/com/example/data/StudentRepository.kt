package com.example.data

import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudentsFlow()

    suspend fun insert(student: Student): Long {
        return studentDao.insertStudent(student)
    }

    suspend fun delete(student: Student) {
        studentDao.deleteStudent(student)
    }

    suspend fun deleteBySemester(semester: String, academicYear: String) {
        studentDao.deleteStudentsBySemester(semester, academicYear)
    }

    suspend fun deleteByYear(academicYear: String) {
        studentDao.deleteStudentsByYear(academicYear)
    }

    suspend fun deleteAll() {
        studentDao.deleteAllStudents()
    }

    suspend fun insertAll(students: List<Student>) {
        studentDao.insertAll(students)
    }
}
