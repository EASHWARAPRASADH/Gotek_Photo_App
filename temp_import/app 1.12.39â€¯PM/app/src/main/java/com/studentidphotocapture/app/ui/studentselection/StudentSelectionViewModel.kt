package com.studentidphotocapture.app.ui.studentselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentidphotocapture.app.data.model.Student
import com.studentidphotocapture.app.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentSelectionUiState(
    val isLoading: Boolean = false,
    val schools: List<String> = emptyList(),
    val classes: List<String> = emptyList(),
    val sections: List<String> = emptyList(),
    val students: List<Student> = emptyList(),
    val selectedSchool: String? = null,
    val selectedClass: String? = null,
    val selectedSection: String? = null,
    val selectedStudent: Student? = null,
    val errorMessage: String? = null
)

class StudentSelectionViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StudentSelectionUiState())
    val uiState: StateFlow<StudentSelectionUiState> = _uiState.asStateFlow()
    
    init {
        loadMockData()
    }
    
    private fun loadMockData() {
        _uiState.value = _uiState.value.copy(
            schools = listOf("SCH01", "SCH02", "SCH03"),
            classes = listOf("10A", "10B", "11A", "11B", "12A", "12B"),
            sections = listOf("A", "B", "C")
        )
    }
    
    fun selectSchool(schoolCode: String) {
        _uiState.value = _uiState.value.copy(selectedSchool = schoolCode)
    }
    
    fun selectClass(classGrade: String) {
        _uiState.value = _uiState.value.copy(selectedClass = classGrade)
    }
    
    fun selectSection(section: String) {
        _uiState.value = _uiState.value.copy(selectedSection = section)
        loadStudents()
    }
    
    fun selectStudent(student: Student) {
        _uiState.value = _uiState.value.copy(selectedStudent = student)
    }
    
    private fun loadStudents() {
        val schoolCode = _uiState.value.selectedSchool ?: return
        val classGrade = _uiState.value.selectedClass ?: return
        val section = _uiState.value.selectedSection ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // First check if students already exist in database
                val existingStudents = studentRepository.getStudentsByClassSection(schoolCode, classGrade, section)
                    .collect { students ->
                        if (students.isEmpty()) {
                            // Generate mock students for demo only if none exist
                            val mockStudents = generateMockStudents(schoolCode, classGrade, section)
                            mockStudents.forEach { studentRepository.insertStudent(it) }
                            _uiState.value = _uiState.value.copy(
                                students = mockStudents,
                                isLoading = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                students = students,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load students: ${e.message}"
                )
            }
        }
    }
    
    private fun generateMockStudents(schoolCode: String, classGrade: String, section: String): List<Student> {
        val students = mutableListOf<Student>()
        val names = listOf(
            "John Doe", "Jane Smith", "Michael Johnson", "Emily Brown",
            "David Wilson", "Sarah Davis", "James Miller", "Lisa Anderson",
            "Robert Taylor", "Mary Thomas", "William Jackson", "Patricia White",
            "Richard Harris", "Jennifer Martin", "Joseph Thompson", "Linda Garcia",
            "Thomas Martinez", "Elizabeth Robinson", "Charles Clark", "Barbara Rodriguez"
        )
        
        names.forEachIndexed { index, name ->
            students.add(
                Student(
                    id = "$schoolCode-$classGrade-$section-${index + 1}",
                    name = name,
                    rollNumber = String.format("%03d", index + 1),
                    classGrade = classGrade,
                    section = section,
                    schoolCode = schoolCode
                )
            )
        }
        
        return students
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
