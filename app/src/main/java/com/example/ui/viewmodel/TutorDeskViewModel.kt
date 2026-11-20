package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Batch
import com.example.data.model.BatchStatus
import com.example.data.model.FeePayment
import com.example.data.model.FeeStatus
import com.example.data.model.HscLevel
import com.example.data.model.PaymentMethod
import com.example.data.model.Student
import com.example.data.model.StudentFeeSummary
import com.example.data.model.StudentStatus
import com.example.data.repository.TutorDeskRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class TutorTab {
    BATCHES,
    ATTENDANCE,
    FEES,
    STUDENTS
}

@OptIn(ExperimentalCoroutinesApi::class)
class TutorDeskViewModel(
    private val repository: TutorDeskRepository,
    val currentUserId: String
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(TutorTab.BATCHES)
    val selectedTab: StateFlow<TutorTab> = _selectedTab.asStateFlow()

    private val _selectedDateIso = MutableStateFlow(todayIso())
    val selectedDateIso: StateFlow<String> = _selectedDateIso.asStateFlow()

    private val _selectedMonthKey = MutableStateFlow(currentMonthKey())
    val selectedMonthKey: StateFlow<String> = _selectedMonthKey.asStateFlow()

    private val _selectedAttendanceBatchId = MutableStateFlow<String?>(null)
    val selectedAttendanceBatchId: StateFlow<String?> = _selectedAttendanceBatchId.asStateFlow()

    private val _draftAttendance = MutableStateFlow<Map<String, AttendanceStatus>>(emptyMap())
    val draftAttendance: StateFlow<Map<String, AttendanceStatus>> = _draftAttendance.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    val batchesState: StateFlow<UiState<List<Batch>>> = repository.observeBatches(currentUserId)
        .map<List<Batch>, UiState<List<Batch>>> { UiState.Success(it) }
        .catch { error -> emit(unwrapFirestoreError(error, "Failed to load batches")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val studentsState: StateFlow<UiState<List<Student>>> = repository.observeStudents(currentUserId)
        .map<List<Student>, UiState<List<Student>>> { UiState.Success(it) }
        .catch { error -> emit(unwrapFirestoreError(error, "Failed to load students")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val attendanceState: StateFlow<UiState<List<AttendanceRecord>>> = _selectedDateIso
        .flatMapLatest { dateIso ->
            repository.observeAttendanceForDate(currentUserId, dateIso)
                .map<List<AttendanceRecord>, UiState<List<AttendanceRecord>>> { UiState.Success(it) }
                .catch { error -> emit(unwrapFirestoreError(error, "Failed to load attendance")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val feesState: StateFlow<UiState<List<FeePayment>>> = _selectedMonthKey
        .flatMapLatest { monthKey ->
            repository.observeFeesForMonth(currentUserId, monthKey)
                .map<List<FeePayment>, UiState<List<FeePayment>>> { UiState.Success(it) }
                .catch { error -> emit(unwrapFirestoreError(error, "Failed to load fee ledger")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private fun unwrapFirestoreError(error: Throwable, fallbackMessage: String): UiState.Error {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w(TAG, fallbackMessage, root)
        return UiState.Error(root.localizedMessage ?: fallbackMessage)
    }

    fun selectTab(tab: TutorTab) {
        _selectedTab.value = tab
    }

    fun openAttendanceForBatch(batchId: String) {
        _selectedAttendanceBatchId.value = batchId
        _draftAttendance.value = emptyMap()
        _selectedTab.value = TutorTab.ATTENDANCE
    }

    fun selectAttendanceBatch(batchId: String) {
        _selectedAttendanceBatchId.value = batchId
        _draftAttendance.value = emptyMap()
    }

    fun selectAttendanceDate(dateIso: String) {
        _selectedDateIso.value = dateIso
        _draftAttendance.value = emptyMap()
    }

    fun selectFeeMonth(monthKey: String) {
        _selectedMonthKey.value = monthKey
    }

    fun setDraftAttendance(studentId: String, status: AttendanceStatus) {
        _draftAttendance.value = _draftAttendance.value + (studentId to status)
    }

    fun markAllDraftPresent(studentsInBatch: List<Student>) {
        val updated = _draftAttendance.value.toMutableMap()
        for (student in studentsInBatch) {
            updated[student.id] = AttendanceStatus.PRESENT
        }
        _draftAttendance.value = updated
    }

    fun saveAttendanceRollCall(
        batchId: String,
        studentsInBatch: List<Student>,
        savedRecords: List<AttendanceRecord>
    ) {
        val dateIso = _selectedDateIso.value
        val savedByStudent = savedRecords
            .filter { it.batchId == batchId }
            .associate { it.studentId to AttendanceStatus.fromRaw(it.status) }
        val drafts = _draftAttendance.value
        val entries = studentsInBatch.mapNotNull { student ->
            val chosen = drafts[student.id] ?: savedByStudent[student.id]
            if (chosen != null) student to chosen else null
        }
        if (entries.isEmpty()) {
            _snackbarMessages.tryEmit("Tap P, L, or A on at least one student first.")
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveAttendanceBatch(currentUserId, batchId, dateIso, entries)
            _isSaving.value = false
            result.onSuccess {
                _draftAttendance.value = emptyMap()
                _snackbarMessages.tryEmit("Roll call saved for ${entries.size} student${if (entries.size == 1) "" else "s"}.")
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Failed to save attendance.")
            }
        }
    }

    fun saveBatch(
        batchId: String?,
        name: String,
        hscLevel: HscLevel,
        scheduleDays: String,
        scheduleTime: String,
        room: String,
        currentTopic: String,
        monthlyFeeBdt: Long,
        maxCapacity: Long,
        status: BatchStatus,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveBatch(
                userId = currentUserId,
                batchId = batchId,
                name = name,
                hscLevel = hscLevel,
                scheduleDays = scheduleDays,
                scheduleTime = scheduleTime,
                room = room,
                currentTopic = currentTopic,
                monthlyFeeBdt = monthlyFeeBdt,
                maxCapacity = maxCapacity,
                status = status
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarMessages.tryEmit(if (batchId == null) "Batch created." else "Batch updated.")
                onDone()
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Could not save batch.")
            }
        }
    }

    fun deleteBatch(batchId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.deleteBatch(currentUserId, batchId)
            _isSaving.value = false
            result.onSuccess {
                _snackbarMessages.tryEmit("Batch deleted.")
                onDone()
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Could not delete batch.")
            }
        }
    }

    fun saveStudent(
        studentId: String?,
        batchId: String,
        batchName: String,
        name: String,
        college: String,
        rollNumber: String,
        guardianPhone: String,
        monthlyFeeBdt: Long,
        status: StudentStatus,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveStudent(
                userId = currentUserId,
                studentId = studentId,
                batchId = batchId,
                batchName = batchName,
                name = name,
                college = college,
                rollNumber = rollNumber,
                guardianPhone = guardianPhone,
                monthlyFeeBdt = monthlyFeeBdt,
                status = status
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarMessages.tryEmit(if (studentId == null) "Student added." else "Student updated.")
                onDone()
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Could not save student.")
            }
        }
    }

    fun deleteStudent(studentId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.deleteStudent(currentUserId, studentId)
            _isSaving.value = false
            result.onSuccess {
                _snackbarMessages.tryEmit("Student removed.")
                onDone()
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Could not remove student.")
            }
        }
    }

    fun recordFeePayment(
        student: Student,
        monthKey: String,
        paidAmountBdt: Long,
        paymentMethod: PaymentMethod,
        note: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.recordFeePayment(
                userId = currentUserId,
                student = student,
                monthKey = monthKey,
                paidAmountBdt = paidAmountBdt,
                paymentMethod = paymentMethod,
                note = note
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarMessages.tryEmit("Payment updated for ${student.name}.")
                onDone()
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Could not record payment.")
            }
        }
    }

    fun loadStarterDataset() {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.seedStarterDataset(currentUserId)
            _isSaving.value = false
            result.onSuccess {
                _snackbarMessages.tryEmit("Starter HSC ICT batches loaded.")
            }.onFailure { err ->
                _snackbarMessages.tryEmit(err.localizedMessage ?: "Could not load starter batches.")
            }
        }
    }

    fun computeFeeSummaries(
        students: List<Student>,
        payments: List<FeePayment>,
        monthKey: String
    ): List<StudentFeeSummary> {
        val paymentByStudent = payments.associateBy { it.studentId }
        return students
            .filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
            .map { student ->
                val payment = paymentByStudent[student.id]
                val expected = (payment?.expectedFeeBdt ?: student.monthlyFeeBdt).coerceAtLeast(0L)
                val paid = (payment?.paidAmountBdt ?: 0L).coerceAtLeast(0L)
                val due = (expected - paid).coerceAtLeast(0L)
                val status = when {
                    expected == 0L || paid >= expected -> FeeStatus.PAID
                    paid > 0L -> FeeStatus.PARTIAL
                    else -> FeeStatus.DUE
                }
                StudentFeeSummary(
                    student = student,
                    monthKey = monthKey,
                    expectedFeeBdt = expected,
                    paidAmountBdt = paid,
                    dueAmountBdt = due,
                    status = status,
                    paymentMethod = payment?.paymentMethod ?: PaymentMethod.CASH.label,
                    note = payment?.note ?: "",
                    existingPayment = payment
                )
            }
    }

    companion object {
        private const val TAG = "TutorDeskVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        fun currentMonthKey(): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

        fun recentDateOptions(daysBack: Int = 6): List<Pair<String, String>> {
            val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val labelFmt = SimpleDateFormat("EEE d", Locale.US)
            val cal = Calendar.getInstance()
            val list = mutableListOf<Pair<String, String>>()
            for (i in 0..daysBack) {
                val copy = cal.clone() as Calendar
                copy.add(Calendar.DAY_OF_MONTH, -i)
                val iso = isoFmt.format(copy.time)
                val label = if (i == 0) "Today" else labelFmt.format(copy.time)
                list.add(iso to label)
            }
            return list
        }

        fun recentMonthOptions(monthsBack: Int = 5): List<Pair<String, String>> {
            val keyFmt = SimpleDateFormat("yyyy-MM", Locale.US)
            val labelFmt = SimpleDateFormat("MMM yyyy", Locale.US)
            val cal = Calendar.getInstance()
            val list = mutableListOf<Pair<String, String>>()
            for (i in 0..monthsBack) {
                val copy = cal.clone() as Calendar
                copy.add(Calendar.MONTH, -i)
                list.add(keyFmt.format(copy.time) to labelFmt.format(copy.time))
            }
            return list
        }
    }
}
