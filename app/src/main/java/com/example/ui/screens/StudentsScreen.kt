package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Batch
import com.example.data.model.Student
import com.example.data.model.StudentStatus
import com.example.ui.components.BdtCurrencyField
import com.example.ui.components.FilterPillChip
import com.example.ui.components.InfoPill
import com.example.ui.components.StatusBadge
import com.example.ui.components.TutorDeskCard
import com.example.ui.components.TutorDeskDestructiveButton
import com.example.ui.components.TutorDeskPrimaryButton
import com.example.ui.components.TutorDeskSecondaryButton
import com.example.ui.components.TutorDeskTextField
import com.example.ui.components.formatBdt
import com.example.ui.theme.CoralBg
import com.example.ui.theme.CoralText
import com.example.ui.theme.DeepSlateNavy
import com.example.ui.theme.EmeraldBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldText
import com.example.ui.theme.InteractiveBlue
import com.example.ui.theme.InteractiveBlueBg
import com.example.ui.theme.InteractiveBlueDeep
import com.example.ui.theme.SheetScrim
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceLow
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularBodyStyle
import com.example.ui.viewmodel.UiState

@Composable
fun StudentsScreen(
    batchesState: UiState<List<Batch>>,
    studentsState: UiState<List<Student>>,
    isSaving: Boolean,
    showCreateSheet: Boolean,
    onOpenCreateSheet: () -> Unit,
    onDismissCreateSheet: () -> Unit,
    onSaveStudent: (
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
    ) -> Unit,
    onDeleteStudent: (String, () -> Unit) -> Unit
) {
    val batches = (batchesState as? UiState.Success)?.data ?: emptyList()

    when (studentsState) {
        UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepSlateNavy)
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                TutorDeskCard(cornerRadius = 16.dp) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Could not load students", style = MaterialTheme.typography.headlineSmall, color = CoralText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(studentsState.message, style = MaterialTheme.typography.bodyMedium, color = Slate700)
                    }
                }
            }
        }
        is UiState.Success -> {
            val allStudents = studentsState.data
            var searchQuery by remember { mutableStateOf("") }
            var selectedBatchFilterId by remember { mutableStateOf<String?>(null) }
            var selectedStudentDetail by remember { mutableStateOf<Student?>(null) }
            var editingStudent by remember { mutableStateOf<Student?>(null) }

            val filteredStudents = remember(allStudents, searchQuery, selectedBatchFilterId) {
                allStudents.filter { student ->
                    val matchesBatch = selectedBatchFilterId == null || student.batchId == selectedBatchFilterId
                    val q = searchQuery.trim()
                    val matchesQuery = q.isEmpty() ||
                        student.name.contains(q, ignoreCase = true) ||
                        student.college.contains(q, ignoreCase = true) ||
                        student.rollNumber.contains(q, ignoreCase = true)
                    matchesBatch && matchesQuery
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    TutorDeskTextField(
                        label = "Directory Search",
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search student name, college, or roll...",
                        testTag = "student_search_input"
                    )
                }

                if (batches.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterPillChip(
                                    label = "All (${allStudents.size})",
                                    selected = selectedBatchFilterId == null,
                                    onClick = { selectedBatchFilterId = null },
                                    activeColor = InteractiveBlue
                                )
                            }
                            items(batches, key = { it.id }) { batch ->
                                val count = allStudents.count { it.batchId == batch.id }
                                FilterPillChip(
                                    label = "${batch.name} ($count)",
                                    selected = selectedBatchFilterId == batch.id,
                                    onClick = { selectedBatchFilterId = batch.id },
                                    activeColor = InteractiveBlue
                                )
                            }
                        }
                    }
                }

                if (filteredStudents.isEmpty()) {
                    item {
                        TutorDeskCard(cornerRadius = 16.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(InteractiveBlueBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.People,
                                        contentDescription = null,
                                        tint = InteractiveBlueDeep,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (batches.isEmpty()) "Create a batch first" else if (allStudents.isEmpty()) "No students enrolled yet" else "No matching students",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (batches.isEmpty())
                                        "Head to the Batches tab to create an HSC ICT batch before enrolling students."
                                    else if (allStudents.isEmpty())
                                        "Add your HSC ICT students with their college, roll, guardian contact, and monthly ৳ fee."
                                    else "Try adjusting your search or batch filter.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate500
                                )
                                if (batches.isNotEmpty() && allStudents.isEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TutorDeskPrimaryButton(
                                        text = "Add First Student",
                                        icon = Icons.Filled.Add,
                                        onClick = onOpenCreateSheet,
                                        modifier = Modifier.fillMaxWidth(),
                                        testTag = "empty_add_student_button"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentDirectoryCard(
                            student = student,
                            onClick = { selectedStudentDetail = student }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            selectedStudentDetail?.let { detailStudent ->
                StudentDetailSheet(
                    student = detailStudent,
                    isSaving = isSaving,
                    onDismiss = { selectedStudentDetail = null },
                    onEdit = {
                        selectedStudentDetail = null
                        editingStudent = detailStudent
                    },
                    onDelete = {
                        onDeleteStudent(detailStudent.id) {
                            selectedStudentDetail = null
                        }
                    }
                )
            }

            if (showCreateSheet || editingStudent != null) {
                StudentFormSheet(
                    batches = batches,
                    initialStudent = editingStudent,
                    isSaving = isSaving,
                    onDismiss = {
                        editingStudent = null
                        onDismissCreateSheet()
                    },
                    onSave = { batchId, batchName, name, college, roll, phone, fee, status ->
                        onSaveStudent(
                            editingStudent?.id,
                            batchId,
                            batchName,
                            name,
                            college,
                            roll,
                            phone,
                            fee,
                            status
                        ) {
                            editingStudent = null
                            onDismissCreateSheet()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StudentDirectoryCard(
    student: Student,
    onClick: () -> Unit
) {
    val initials = student.name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "S" }

    val isPaused = StudentStatus.fromRaw(student.status) == StudentStatus.PAUSED

    TutorDeskCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}"),
        cornerRadius = 12.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepSlateNavy
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isPaused) {
                        StatusBadge("Paused", SurfaceLow, Slate500, Slate500)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val meta = buildString {
                    if (student.rollNumber.isNotBlank()) append("Roll ${student.rollNumber}")
                    if (student.college.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(student.college)
                    }
                }
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoPill(text = student.batchName)
                    if (student.guardianPhone.isNotBlank()) {
                        Text(
                            text = student.guardianPhone,
                            style = TabularBodyStyle.copy(fontWeight = FontWeight.Normal),
                            color = Slate500,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatBdt(student.monthlyFeeBdt),
                    style = TabularBodyStyle.copy(fontWeight = FontWeight.Bold),
                    color = DeepSlateNavy
                )
                Text(
                    text = "/mo",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentDetailSheet(
    student: Student,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        scrimColor = SheetScrim,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(student.name, style = MaterialTheme.typography.headlineMedium, color = Slate900)
                    Text(student.batchName, style = MaterialTheme.typography.bodyMedium, color = Slate500)
                }
                if (StudentStatus.fromRaw(student.status) == StudentStatus.ACTIVE) {
                    StatusBadge("Active", EmeraldBg, EmeraldText, EmeraldGreen)
                } else {
                    StatusBadge("Paused", SurfaceLow, Slate500, Slate500)
                }
            }

            HorizontalDivider(color = Slate200)

            DetailRow(Icons.Filled.School, "College & Roll", "${student.college.ifBlank { "—" }} · Roll ${student.rollNumber.ifBlank { "—" }}")
            DetailRow(Icons.Filled.Phone, "Guardian Phone", student.guardianPhone.ifBlank { "Not provided" })

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceLow)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Tuition Rate", style = MaterialTheme.typography.labelLarge, color = Slate700)
                Text(formatBdt(student.monthlyFeeBdt), style = TabularBodyStyle.copy(fontWeight = FontWeight.Bold), color = DeepSlateNavy)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TutorDeskPrimaryButton(
                    text = "Edit Student",
                    icon = Icons.Filled.Edit,
                    onClick = onEdit,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                )
                TutorDeskDestructiveButton(
                    text = "Remove",
                    onClick = onDelete,
                    enabled = !isSaving,
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Slate900)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentFormSheet(
    batches: List<Batch>,
    initialStudent: Student?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        batchId: String,
        batchName: String,
        name: String,
        college: String,
        rollNumber: String,
        guardianPhone: String,
        monthlyFeeBdt: Long,
        status: StudentStatus
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedBatch by remember(initialStudent, batches) {
        mutableStateOf(
            batches.firstOrNull { it.id == initialStudent?.batchId } ?: batches.firstOrNull()
        )
    }
    var name by remember(initialStudent) { mutableStateOf(initialStudent?.name ?: "") }
    var college by remember(initialStudent) { mutableStateOf(initialStudent?.college ?: "") }
    var rollNumber by remember(initialStudent) { mutableStateOf(initialStudent?.rollNumber ?: "") }
    var guardianPhone by remember(initialStudent) { mutableStateOf(initialStudent?.guardianPhone ?: "") }
    var feeStr by remember(initialStudent, selectedBatch) {
        mutableStateOf(
            (initialStudent?.monthlyFeeBdt ?: selectedBatch?.monthlyFeeBdt ?: 1500L).toString()
        )
    }
    var status by remember(initialStudent) { mutableStateOf(StudentStatus.fromRaw(initialStudent?.status)) }
    var localError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        scrimColor = SheetScrim,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (initialStudent == null) "Enroll Student" else "Edit Student",
                style = MaterialTheme.typography.headlineMedium,
                color = Slate900
            )

            Column {
                Text(
                    text = "Assign Batch",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (batches.isEmpty()) {
                    Text("No batches found. Create a batch first.", style = MaterialTheme.typography.bodySmall, color = CoralText)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(batches, key = { it.id }) { batch ->
                            FilterPillChip(
                                label = batch.name,
                                selected = selectedBatch?.id == batch.id,
                                onClick = {
                                    selectedBatch = batch
                                    if (initialStudent == null) {
                                        feeStr = batch.monthlyFeeBdt.toString()
                                    }
                                    localError = null
                                },
                                activeColor = DeepSlateNavy
                            )
                        }
                    }
                }
            }

            TutorDeskTextField(
                label = "Student Full Name",
                value = name,
                onValueChange = { name = it; localError = null },
                placeholder = "e.g. Tasnim Tabassum",
                testTag = "student_name_input"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TutorDeskTextField(
                    label = "College",
                    value = college,
                    onValueChange = { college = it },
                    placeholder = "e.g. Dhaka College",
                    modifier = Modifier.weight(1.3f)
                )
                TutorDeskTextField(
                    label = "Roll / ID",
                    value = rollNumber,
                    onValueChange = { rollNumber = it },
                    placeholder = "104",
                    modifier = Modifier.weight(0.7f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TutorDeskTextField(
                    label = "Guardian Phone",
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    placeholder = "01711-000000",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.weight(1.1f)
                )
                BdtCurrencyField(
                    label = "Monthly Fee",
                    value = feeStr,
                    onValueChange = { feeStr = it; localError = null },
                    placeholder = "1500",
                    modifier = Modifier.weight(0.9f),
                    testTag = "student_fee_input"
                )
            }

            if (initialStudent != null) {
                Column {
                    Text(
                        text = "Enrollment Status",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StudentStatus.entries.forEach { st ->
                            FilterPillChip(
                                label = if (st == StudentStatus.ACTIVE) "Active" else "Paused",
                                selected = status == st,
                                onClick = { status = st }
                            )
                        }
                    }
                }
            }

            if (!localError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CoralBg)
                        .padding(10.dp)
                ) {
                    Text(localError ?: "", style = MaterialTheme.typography.bodySmall, color = CoralText)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TutorDeskSecondaryButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.weight(0.4f)
                )
                TutorDeskPrimaryButton(
                    text = if (isSaving) "Saving..." else if (initialStudent == null) "Enroll Student" else "Save Changes",
                    onClick = {
                        val batch = selectedBatch
                        val fee = feeStr.toLongOrNull() ?: -1L
                        when {
                            batch == null -> localError = "Please select a batch."
                            name.isBlank() -> localError = "Please enter the student's name."
                            fee !in 0L..100_000L -> localError = "Monthly fee must be between ৳ 0 and ৳ 100,000."
                            else -> onSave(batch.id, batch.name, name, college, rollNumber, guardianPhone, fee, status)
                        }
                    },
                    enabled = !isSaving && batches.isNotEmpty(),
                    modifier = Modifier.weight(0.6f),
                    testTag = "save_student_button"
                )
            }
        }
    }
}
