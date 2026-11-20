package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
enum class HscLevel(val label: String) {
    XI("XI"),
    XII("XII"),
    HSC_REVISION("HSC Revision");

    companion object {
        fun fromRaw(raw: String?): HscLevel =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: XII
    }
}

@Keep
enum class BatchStatus {
    ACTIVE,
    ARCHIVED;

    companion object {
        fun fromRaw(raw: String?): BatchStatus =
            entries.firstOrNull { it.name == raw } ?: ACTIVE
    }
}

@Keep
enum class StudentStatus {
    ACTIVE,
    PAUSED;

    companion object {
        fun fromRaw(raw: String?): StudentStatus =
            entries.firstOrNull { it.name == raw } ?: ACTIVE
    }
}

@Keep
enum class AttendanceStatus(val shortLabel: String, val displayLabel: String) {
    PRESENT("P", "Present"),
    LATE("L", "Late"),
    ABSENT("A", "Absent");

    companion object {
        fun fromRaw(raw: String?): AttendanceStatus? =
            entries.firstOrNull { it.name == raw }
    }
}

@Keep
enum class FeeStatus(val label: String) {
    PAID("Paid"),
    PARTIAL("Partial"),
    DUE("Due")
}

@Keep
enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    BKASH("bKash"),
    NAGAD("Nagad"),
    ROCKET("Rocket"),
    BANK("Bank");

    companion object {
        fun fromRaw(raw: String?): PaymentMethod =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: CASH
    }
}

@Keep
@IgnoreExtraProperties
data class Batch(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val hscLevel: String = HscLevel.XII.label,
    val scheduleDays: String = "",
    val scheduleTime: String = "",
    val room: String = "",
    val currentTopic: String = "",
    val monthlyFeeBdt: Long = 1500L,
    val maxCapacity: Long = 25L,
    val status: String = BatchStatus.ACTIVE.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Student(
    @DocumentId val id: String = "",
    val userId: String = "",
    val batchId: String = "",
    val batchName: String = "",
    val name: String = "",
    val college: String = "",
    val rollNumber: String = "",
    val guardianPhone: String = "",
    val monthlyFeeBdt: Long = 1500L,
    val status: String = StudentStatus.ACTIVE.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AttendanceRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val batchId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val dateIso: String = "",
    val status: String = AttendanceStatus.PRESENT.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FeePayment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val batchId: String = "",
    val batchName: String = "",
    val monthKey: String = "",
    val expectedFeeBdt: Long = 0L,
    val paidAmountBdt: Long = 0L,
    val paymentMethod: String = PaymentMethod.CASH.label,
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class StudentFeeSummary(
    val student: Student,
    val monthKey: String,
    val expectedFeeBdt: Long,
    val paidAmountBdt: Long,
    val dueAmountBdt: Long,
    val status: FeeStatus,
    val paymentMethod: String,
    val note: String,
    val existingPayment: FeePayment?
)
