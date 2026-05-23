package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Student
import com.example.data.StudentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudentRepository
    private val sharedPrefs = application.getSharedPreferences("exam_corrector_prefs", Context.MODE_PRIVATE)

    // Room state
    val students: StateFlow<List<Student>>

    // Settings State
    private val _teacherName = MutableStateFlow(sharedPrefs.getString("teacher_name", "") ?: "")
    val teacherName = _teacherName.asStateFlow()

    private val _subject = MutableStateFlow(sharedPrefs.getString("subject", "") ?: "")
    val subject = _subject.asStateFlow()

    private val _institution = MutableStateFlow(sharedPrefs.getString("institution", "") ?: "")
    val institution = _institution.asStateFlow()

    private val _academicYear = MutableStateFlow(sharedPrefs.getString("academic_year", "2025-2026") ?: "2025-2026")
    val academicYear = _academicYear.asStateFlow()

    private val _semester = MutableStateFlow(sharedPrefs.getString("semester", "1") ?: "1")
    val semester = _semester.asStateFlow()

    private val _maxScore = MutableStateFlow(sharedPrefs.getFloat("max_score", 20f))
    val maxScore = _maxScore.asStateFlow()

    // Calculator State
    private val _currentSum = MutableStateFlow(0.0)
    val currentSum = _currentSum.asStateFlow()

    private val _operations = MutableStateFlow<List<Double>>(emptyList())
    val operations = _operations.asStateFlow()

    private val _lastAdded = MutableStateFlow<Double?>(null)
    val lastAdded = _lastAdded.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudentRepository(database.studentDao)
        students = repository.allStudents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Save Settings
    fun saveSettings(
        name: String,
        sub: String,
        inst: String,
        year: String,
        sem: String,
        max: Float
    ) {
        _teacherName.value = name
        _subject.value = sub
        _institution.value = inst
        _academicYear.value = year
        _semester.value = sem
        _maxScore.value = max

        sharedPrefs.edit()
            .putString("teacher_name", name)
            .putString("subject", sub)
            .putString("institution", inst)
            .putString("academic_year", year)
            .putString("semester", sem)
            .putFloat("max_score", max)
            .apply()
    }

    // Calculator Operations
    fun addValue(value: Double) {
        _currentSum.value = _currentSum.value + value
        _operations.value = _operations.value + value
        _lastAdded.value = value
    }

    fun undo() {
        val ops = _operations.value
        if (ops.isNotEmpty()) {
            val last = ops.last()
            _currentSum.value = maxOf(0.0, _currentSum.value - last)
            _operations.value = ops.dropLast(1)
            _lastAdded.value = if (_operations.value.isNotEmpty()) _operations.value.last() else null
        }
    }

    fun resetCalculator() {
        _currentSum.value = 0.0
        _operations.value = emptyList()
        _lastAdded.value = null
    }

    // Database Actions
    fun saveStudent(name: String) {
        val score = _currentSum.value
        val maxSc = _maxScore.value.toDouble()
        val sem = _semester.value
        val year = _academicYear.value
        val sub = _subject.value
        val inst = _institution.value

        viewModelScope.launch {
            val student = Student(
                name = name.trim().ifEmpty { "طالب غير معروف" },
                score = score,
                maxScore = maxSc,
                semester = sem,
                academicYear = year,
                subject = sub,
                institution = inst
            )
            repository.insert(student)
            resetCalculator() // Clear after save to be ready for next sheet
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.delete(student)
        }
    }

    fun clearSemester() {
        viewModelScope.launch {
            repository.deleteBySemester(_semester.value, _academicYear.value)
        }
    }

    fun clearYear() {
        viewModelScope.launch {
            repository.deleteByYear(_academicYear.value)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // Backup as JSON string
    fun exportBackupJson(): String {
        val list = students.value
        val root = JSONObject()
        val array = JSONArray()
        for (st in list) {
            val obj = JSONObject()
            obj.put("id", st.id)
            obj.put("name", st.name)
            obj.put("score", st.score)
            obj.put("maxScore", st.maxScore)
            obj.put("timestamp", st.timestamp)
            obj.put("semester", st.semester)
            obj.put("academicYear", st.academicYear)
            obj.put("subject", st.subject)
            obj.put("institution", st.institution)
            array.put(obj)
        }
        root.put("students", array)
        return root.toString(2)
    }

    // Restore from JSON string
    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val array = root.getJSONArray("students")
            val restoredList = mutableListOf<Student>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                restoredList.add(
                    Student(
                        name = obj.getString("name"),
                        score = obj.getDouble("score"),
                        maxScore = obj.getDouble("maxScore"),
                        timestamp = obj.getLong("timestamp"),
                        semester = obj.optString("semester", "1"),
                        academicYear = obj.optString("academicYear", "2025-2026"),
                        subject = obj.optString("subject", ""),
                        institution = obj.optString("institution", "")
                    )
                )
            }
            if (restoredList.isNotEmpty()) {
                viewModelScope.launch {
                    repository.deleteAll()
                    repository.insertAll(restoredList)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
