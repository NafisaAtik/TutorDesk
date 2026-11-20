package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firestore.OperationType
import com.example.data.firestore.handleFirestoreError
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Batch
import com.example.data.model.BatchStatus
import com.example.data.model.HscLevel
import com.example.data.model.PaymentMethod
import com.example.data.model.Student
import com.example.data.model.StudentStatus
import com.example.data.model.FeePayment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TutorDeskRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please sign in with Google before saving changes.")
        check(uid == expectedUserId) { "Signed-in account changed; please try again." }
        return uid
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    fun observeBatches(userId: String): Flow<List<Batch>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/batches"
        emitAll(
            userDoc(userId).collection("batches")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Batch::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.name.lowercase(Locale.getDefault()) }
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                }
        )
    }

    fun observeStudents(userId: String): Flow<List<Student>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/students"
        emitAll(
            userDoc(userId).collection("students")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Student::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.name.lowercase(Locale.getDefault()) }
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                }
        )
    }

    fun observeAttendanceForDate(userId: String, dateIso: String): Flow<List<AttendanceRecord>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/attendance"
        emitAll(
            userDoc(userId).collection("attendance")
                .whereEqualTo("dateIso", dateIso)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AttendanceRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                }
        )
    }

    fun observeFeesForMonth(userId: String, monthKey: String): Flow<List<FeePayment>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/feePayments"
        emitAll(
            userDoc(userId).collection("feePayments")
                .whereEqualTo("monthKey", monthKey)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FeePayment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                }
        )
    }

    private fun handleFlowError(
        error: Throwable,
        operationType: OperationType,
        path: String,
        userId: String
    ): Nothing {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, operationType, path, auth)
        }
        throw error
    }

    suspend fun saveBatch(
        userId: String,
        batchId: String?,
        name: String,
        hscLevel: HscLevel,
        scheduleDays: String,
        scheduleTime: String,
        room: String,
        currentTopic: String,
        monthlyFeeBdt: Long,
        maxCapacity: Long,
        status: BatchStatus
    ): Result<Unit> {
        var docPath = "users/$userId/batches"
        var op = if (batchId.isNullOrBlank()) OperationType.CREATE else OperationType.UPDATE
        return try {
            val uid = requireUserId(userId)
            val trimmedName = name.trim()
            val trimmedDays = scheduleDays.trim()
            val trimmedTime = scheduleTime.trim()
            val trimmedRoom = room.trim()
            val trimmedTopic = currentTopic.trim()

            require(trimmedName.length in 1..80) { "Batch name must be 1–80 characters." }
            require(trimmedDays.length in 1..60) { "Schedule days must be 1–60 characters." }
            require(trimmedTime.length in 1..30) { "Class time must be 1–30 characters." }
            require(trimmedRoom.length <= 60) { "Room label must be at most 60 characters." }
            require(trimmedTopic.length <= 120) { "Topic must be at most 120 characters." }
            require(monthlyFeeBdt in 0L..100_000L) { "Monthly fee must be between ৳ 0 and ৳ 100,000." }
            require(maxCapacity in 1L..500L) { "Capacity must be between 1 and 500 students." }

            val colRef = userDoc(uid).collection("batches")
            if (batchId.isNullOrBlank()) {
                val docRef = colRef.document()
                docPath = docRef.path
                val payload = mapOf(
                    "userId" to uid,
                    "name" to trimmedName,
                    "hscLevel" to hscLevel.label,
                    "scheduleDays" to trimmedDays,
                    "scheduleTime" to trimmedTime,
                    "room" to trimmedRoom,
                    "currentTopic" to trimmedTopic,
                    "monthlyFeeBdt" to monthlyFeeBdt,
                    "maxCapacity" to maxCapacity,
                    "status" to status.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                op = OperationType.UPDATE
                val docRef = colRef.document(batchId)
                docPath = docRef.path
                val updatePayload = mapOf(
                    "name" to trimmedName,
                    "hscLevel" to hscLevel.label,
                    "scheduleDays" to trimmedDays,
                    "scheduleTime" to trimmedTime,
                    "room" to trimmedRoom,
                    "currentTopic" to trimmedTopic,
                    "monthlyFeeBdt" to monthlyFeeBdt,
                    "maxCapacity" to maxCapacity,
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, op, docPath, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteBatch(userId: String, batchId: String): Result<Unit> {
        var docPath = "users/$userId/batches/$batchId"
        return try {
            val uid = requireUserId(userId)
            require(batchId.isNotBlank()) { "Batch ID is required." }
            val docRef = userDoc(uid).collection("batches").document(batchId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath, auth)
            Result.failure(e)
        }
    }

    suspend fun saveStudent(
        userId: String,
        studentId: String?,
        batchId: String,
        batchName: String,
        name: String,
        college: String,
        rollNumber: String,
        guardianPhone: String,
        monthlyFeeBdt: Long,
        status: StudentStatus
    ): Result<Unit> {
        var docPath = "users/$userId/students"
        val op = if (studentId.isNullOrBlank()) OperationType.CREATE else OperationType.UPDATE
        return try {
            val uid = requireUserId(userId)
            val trimmedName = name.trim()
            val trimmedBatchName = batchName.trim()
            val trimmedCollege = college.trim()
            val trimmedRoll = rollNumber.trim()
            val trimmedPhone = guardianPhone.trim()

            require(batchId.isNotBlank()) { "Please select a batch." }
            require(trimmedBatchName.length in 1..80) { "Batch name is invalid." }
            require(trimmedName.length in 1..80) { "Student name must be 1–80 characters." }
            require(trimmedCollege.length <= 100) { "College name must be at most 100 characters." }
            require(trimmedRoll.length <= 30) { "Roll number must be at most 30 characters." }
            require(trimmedPhone.length <= 25) { "Guardian phone must be at most 25 characters." }
            require(monthlyFeeBdt in 0L..100_000L) { "Monthly fee must be between ৳ 0 and ৳ 100,000." }

            val colRef = userDoc(uid).collection("students")
            if (studentId.isNullOrBlank()) {
                val docRef = colRef.document()
                docPath = docRef.path
                val payload = mapOf(
                    "userId" to uid,
                    "batchId" to batchId,
                    "batchName" to trimmedBatchName,
                    "name" to trimmedName,
                    "college" to trimmedCollege,
                    "rollNumber" to trimmedRoll,
                    "guardianPhone" to trimmedPhone,
                    "monthlyFeeBdt" to monthlyFeeBdt,
                    "status" to status.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val docRef = colRef.document(studentId)
                docPath = docRef.path
                val updatePayload = mapOf(
                    "batchId" to batchId,
                    "batchName" to trimmedBatchName,
                    "name" to trimmedName,
                    "college" to trimmedCollege,
                    "rollNumber" to trimmedRoll,
                    "guardianPhone" to trimmedPhone,
                    "monthlyFeeBdt" to monthlyFeeBdt,
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, op, docPath, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteStudent(userId: String, studentId: String): Result<Unit> {
        var docPath = "users/$userId/students/$studentId"
        return try {
            val uid = requireUserId(userId)
            require(studentId.isNotBlank()) { "Student ID is required." }
            val docRef = userDoc(uid).collection("students").document(studentId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath, auth)
            Result.failure(e)
        }
    }

    suspend fun saveAttendanceBatch(
        userId: String,
        batchId: String,
        dateIso: String,
        entries: List<Pair<Student, AttendanceStatus>>
    ): Result<Unit> {
        val path = "users/$userId/attendance"
        return try {
            val uid = requireUserId(userId)
            require(batchId.isNotBlank()) { "Batch ID is required." }
            require(dateIso.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) { "Invalid date format." }
            if (entries.isEmpty()) return Result.success(Unit)

            val colRef = userDoc(uid).collection("attendance")
            val existingDocs = colRef
                .whereEqualTo("batchId", batchId)
                .whereEqualTo("dateIso", dateIso)
                .get()
                .await()
                .documents
                .map { it.id }
                .toSet()

            val writeBatch = db.batch()
            for ((student, status) in entries) {
                val docId = "${batchId}_${dateIso}_${student.id}".take(128)
                val docRef = colRef.document(docId)
                val safeName = student.name.trim().ifBlank { "Student" }.take(80)
                if (docId in existingDocs) {
                    writeBatch.update(
                        docRef,
                        mapOf(
                            "studentName" to safeName,
                            "status" to status.name,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    writeBatch.set(
                        docRef,
                        mapOf(
                            "userId" to uid,
                            "batchId" to batchId,
                            "studentId" to student.id,
                            "studentName" to safeName,
                            "dateIso" to dateIso,
                            "status" to status.name,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            writeBatch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun recordFeePayment(
        userId: String,
        student: Student,
        monthKey: String,
        paidAmountBdt: Long,
        paymentMethod: PaymentMethod,
        note: String
    ): Result<Unit> {
        val docId = "${monthKey}_${student.id}".take(128)
        var docPath = "users/$userId/feePayments/$docId"
        return try {
            val uid = requireUserId(userId)
            require(monthKey.matches(Regex("^[0-9]{4}-[0-9]{2}$"))) { "Invalid month format." }
            require(student.id.isNotBlank() && student.batchId.isNotBlank()) { "Student and batch are required." }
            require(paidAmountBdt in 0L..100_000L) { "Payment amount must be between ৳ 0 and ৳ 100,000." }
            val trimmedNote = note.trim()
            require(trimmedNote.length <= 200) { "Note must be at most 200 characters." }

            val expectedFee = student.monthlyFeeBdt.coerceIn(0L, 100_000L)
            val docRef = userDoc(uid).collection("feePayments").document(docId)
            docPath = docRef.path

            val existingSnap = docRef.get().await()
            val safeStudentName = student.name.trim().ifBlank { "Student" }.take(80)
            val safeBatchName = student.batchName.trim().ifBlank { "Batch" }.take(80)

            if (existingSnap.exists()) {
                docRef.update(
                    mapOf(
                        "studentName" to safeStudentName,
                        "batchId" to student.batchId,
                        "batchName" to safeBatchName,
                        "expectedFeeBdt" to expectedFee,
                        "paidAmountBdt" to paidAmountBdt,
                        "paymentMethod" to paymentMethod.label,
                        "note" to trimmedNote,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "studentId" to student.id,
                        "studentName" to safeStudentName,
                        "batchId" to student.batchId,
                        "batchName" to safeBatchName,
                        "monthKey" to monthKey,
                        "expectedFeeBdt" to expectedFee,
                        "paidAmountBdt" to paidAmountBdt,
                        "paymentMethod" to paymentMethod.label,
                        "note" to trimmedNote,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath, auth)
            Result.failure(e)
        }
    }

    suspend fun seedStarterDataset(userId: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val monthKey = todayIso.substring(0, 7)

            val batchesCol = userDoc(uid).collection("batches")
            val studentsCol = userDoc(uid).collection("students")
            val attendanceCol = userDoc(uid).collection("attendance")
            val feesCol = userDoc(uid).collection("feePayments")

            val b1Ref = batchesCol.document("hsc26_morning")
            val b2Ref = batchesCol.document("xi_evening_logic")

            val setupBatch = db.batch()
            setupBatch.set(
                b1Ref,
                mapOf(
                    "userId" to uid,
                    "name" to "HSC-26 Morning ICT",
                    "hscLevel" to HscLevel.XII.label,
                    "scheduleDays" to "Sat · Mon · Wed",
                    "scheduleTime" to "08:00 AM",
                    "room" to "Mirpur-10 Room 2",
                    "currentTopic" to "Ch 3: Number Systems & Logic Gates",
                    "monthlyFeeBdt" to 1500L,
                    "maxCapacity" to 25L,
                    "status" to BatchStatus.ACTIVE.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            setupBatch.set(
                b2Ref,
                mapOf(
                    "userId" to uid,
                    "name" to "Class XI Evening Web & C",
                    "hscLevel" to HscLevel.XI.label,
                    "scheduleDays" to "Sun · Tue · Thu",
                    "scheduleTime" to "04:30 PM",
                    "room" to "Dhanmondi Lab 1",
                    "currentTopic" to "Ch 4: HTML Tables & Hyperlinks",
                    "monthlyFeeBdt" to 1200L,
                    "maxCapacity" to 20L,
                    "status" to BatchStatus.ACTIVE.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            setupBatch.commit().await()

            data class SeedStudent(
                val id: String,
                val batchId: String,
                val batchName: String,
                val name: String,
                val college: String,
                val roll: String,
                val phone: String,
                val fee: Long,
                val att: AttendanceStatus,
                val paid: Long,
                val method: PaymentMethod,
                val note: String
            )

            val seeds = listOf(
                SeedStudent("stu_rafid", b1Ref.id, "HSC-26 Morning ICT", "Rafid Al Mahmud", "Notre Dame College", "101", "01711-234501", 1500L, AttendanceStatus.PRESENT, 1500L, PaymentMethod.BKASH, "Paid via bKash"),
                SeedStudent("stu_tasnim", b1Ref.id, "HSC-26 Morning ICT", "Tasnim Tabassum", "Viqarunnisa Noon College", "102", "01819-445502", 1500L, AttendanceStatus.PRESENT, 1000L, PaymentMethod.CASH, "৳ 500 due next class"),
                SeedStudent("stu_farhan", b1Ref.id, "HSC-26 Morning ICT", "Farhan Sadik", "Dhaka College", "103", "01911-887703", 1500L, AttendanceStatus.LATE, 0L, PaymentMethod.CASH, ""),
                SeedStudent("stu_mehzabin", b2Ref.id, "Class XI Evening Web & C", "Mehzabin Chowdhury", "Holy Cross College", "201", "01670-332204", 1200L, AttendanceStatus.PRESENT, 1200L, PaymentMethod.NAGAD, "Cleared on 5th"),
                SeedStudent("stu_saad", b2Ref.id, "Class XI Evening Web & C", "Saad Ibn Tariq", "Rajuk Uttara Model College", "202", "01755-669905", 1200L, AttendanceStatus.ABSENT, 0L, PaymentMethod.CASH, "")
            )

            val studentBatch = db.batch()
            for (s in seeds) {
                studentBatch.set(
                    studentsCol.document(s.id),
                    mapOf(
                        "userId" to uid,
                        "batchId" to s.batchId,
                        "batchName" to s.batchName,
                        "name" to s.name,
                        "college" to s.college,
                        "rollNumber" to s.roll,
                        "guardianPhone" to s.phone,
                        "monthlyFeeBdt" to s.fee,
                        "status" to StudentStatus.ACTIVE.name,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            studentBatch.commit().await()

            val ledgerBatch = db.batch()
            for (s in seeds) {
                val attId = "${s.batchId}_${todayIso}_${s.id}".take(128)
                ledgerBatch.set(
                    attendanceCol.document(attId),
                    mapOf(
                        "userId" to uid,
                        "batchId" to s.batchId,
                        "studentId" to s.id,
                        "studentName" to s.name,
                        "dateIso" to todayIso,
                        "status" to s.att.name,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (s.paid > 0L) {
                    val feeId = "${monthKey}_${s.id}".take(128)
                    ledgerBatch.set(
                        feesCol.document(feeId),
                        mapOf(
                            "userId" to uid,
                            "studentId" to s.id,
                            "studentName" to s.name,
                            "batchId" to s.batchId,
                            "batchName" to s.batchName,
                            "monthKey" to monthKey,
                            "expectedFeeBdt" to s.fee,
                            "paidAmountBdt" to s.paid,
                            "paymentMethod" to s.method.label,
                            "note" to s.note,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            ledgerBatch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }
}
