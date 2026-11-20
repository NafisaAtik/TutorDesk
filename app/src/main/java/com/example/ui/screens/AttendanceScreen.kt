package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Batch
import com.example.data.model.Student
import com.example.data.model.StudentStatus
import com.example.ui.components.FilterPillChip
import com.example.ui.components.SegmentedAttendanceToggle
import com.example.ui.components.TutorDeskCard
import com.example.ui.components.TutorDeskPrimaryButton
import com.example.ui.theme.CoralBg
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralText
import com.example.ui.theme.DeepSlateNavy
import com.example.ui.theme.EmeraldBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldText
import com.example.ui.theme.InteractiveBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceLow
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.AmberBg
import com.example.ui.theme.AmberText
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.TutorDeskViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun AttendanceScreen(
    batchesState: UiState<List<Batch>>,
    studentsState: UiState<List<Student>>,
    attendanceState: UiState<List<AttendanceRecord>>,
    selectedBatchId: String?,
    selectedDateIso: String,
    draftAttendance: Map<String, AttendanceStatus>,
    isSaving: Boolean,
    onSelectBatch: (String) -> Unit,
    onSelectDate: (String) -> Unit,
    onToggleStudentStatus: (String, AttendanceStatus) -> Unit,
    onMarkAllPresent: (List<Student>) -> Unit,
    onSaveRollCall: (batchId: String, studentsInBatch: List<Student>, savedRecords: List<AttendanceRecord>) -> Unit
) {
    val batches = (batchesState as? UiState.Success)?.data ?: emptyList()
    val students = (studentsState as? UiState.Success)?.data ?: emptyList()
    val savedRecords = (attendanceState as? UiState.Success)?.data ?: emptyList()

    val activeBatch = remember(batches, selectedBatchId) {
        batches.firstOrNull { it.id == selectedBatchId } ?: batches.firstOrNull()
    }

    if (batchesState is UiState.Loading || studentsState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DeepSlateNavy)
        }
        return
    }

    if (activeBatch == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TutorDeskCard(cornerRadius = 16.dp) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No batches available", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Create a batch first in the Batches tab, then add students to start marking attendance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500
                    )
                }
            }
        }
        return
    }

    val studentsInBatch = remember(students, activeBatch.id) {
        students.filter {
            it.batchId == activeBatch.id && StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE
        }
    }

    val savedStatusByStudent = remember(savedRecords, activeBatch.id) {
        savedRecords
            .filter { it.batchId == activeBatch.id }
            .associate { it.studentId to AttendanceStatus.fromRaw(it.status) }
    }

    val effectiveStatusByStudent = remember(studentsInBatch, savedStatusByStudent, draftAttendance) {
        studentsInBatch.associate { student ->
            student.id to (draftAttendance[student.id] ?: savedStatusByStudent[student.id])
        }
    }

    val presentCount = effectiveStatusByStudent.values.count { it == AttendanceStatus.PRESENT }
    val lateCount = effectiveStatusByStudent.values.count { it == AttendanceStatus.LATE }
    val absentCount = effectiveStatusByStudent.values.count { it == AttendanceStatus.ABSENT }
    val markedCount = presentCount + lateCount + absentCount

    val dateOptions = remember { TutorDeskViewModel.recentDateOptions() }
    var batchMenuOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box {
                    Surface(
                        onClick = { batchMenuOpen = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_batch_switcher"),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceWhite,
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.dp, Slate300)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ACTIVE BATCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeBatch.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Slate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${activeBatch.scheduleDays} · ${activeBatch.scheduleTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Switch batch",
                                tint = DeepSlateNavy
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = batchMenuOpen,
                        onDismissRequest = { batchMenuOpen = false },
                        containerColor = SurfaceWhite
                    ) {
                        batches.forEach { batch ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = batch.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (batch.id == activeBatch.id) InteractiveBlue else Slate900
                                        )
                                        Text(
                                            text = "${batch.scheduleDays} · ${batch.scheduleTime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500
                                        )
                                    }
                                },
                                onClick = {
                                    batchMenuOpen = false
                                    onSelectBatch(batch.id)
                                }
                            )
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dateOptions, key = { it.first }) { (iso, label) ->
                        FilterPillChip(
                            label = label,
                            selected = selectedDateIso == iso,
                            onClick = { onSelectDate(iso) },
                            activeColor = InteractiveBlue
                        )
                    }
                }
            }

            item {
                TutorDeskCard(cornerRadius = 12.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryCountPill("$presentCount P", EmeraldBg, EmeraldText)
                            SummaryCountPill("$lateCount L", AmberBg, AmberText)
                            SummaryCountPill("$absentCount A", CoralBg, CoralText)
                        }
                        if (studentsInBatch.isNotEmpty()) {
                            TextButton(
                                onClick = { onMarkAllPresent(studentsInBatch) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("mark_all_present_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = null,
                                    tint = InteractiveBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All Present",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = InteractiveBlue
                                )
                            }
                        }
                    }
                }
            }

            if (studentsInBatch.isEmpty()) {
                item {
                    TutorDeskCard(cornerRadius = 16.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No students enrolled in ${activeBatch.name}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add students to this batch from the Students tab to start taking roll call.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate500
                            )
                        }
                    }
                }
            } else {
                items(studentsInBatch, key = { it.id }) { student ->
                    val status = effectiveStatusByStudent[student.id]
                    TutorDeskCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_row_${student.id}"),
                        cornerRadius = 12.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Slate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val subtitle = buildString {
                                    if (student.rollNumber.isNotBlank()) append("Roll ${student.rollNumber}")
                                    if (student.college.isNotBlank()) {
                                        if (isNotEmpty()) append(" · ")
                                        append(student.college)
                                    }
                                    if (isEmpty()) append(activeBatch.name)
                                }
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            SegmentedAttendanceToggle(
                                selectedStatus = status,
                                onSelect = { chosen -> onToggleStudentStatus(student.id, chosen) },
                                studentIdTag = student.id
                            )
                        }
                    }
                }
            }
        }

        if (studentsInBatch.isNotEmpty()) {
            Surface(
                color = SurfaceWhite,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, Slate300)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$markedCount of ${studentsInBatch.size} marked",
                            style = MaterialTheme.typography.labelLarge,
                            color = Slate900
                        )
                        Text(
                            text = if (draftAttendance.isNotEmpty()) "Unsaved changes ready" else "Synced for $selectedDateIso",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (draftAttendance.isNotEmpty()) WarmAmber else Slate500
                        )
                    }
                    TutorDeskPrimaryButton(
                        text = if (isSaving) "Saving..." else "Save Roll Call",
                        icon = Icons.Filled.Check,
                        enabled = !isSaving && markedCount > 0,
                        onClick = { onSaveRollCall(activeBatch.id, studentsInBatch, savedRecords) },
                        testTag = "save_attendance_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCountPill(
    text: String,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
                fontSize = 12.sp
            ),
            color = textColor
        )
    }
}
