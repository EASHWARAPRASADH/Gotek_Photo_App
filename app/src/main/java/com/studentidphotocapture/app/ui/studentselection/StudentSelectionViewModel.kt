package com.studentidphotocapture.app.ui.studentselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentidphotocapture.app.data.model.Student
import com.studentidphotocapture.app.data.model.PhotoStatus
import com.studentidphotocapture.app.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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
    val errorMessage: String? = null,
    val isParentMode: Boolean = false,
    val isTeacherMode: Boolean = false,
    val parentMobile: String? = null,
    val assignedClass: String? = null,
    val assignedSection: String? = null
)

class StudentSelectionViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StudentSelectionUiState())
    val uiState: StateFlow<StudentSelectionUiState> = _uiState.asStateFlow()
    
    fun initMode(role: String?, parentMobile: String? = null, assignedClass: String? = null, assignedSection: String? = null) {
        val isParent = role == "PARENT"
        val isTeacher = role == "TEACHER"
        _uiState.value = _uiState.value.copy(
            isParentMode = isParent,
            isTeacherMode = isTeacher,
            parentMobile = parentMobile,
            assignedClass = if (isTeacher) assignedClass else null,
            assignedSection = if (isTeacher) assignedSection else null
        )
        
        loadMockData()
        
        if (isTeacher) {
            // Force assigned class/section for teachers
            selectSchool(_uiState.value.schools.firstOrNull() ?: "Bharathi Vidyalaya HSS")
            assignedClass?.let { selectClass(it) }
            assignedSection?.let { selectSection(it) }
        } else if (!isParent) {
            // Auto-load with defaults for admin/staff
            val state = _uiState.value
            selectSchool(state.schools.firstOrNull() ?: "Bharathi Vidyalaya HSS")
            selectClass(state.classes.firstOrNull() ?: "10")
            selectSection(state.sections.firstOrNull() ?: "A")
        } else {
            // Load students matching parent mobile
            loadStudents()
        }
    }
    
    private fun loadMockData() {
        _uiState.value = _uiState.value.copy(
            schools = listOf("Bharathi Vidyalaya HSS", "St. Mary's Matriculation", "Tagore Higher Sec. School"),
            classes = listOf("10", "11", "12"),
            sections = listOf("A", "B", "C")
        )
    }
    
    fun selectSchool(schoolCode: String) {
        _uiState.value = _uiState.value.copy(selectedSchool = schoolCode)
        loadStudents()
    }
    
    fun selectClass(classGrade: String) {
        _uiState.value = _uiState.value.copy(selectedClass = classGrade)
        loadStudents()
    }
    
    fun selectSection(section: String) {
        _uiState.value = _uiState.value.copy(selectedSection = section)
        loadStudents()
    }
    
    fun selectStudent(student: Student) {
        _uiState.value = _uiState.value.copy(selectedStudent = student)
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun searchStudents(query: String) {
        if (query.isEmpty()) {
            loadStudents()
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val state = _uiState.value
            if (state.isParentMode) {
                // If parent mode, filter search results to only show their children
                val mobile = state.parentMobile ?: ""
                studentRepository.getStudentsByParentMobile(mobile).collect { students ->
                    val filtered = students.filter { 
                        it.name.contains(query, ignoreCase = true) || 
                        it.admissionNumber.contains(query, ignoreCase = true) ||
                        it.rollNumber.contains(query, ignoreCase = true)
                    }
                    _uiState.value = _uiState.value.copy(
                        students = filtered,
                        isLoading = false
                    )
                }
            } else {
                val schoolCode = state.selectedSchool
                val classGrade = state.selectedClass
                val section = state.selectedSection
                
                if (schoolCode != null && classGrade != null && section != null) {
                    // Filter within selected class and section
                    studentRepository.getStudentsByClassSection(schoolCode, classGrade, section).collect { students ->
                        val filtered = students.filter {
                            it.name.contains(query, ignoreCase = true) ||
                            it.admissionNumber.contains(query, ignoreCase = true) ||
                            it.rollNumber.contains(query, ignoreCase = true)
                        }
                        _uiState.value = _uiState.value.copy(
                            students = filtered,
                            isLoading = false
                        )
                    }
                } else {
                    // Global search fallback
                    studentRepository.searchStudents(query).collect { students ->
                        _uiState.value = _uiState.value.copy(
                            students = students,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
    
    fun searchByAdmissionNumber(admissionNumber: String) {
        if (admissionNumber.isEmpty()) {
            loadStudents()
            return
        }
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val student = studentRepository.getStudentByAdmissionNumber(admissionNumber)
                _uiState.value = _uiState.value.copy(
                    students = if (student != null) listOf(student) else emptyList(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to search by admission number: ${e.message}"
                )
            }
        }
    }
    
    private fun loadStudents() {
        val state = _uiState.value
        
        searchJob?.cancel()
        if (state.isParentMode) {
            val mobile = state.parentMobile ?: return
            searchJob = viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                try {
                    // First, clear out any accidentally generated duplicate dummy children from previous builds/actions
                    studentRepository.clearDuplicateMockParentStudents(mobile)
                    
                    studentRepository.getStudentsByParentMobile(mobile).collect { students ->
                        if (students.isEmpty()) {
                            // Add a mock student for the parent if none exists
                            // Use same ID format as generateMockStudents to avoid duplicates
                             val mockStudent = Student(
                                id = "Bharathi Vidyalaya HSS-10-A-1",
                                name = "Karthik Raja",
                                rollNumber = "001",
                                admissionNumber = "1001",
                                classGrade = "10",
                                section = "A",
                                schoolCode = "Bharathi Vidyalaya HSS",
                                parentMobile = mobile,
                                photoStatus = PhotoStatus.PENDING
                            )
                            studentRepository.insertStudent(mockStudent)
                            _uiState.value = _uiState.value.copy(
                                students = listOf(mockStudent),
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
                        errorMessage = "Error loading your children: ${e.message}"
                    )
                }
            }
            return
        }

        val schoolCode = state.selectedSchool ?: return
        val classGrade = state.selectedClass ?: return
        val section = state.selectedSection ?: return
        
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                studentRepository.getStudentsByClassSection(schoolCode, classGrade, section)
                    .collect { students ->
                        if (students.size < 10 && !state.isParentMode) {
                            val mockStudents = generateMockStudents(schoolCode, classGrade, section)
                            mockStudents.forEach { studentRepository.insertStudent(it) }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            students = students,
                            isLoading = false
                        )
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
            "Karthik Raja", "Anitha Selvam", "Arun Prasath", "Priya Sundaram", 
            "Senthil Kumar", "Divya Ramachandran", "Venkatesh Prasad", "Meenakshi Sundaram",
            "Ranjith Kumar", "Kavitha Devi", "Sanjay Raghavan", "Sandhiya Murugan",
            "Hariharan Raju", "Abirami Balakrishnan", "Vijay Chandrasekhar", "Deepika Rajendran",
            "Manoj Prabhakar", "Yamini Ganesan", "Suresh Kumar", "Nandhini Palanisamy"
        )
        
        names.forEachIndexed { index, name ->
            val status = if (index % 5 == 0) PhotoStatus.CAPTURED else PhotoStatus.PENDING
            students.add(
                Student(
                    id = "$schoolCode-$classGrade-$section-${index + 1}",
                    name = name,
                    rollNumber = String.format("%03d", index + 1),
                    admissionNumber = String.format("%04d", 1000 + index + 1),
                    classGrade = classGrade,
                    section = section,
                    schoolCode = schoolCode,
                    parentMobile = if (index == 0 && classGrade == "10" && section == "A") "9876543210" else null, // Link only one specific student to our test parent
                    photoStatus = status,
                    photoCapturedAt = if (status == PhotoStatus.CAPTURED) System.currentTimeMillis() else null
                )
            )
        }
        
        return students
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
