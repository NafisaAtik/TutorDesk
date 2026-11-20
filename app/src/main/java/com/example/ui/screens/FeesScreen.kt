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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Batch
import com.example.data.model.FeePayment
import com.example.data.model.FeeStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Student
import com.example.data.model.StudentFeeSummary
import com.example.ui.components.BdtCurrencyField
import com.example.ui.components.FeeStatusBadge
import com.example.ui.components.FilterPillChip
import com.example.ui.components.InfoPill
import com.example.ui.components.KpiStatCard
import com.example.ui.components.TutorDeskCard
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
import com.example.ui.viewmodel.TutorDeskViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun FeesScreen(
    batchesState: UiState<List<Batch>>,
    studentsState: UiState<List<Student>>,
    feesState: UiState<List<FeePayment>>,
    selectedMonthKey: String,
    isSaving: Boolean,
    computeSummaries: (List<Student>, List<FeePayment>, String) -> List<StudentFeeSummary>,
    onSelectMonth: (String) -> Unit,
    onRecordPayment: (
        student: Student,
        monthKey: String,
        paidAmountBdt: Long,
        paymentMethod: PaymentMethod,
        note: String,
        onDone: () -> Unit
    ) -> Unit
) {
    val batches = (batchesState as? UiState.Success)?.data ?: emptyList()
    val students = (studentsState as? UiState.Success)?.data ?: emptyList()
    val payments = (feesState as? UiState.Success)?.data ?: emptyList()

    if (studentsState is UiState.Loading || feesState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DeepSlateNavy)
        }
        return
    }

    if (feesState is UiState.Error) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            TutorDeskCard(cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Could not load fee ledger", style = MaterialTheme.typography.headlineSmall, color = CoralText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(feesState.message, style = MaterialTheme.typography.bodyMedium, color = Slate700)
                }
            }
        }
        return
    }

    var selectedStatusFilter by remember { mutableStateOf<FeeStatus?>(null) }
    var selectedBatchFilterId by remember { mutableStateOf<String?>(null) }
    var activeFeeSheetTarget by remember { mutableStateOf<StudentFeeSummary?>(null) }

    val monthOptions = remember { TutorDeskViewModel.recentMonthOptions() }
    val allSummaries = remember(students, payments, selectedMonthKey) {
        computeSummaries(students, payments, selectedMonthKey)
    }

    val batchScopedSummaries = remember(allSummaries, selectedBatchFilterId) {
        if (selectedBatchFilterId == null) allSummaries
        else allSummaries.filter { it.student.batchId == selectedBatchFilterId }
    }

    val totalExpected = batchScopedSummaries.sumOf { it.expectedFeeBdt }
    val totalCollected = batchScopedSummaries.sumOf { it.paidAmountBdt }
    val totalDues = batchScopedSummaries.sumOf { it.dueAmountBdt }
    val paidStudentsCount = batchScopedSummaries.count { it.status == FeeStatus.PAID }
    val collectionRatio = if (totalExpected > 0L) (totalCollected.toFloat() / totalExpected.toFloat()).coerceIn(0f, 1f) else 0f

    val displayedSummaries = remember(batchScopedSummaries, selectedStatusFilter) {
        val base = if (selectedStatusFilter == null) batchScopedSummaries
        else batchScopedSummaries.filter { it.status == selectedStatusFilter }
        base.sortedWith(compareBy<StudentFeeSummary> { it.status == FeeStatus.PAID }.thenByDescending { it.dueAmountBdt })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(monthOptions, key = { it.first }) { (key, label) ->
                    FilterPillChip(
                        label = label,
                        selected = selectedMonthKey == key,
                        onClick = { onSelectMonth(key) },
                        activeColor = InteractiveBlue
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiStatCard(
                    label = "Total Collected",
                    value = formatBdt(totalCollected),
                    valueColor = EmeraldGreen,
                    subtitle = "$paidStudentsCount of ${batchScopedSummaries.size} paid",
                    modifier = Modifier.weight(1f)
                )
                KpiStatCard(
                    label = "Total Dues",
                    value = formatBdt(totalDues),
                    valueColor = if (totalDues == 0L) EmeraldGreen else CoralRed,
                    subtitle = "Target ${formatBdt(totalExpected)}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            TutorDeskCard(cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Collection Progress",
                            style = MaterialTheme.typography.labelLarge,
                            color = Slate900
                        )
                        Text(
                            text = "${(collectionRatio * 100).toInt()}%",
                            style = TabularBodyStyle,
                            color = EmeraldText
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { collectionRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = EmeraldGreen,
                        trackColor = SurfaceLow
                    )
                }
            }
        }

        if (batches.size > 1) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterPillChip(
                            label = "All Batches",
                            selected = selectedBatchFilterId == null,
                            onClick = { selectedBatchFilterId = null }
                        )
                    }
                    items(batches, key = { it.id }) { batch ->
                        FilterPillChip(
                            label = batch.name,
                            selected = selectedBatchFilterId == batch.id,
                            onClick = { selectedBatchFilterId = batch.id }
                        )
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterPillChip(
                        label = "All (${batchScopedSummaries.size})",
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        activeColor = DeepSlateNavy
                    )
                }
                item {
                    val count = batchScopedSummaries.count { it.status == FeeStatus.DUE }
                    FilterPillChip(
                        label = "Due ($count)",
                        selected = selectedStatusFilter == FeeStatus.DUE,
                        onClick = { selectedStatusFilter = FeeStatus.DUE },
                        activeColor = CoralRed
                    )
                }
                item {
                    val count = batchScopedSummaries.count { it.status == FeeStatus.PARTIAL }
                    FilterPillChip(
                        label = "Partial ($count)",
                        selected = selectedStatusFilter == FeeStatus.PARTIAL,
                        onClick = { selectedStatusFilter = FeeStatus.PARTIAL },
                        activeColor = DeepSlateNavy
                    )
                }
                item {
                    val count = batchScopedSummaries.count { it.status == FeeStatus.PAID }
                    FilterPillChip(
                        label = "Paid ($count)",
                        selected = selectedStatusFilter == FeeStatus.PAID,
                        onClick = { selectedStatusFilter = FeeStatus.PAID },
                        activeColor = EmeraldGreen
                    )
                }
            }
        }

        if (displayedSummaries.isEmpty()) {
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
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(EmeraldBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (allSummaries.isEmpty()) "No active students to bill" else "No matching records",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (allSummaries.isEmpty())
                                "Enroll students in your HSC ICT batches to track monthly ৳ tuition payments here."
                            else "Try clearing the filter chips above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate500
                        )
                    }
                }
            }
        } else {
            items(displayedSummaries, key = { it.student.id }) { summary ->
                FeeLedgerRowCard(
                    summary = summary,
                    onClick = { activeFeeSheetTarget = summary }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    activeFeeSheetTarget?.let { target ->
        FeePaymentSheet(
            summary = target,
            isSaving = isSaving,
            onDismiss = { activeFeeSheetTarget = null },
            onSave = { paidAmount, method, note ->
                onRecordPayment(
                    target.student,
                    target.monthKey,
                    paidAmount,
                    method,
                    note
                ) {
                    activeFeeSheetTarget = null
                }
            }
        )
    }
}

@Composable
private fun FeeLedgerRowCard(
    summary: StudentFeeSummary,
    onClick: () -> Unit
) {
    TutorDeskCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fee_card_${summary.student.id}"),
        cornerRadius = 12.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.student.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = summary.student.batchName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FeeStatusBadge(status = summary.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Paid ${formatBdt(summary.paidAmountBdt)} / ${formatBdt(summary.expectedFeeBdt)}",
                        style = TabularBodyStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        color = Slate700
                    )
                    if (summary.paidAmountBdt > 0L) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = buildString {
                                append("via ${summary.paymentMethod}")
                                if (summary.note.isNotBlank()) append(" · ${summary.note}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatBdt(summary.dueAmountBdt)} Due",
                        style = TabularBodyStyle.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.dueAmountBdt == 0L) EmeraldGreen else CoralRed
                    )
                    Text(
                        text = if (summary.dueAmountBdt == 0L) "Cleared" else "Tap to collect",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (summary.dueAmountBdt == 0L) Slate500 else InteractiveBlue
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeePaymentSheet(
    summary: StudentFeeSummary,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (paidAmountBdt: Long, method: PaymentMethod, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountStr by remember(summary) {
        val prefill = if (summary.paidAmountBdt > 0L) summary.paidAmountBdt else summary.expectedFeeBdt
        mutableStateOf(prefill.toString())
    }
    var selectedMethod by remember(summary) {
        mutableStateOf(PaymentMethod.fromRaw(summary.paymentMethod))
    }
    var note by remember(summary) { mutableStateOf(summary.note) }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Record Tuition Payment",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Slate900
                    )
                    Text(
                        text = "${summary.student.name} · ${summary.monthKey}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                InfoPill(
                    text = "Fee ${formatBdt(summary.expectedFeeBdt)}",
                    containerColor = InteractiveBlueBg,
                    textColor = InteractiveBlueDeep
                )
            }

            BdtCurrencyField(
                label = "Total Paid Amount for ${summary.monthKey}",
                value = amountStr,
                onValueChange = { amountStr = it; localError = null },
                placeholder = summary.expectedFeeBdt.toString(),
                testTag = "fee_amount_input"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterPillChip(
                        label = "Full (${formatBdt(summary.expectedFeeBdt)})",
                        selected = amountStr == summary.expectedFeeBdt.toString(),
                        onClick = { amountStr = summary.expectedFeeBdt.toString() },
                        activeColor = EmeraldGreen
                    )
                }
                item {
                    val half = summary.expectedFeeBdt / 2L
                    FilterPillChip(
                        label = "Half (${formatBdt(half)})",
                        selected = amountStr == half.toString(),
                        onClick = { amountStr = half.toString() }
                    )
                }
                item {
                    FilterPillChip(
                        label = "Unpaid (৳ 0)",
                        selected = amountStr == "0",
                        onClick = { amountStr = "0" },
                        activeColor = CoralRed
                    )
                }
            }

            Column {
                Text(
                    text = "Payment Channel",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PaymentMethod.entries) { method ->
                        FilterPillChip(
                            label = method.label,
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            activeColor = DeepSlateNavy
                        )
                    }
                }
            }

            TutorDeskTextField(
                label = "Receipt / Note (Optional)",
                value = note,
                onValueChange = { note = it.take(200) },
                placeholder = "e.g. bKash TrxID or Paid in morning batch"
            )

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
                    text = if (isSaving) "Saving..." else "Save Payment",
                    icon = Icons.Filled.Payments,
                    onClick = {
                        val parsed = amountStr.toLongOrNull() ?: -1L
                        if (parsed !in 0L..100_000L) {
                            localError = "Enter an amount between ৳ 0 and ৳ 100,000."
                        } else {
                            onSave(parsed, selectedMethod, note)
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.weight(0.6f),
                    testTag = "save_fee_payment_button"
                )
            }
        }
    }
}
