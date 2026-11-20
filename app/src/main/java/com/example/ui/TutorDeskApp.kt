package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.repository.TutorDeskRepository
import com.example.ui.components.TutorDeskBottomNav
import com.example.ui.components.TutorDeskTopBar
import com.example.ui.components.TutorNavTabs
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BatchesScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.CanvasBase
import com.example.ui.theme.DeepSlateNavy
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.SurfaceLow
import com.example.ui.theme.SurfaceWhite
import com.example.ui.viewmodel.TutorDeskViewModel
import com.example.ui.viewmodel.TutorTab
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { /* AuthStateFlow transitions automatically */ })
    } else {
        key(user.uid) {
            TutorDeskWorkspace(user = user)
        }
    }
}

@Composable
private fun TutorDeskWorkspace(user: FirebaseUser) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: TutorDeskViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                TutorDeskViewModel(
                    repository = TutorDeskRepository(db),
                    currentUserId = user.uid
                )
            }
        }
    )

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val batchesState by viewModel.batchesState.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val attendanceState by viewModel.attendanceState.collectAsStateWithLifecycle()
    val feesState by viewModel.feesState.collectAsStateWithLifecycle()

    val selectedBatchId by viewModel.selectedAttendanceBatchId.collectAsStateWithLifecycle()
    val selectedDateIso by viewModel.selectedDateIso.collectAsStateWithLifecycle()
    val selectedMonthKey by viewModel.selectedMonthKey.collectAsStateWithLifecycle()
    val draftAttendance by viewModel.draftAttendance.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var showCreateBatchSheet by remember { mutableStateOf(false) }
    var showCreateStudentSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedTab != TutorTab.BATCHES) {
        viewModel.selectTab(TutorTab.BATCHES)
    }

    val headerSubtitle = when (selectedTab) {
        TutorTab.BATCHES -> "HSC ICT Batches & Schedules"
        TutorTab.ATTENDANCE -> "Daily Roll Call · $selectedDateIso"
        TutorTab.FEES -> "Monthly BDT (৳) Ledger · $selectedMonthKey"
        TutorTab.STUDENTS -> "Enrolled Student Directory"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(CanvasBase)) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = CanvasBase,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TutorDeskTopBar(
                    title = "TutorDesk",
                    subtitle = headerSubtitle,
                    userEmail = user.email,
                    onSignOut = { signOut(credentialManager, scope) }
                )
            },
            bottomBar = {
                if (!useRail) {
                    TutorDeskBottomNav(
                        selectedTab = selectedTab,
                        onSelectTab = viewModel::selectTab
                    )
                }
            },
            floatingActionButton = {
                when (selectedTab) {
                    TutorTab.BATCHES -> {
                        FloatingActionButton(
                            onClick = { showCreateBatchSheet = true },
                            containerColor = DeepSlateNavy,
                            contentColor = SurfaceWhite,
                            modifier = Modifier.testTag("fab_new_batch")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Create new batch")
                        }
                    }
                    TutorTab.STUDENTS -> {
                        val hasBatches = (batchesState as? UiState.Success)?.data?.isNotEmpty() == true
                        if (hasBatches) {
                            FloatingActionButton(
                                onClick = { showCreateStudentSheet = true },
                                containerColor = DeepSlateNavy,
                                contentColor = SurfaceWhite,
                                modifier = Modifier.testTag("fab_new_student")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Enroll new student")
                            }
                        }
                    }
                    else -> Unit
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = SurfaceWhite,
                        contentColor = DeepSlateNavy
                    ) {
                        TutorNavTabs.forEach { spec ->
                            val isSelected = selectedTab == spec.tab
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(spec.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) spec.selectedIcon else spec.unselectedIcon,
                                        contentDescription = spec.label
                                    )
                                },
                                label = { Text(spec.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = DeepSlateNavy,
                                    selectedTextColor = DeepSlateNavy,
                                    indicatorColor = SurfaceLow,
                                    unselectedIconColor = Slate400,
                                    unselectedTextColor = Slate400
                                ),
                                modifier = Modifier.testTag("rail_${spec.testTag}")
                            )
                        }
                    }
                    VerticalDivider(thickness = 1.dp, color = Slate200)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        when (selectedTab) {
                            TutorTab.BATCHES -> BatchesScreen(
                                batchesState = batchesState,
                                studentsState = studentsState,
                                isSaving = isSaving,
                                onTakeAttendance = viewModel::openAttendanceForBatch,
                                onSaveBatch = viewModel::saveBatch,
                                onDeleteBatch = viewModel::deleteBatch,
                                onLoadStarterData = viewModel::loadStarterDataset,
                                showCreateSheet = showCreateBatchSheet,
                                onDismissCreateSheet = { showCreateBatchSheet = false },
                                onOpenCreateSheet = { showCreateBatchSheet = true }
                            )
                            TutorTab.ATTENDANCE -> AttendanceScreen(
                                batchesState = batchesState,
                                studentsState = studentsState,
                                attendanceState = attendanceState,
                                selectedBatchId = selectedBatchId,
                                selectedDateIso = selectedDateIso,
                                draftAttendance = draftAttendance,
                                isSaving = isSaving,
                                onSelectBatch = viewModel::selectAttendanceBatch,
                                onSelectDate = viewModel::selectAttendanceDate,
                                onToggleStudentStatus = viewModel::setDraftAttendance,
                                onMarkAllPresent = viewModel::markAllDraftPresent,
                                onSaveRollCall = viewModel::saveAttendanceRollCall
                            )
                            TutorTab.FEES -> FeesScreen(
                                batchesState = batchesState,
                                studentsState = studentsState,
                                feesState = feesState,
                                selectedMonthKey = selectedMonthKey,
                                isSaving = isSaving,
                                computeSummaries = viewModel::computeFeeSummaries,
                                onSelectMonth = viewModel::selectFeeMonth,
                                onRecordPayment = viewModel::recordFeePayment
                            )
                            TutorTab.STUDENTS -> StudentsScreen(
                                batchesState = batchesState,
                                studentsState = studentsState,
                                isSaving = isSaving,
                                showCreateSheet = showCreateStudentSheet,
                                onOpenCreateSheet = { showCreateStudentSheet = true },
                                onDismissCreateSheet = { showCreateStudentSheet = false },
                                onSaveStudent = viewModel::saveStudent,
                                onDeleteStudent = viewModel::deleteStudent
                            )
                        }
                    }
                }
            }
        }
    }
}
