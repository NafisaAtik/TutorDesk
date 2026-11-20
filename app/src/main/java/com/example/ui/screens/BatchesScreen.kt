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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Batch
import com.example.data.model.BatchStatus
import com.example.data.model.HscLevel
import com.example.data.model.Student
import com.example.data.model.StudentStatus
import com.example.ui.components.BdtCurrencyField
import com.example.ui.components.FilterPillChip
import com.example.ui.components.InfoPill
import com.example.ui.components.KpiStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TutorDeskCard
import com.example.ui.components.TutorDeskDestructiveButton
import com.example.ui.components.TutorDeskPrimaryButton
import com.example.ui.components.TutorDeskSecondaryButton
import com.example.ui.components.TutorDeskTextField
import com.example.ui.components.formatBdt
import com.example.ui.theme.CoralBg
import com.example.ui.theme.CoralRed
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
import com.example.ui.theme.SurfaceLow
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularBodyStyle
import com.example.ui.viewmodel.UiState

@Composable
fun BatchesScreen(
    batchesState: UiState<List<Batch>>,
    studentsState: UiState<List<Student>>,
    isSaving: Boolean,
    onTakeAttendance: (String) -> Unit,
    onSaveBatch: (
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
    ) -> Unit,
    onDeleteBatch: (String, () -> Unit) -> Unit,
    onLoadStarterData: () -> Unit,
    showCreateSheet: Boolean,
    onDismissCreateSheet: () -> Unit,
    onOpenCreateSheet: () -> Unit
) {
    var selectedLevelFilter by remember { mutableStateOf<String>("All") }
    var editingBatch by remember { mutableStateOf<Batch?>(null) }

    val students = (studentsState as? UiState.Success)?.data ?: emptyList()
    val activeStudentsByBatch = remember(students) {
        students
            .filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
            .groupBy { it.batchId }
    }

    when (batchesState) {
        UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepSlateNavy)
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                TutorDeskCard(cornerRadius = 16.dp) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Could not load batches", style = MaterialTheme.typography.headlineSmall, color = CoralText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(batchesState.message, style = MaterialTheme.typography.bodyMedium, color = Slate700)
                    }
                }
            }
        }
        is UiState.Success -> {
            val allBatches = batchesState.data
            val activeBatchesCount = allBatches.count { BatchStatus.fromRaw(it.status) == BatchStatus.ACTIVE }
            val totalEnrolled = students.count { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
            val expectedMonthlyTotal = students
                .filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
                .sumOf { it.monthlyFeeBdt }

            val filteredBatches = remember(allBatches, selectedLevelFilter) {
                if (selectedLevelFilter == "All") allBatches
                else allBatches.filter { it.hscLevel == selectedLevelFilter }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiStatCard(
                            label = "Active Batches",
                            value = activeBatchesCount.toString(),
                            valueColor = DeepSlateNavy,
                            subtitle = "$totalEnrolled active students",
                            modifier = Modifier.weight(1f)
                        )
                        KpiStatCard(
                            label = "Monthly Target",
                            value = formatBdt(expectedMonthlyTotal),
                            valueColor = EmeraldGreen,
                            subtitle = "Projected tuition",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filters = listOf("All", "XI", "XII", "HSC Revision")
                        items(filters) { level ->
                            FilterPillChip(
                                label = if (level == "All") "All Levels" else if (level == "HSC Revision") level else "Class $level",
                                selected = selectedLevelFilter == level,
                                onClick = { selectedLevelFilter = level },
                                activeColor = InteractiveBlue
                            )
                        }
                    }
                }

                if (filteredBatches.isEmpty()) {
                    item {
                        TutorDeskCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 16.dp
                        ) {
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
                                        imageVector = Icons.Filled.Class,
                                        contentDescription = null,
                                        tint = InteractiveBlueDeep,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (allBatches.isEmpty()) "No HSC ICT batches yet" else "No batches match this level",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (allBatches.isEmpty())
                                        "Create your first Class XI or XII batch to start taking roll call and tracking monthly BDT fees."
                                    else "Try switching to All Levels or add a new batch.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                TutorDeskPrimaryButton(
                                    text = "Create New Batch",
                                    icon = Icons.Filled.Add,
                                    onClick = onOpenCreateSheet,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "empty_create_batch_button"
                                )
                                if (allBatches.isEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    TutorDeskSecondaryButton(
                                        text = "Load Starter HSC ICT Batches",
                                        onClick = onLoadStarterData,
                                        enabled = !isSaving,
                                        modifier = Modifier.fillMaxWidth(),
                                        testTag = "load_starter_data_button"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredBatches, key = { it.id }) { batch ->
                        val enrolled = activeStudentsByBatch[batch.id]?.size ?: 0
                        BatchOverviewCard(
                            batch = batch,
                            enrolledCount = enrolled,
                            onTakeAttendance = { onTakeAttendance(batch.id) },
                            onEdit = { editingBatch = batch }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showCreateSheet || editingBatch != null) {
        BatchFormSheet(
            initialBatch = editingBatch,
            isSaving = isSaving,
            onDismiss = {
                editingBatch = null
                onDismissCreateSheet()
            },
            onSave = { name, level, days, time, room, topic, fee, cap, status ->
                onSaveBatch(
                    editingBatch?.id,
                    name,
                    level,
                    days,
                    time,
                    room,
                    topic,
                    fee,
                    cap,
                    status
                ) {
                    editingBatch = null
                    onDismissCreateSheet()
                }
            },
            onDelete = editingBatch?.let { batch ->
                {
                    onDeleteBatch(batch.id) {
                        editingBatch = null
                        onDismissCreateSheet()
                    }
                }
            }
        )
    }
}

@Composable
private fun BatchOverviewCard(
    batch: Batch,
    enrolledCount: Int,
    onTakeAttendance: () -> Unit,
    onEdit: () -> Unit
) {
    val isArchived = BatchStatus.fromRaw(batch.status) == BatchStatus.ARCHIVED
    val capacity = batch.maxCapacity.coerceAtLeast(1L).toInt()
    val fillRatio = (enrolledCount.toFloat() / capacity.toFloat()).coerceIn(0f, 1f)

    TutorDeskCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("batch_card_${batch.id}"),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoPill(
                            text = if (batch.hscLevel == "HSC Revision") batch.hscLevel else "Class ${batch.hscLevel}"
                        )
                        if (isArchived) {
                            StatusBadge("Archived", SurfaceLow, Slate500, Slate500)
                        } else {
                            StatusBadge("Active", EmeraldBg, EmeraldText, EmeraldGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = batch.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Slate900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatBdt(batch.monthlyFeeBdt),
                        style = TabularBodyStyle.copy(fontWeight = FontWeight.Bold),
                        color = DeepSlateNavy
                    )
                    Text(
                        text = "per month",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Slate500, modifier = Modifier.size(15.dp))
                    Text(batch.scheduleDays, style = MaterialTheme.typography.bodySmall, color = Slate700)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Slate500, modifier = Modifier.size(15.dp))
                    Text(batch.scheduleTime, style = MaterialTheme.typography.bodySmall, color = Slate700)
                }
            }

            if (batch.room.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Slate500, modifier = Modifier.size(15.dp))
                    Text(batch.room, style = MaterialTheme.typography.bodySmall, color = Slate500)
                }
            }

            if (batch.currentTopic.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceLow)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = InteractiveBlue, modifier = Modifier.size(16.dp))
                    Text(
                        text = batch.currentTopic,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Slate700,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seat Occupancy",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate500
                )
                Text(
                    text = "$enrolledCount / $capacity students",
                    style = TabularBodyStyle.copy(fontSize = 12.sp),
                    color = if (enrolledCount >= capacity) CoralRed else Slate700
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fillRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (enrolledCount >= capacity) CoralRed else InteractiveBlue,
                trackColor = SurfaceLow
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TutorDeskPrimaryButton(
                    text = "Roll Call",
                    icon = Icons.Filled.Checklist,
                    onClick = onTakeAttendance,
                    modifier = Modifier.weight(1f),
                    testTag = "roll_call_button_${batch.id}"
                )
                TutorDeskSecondaryButton(
                    text = "Edit",
                    icon = Icons.Filled.Edit,
                    onClick = onEdit,
                    testTag = "edit_batch_button_${batch.id}"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchFormSheet(
    initialBatch: Batch?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        hscLevel: HscLevel,
        scheduleDays: String,
        scheduleTime: String,
        room: String,
        currentTopic: String,
        monthlyFeeBdt: Long,
        maxCapacity: Long,
        status: BatchStatus
    ) -> Unit,
    onDelete: (() -> Unit)?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(initialBatch) { mutableStateOf(initialBatch?.name ?: "") }
    var hscLevel by remember(initialBatch) { mutableStateOf(HscLevel.fromRaw(initialBatch?.hscLevel)) }
    var scheduleDays by remember(initialBatch) { mutableStateOf(initialBatch?.scheduleDays ?: "Sat · Mon · Wed") }
    var scheduleTime by remember(initialBatch) { mutableStateOf(initialBatch?.scheduleTime ?: "08:00 AM") }
    var room by remember(initialBatch) { mutableStateOf(initialBatch?.room ?: "") }
    var currentTopic by remember(initialBatch) { mutableStateOf(initialBatch?.currentTopic ?: "") }
    var monthlyFeeStr by remember(initialBatch) { mutableStateOf((initialBatch?.monthlyFeeBdt ?: 1500L).toString()) }
    var maxCapStr by remember(initialBatch) { mutableStateOf((initialBatch?.maxCapacity ?: 25L).toString()) }
    var status by remember(initialBatch) { mutableStateOf(BatchStatus.fromRaw(initialBatch?.status)) }
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
                text = if (initialBatch == null) "Create HSC ICT Batch" else "Edit Batch",
                style = MaterialTheme.typography.headlineMedium,
                color = Slate900
            )

            TutorDeskTextField(
                label = "Batch Name",
                value = name,
                onValueChange = { name = it; localError = null },
                placeholder = "e.g. HSC-26 Morning ICT",
                testTag = "batch_name_input"
            )

            Column {
                Text(
                    text = "Academic Level",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HscLevel.entries.forEach { lvl ->
                        FilterPillChip(
                            label = if (lvl == HscLevel.HSC_REVISION) lvl.label else "Class ${lvl.label}",
                            selected = hscLevel == lvl,
                            onClick = { hscLevel = lvl }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TutorDeskTextField(
                    label = "Class Days",
                    value = scheduleDays,
                    onValueChange = { scheduleDays = it; localError = null },
                    placeholder = "Sat · Mon · Wed",
                    modifier = Modifier.weight(1.2f)
                )
                TutorDeskTextField(
                    label = "Time",
                    value = scheduleTime,
                    onValueChange = { scheduleTime = it; localError = null },
                    placeholder = "08:00 AM",
                    modifier = Modifier.weight(0.8f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BdtCurrencyField(
                    label = "Monthly Tuition",
                    value = monthlyFeeStr,
                    onValueChange = { monthlyFeeStr = it; localError = null },
                    placeholder = "1500",
                    modifier = Modifier.weight(1f),
                    testTag = "batch_fee_input"
                )
                TutorDeskTextField(
                    label = "Max Seats",
                    value = maxCapStr,
                    onValueChange = { maxCapStr = it.filter { ch -> ch.isDigit() }.take(3) },
                    placeholder = "25",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(0.8f)
                )
            }

            TutorDeskTextField(
                label = "Room / Location",
                value = room,
                onValueChange = { room = it },
                placeholder = "e.g. Mirpur-10 Room 2 or Online"
            )

            TutorDeskTextField(
                label = "Current ICT Chapter / Topic",
                value = currentTopic,
                onValueChange = { currentTopic = it },
                placeholder = "e.g. Ch 3: Number Systems & Logic Gates"
            )

            if (initialBatch != null) {
                Column {
                    Text(
                        text = "Batch Status",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BatchStatus.entries.forEach { st ->
                            FilterPillChip(
                                label = if (st == BatchStatus.ACTIVE) "Active" else "Archived",
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

            TutorDeskPrimaryButton(
                text = if (isSaving) "Saving..." else if (initialBatch == null) "Create Batch" else "Save Changes",
                onClick = {
                    val fee = monthlyFeeStr.toLongOrNull() ?: -1L
                    val cap = maxCapStr.toLongOrNull() ?: 0L
                    when {
                        name.isBlank() -> localError = "Please enter a batch name."
                        scheduleDays.isBlank() -> localError = "Please enter class days."
                        scheduleTime.isBlank() -> localError = "Please enter class time."
                        fee !in 0L..100_000L -> localError = "Fee must be between ৳ 0 and ৳ 100,000."
                        cap !in 1L..500L -> localError = "Capacity must be 1–500."
                        else -> onSave(name, hscLevel, scheduleDays, scheduleTime, room, currentTopic, fee, cap, status)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_batch_button"
            )

            if (onDelete != null) {
                TutorDeskDestructiveButton(
                    text = "Delete Batch",
                    onClick = onDelete,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "delete_batch_button"
                )
            }
        }
    }
}
