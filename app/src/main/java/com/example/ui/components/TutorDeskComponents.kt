package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeStatus
import com.example.ui.theme.AmberBg
import com.example.ui.theme.AmberText
import com.example.ui.theme.CoralBg
import com.example.ui.theme.CoralBorder
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralText
import com.example.ui.theme.CurrencyStatTextStyle
import com.example.ui.theme.DeepSlateNavy
import com.example.ui.theme.DestructiveText
import com.example.ui.theme.EmeraldBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldText
import com.example.ui.theme.InteractiveBlue
import com.example.ui.theme.InteractiveBlueBg
import com.example.ui.theme.InteractiveBlueDeep
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceLow
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TabularBodyStyle
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.TutorTab
import java.text.NumberFormat
import java.util.Locale

fun formatBdt(amount: Long): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
    return "৳\u00A0$formatted"
}

@Composable
fun TutorDeskCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    borderColor: Color = Slate200,
    elevation: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = SurfaceWhite,
            shadowElevation = elevation,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = SurfaceWhite,
            shadowElevation = elevation,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun KpiStatCard(
    label: String,
    value: String,
    valueColor: Color,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    TutorDeskCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        borderColor = Slate200,
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall,
                color = Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = CurrencyStatTextStyle,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    label: String,
    containerColor: Color,
    textColor: Color,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun FeeStatusBadge(status: FeeStatus, modifier: Modifier = Modifier) {
    when (status) {
        FeeStatus.PAID -> StatusBadge("Paid", EmeraldBg, EmeraldText, EmeraldGreen, modifier)
        FeeStatus.PARTIAL -> StatusBadge("Partial", AmberBg, AmberText, WarmAmber, modifier)
        FeeStatus.DUE -> StatusBadge("Due", CoralBg, CoralText, CoralRed, modifier)
    }
}

@Composable
fun InfoPill(
    text: String,
    containerColor: Color = InteractiveBlueBg,
    textColor: Color = InteractiveBlueDeep,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun SegmentedAttendanceToggle(
    selectedStatus: AttendanceStatus?,
    onSelect: (AttendanceStatus) -> Unit,
    studentIdTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(SurfaceLow)
            .border(1.dp, Slate200, CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AttendanceStatus.entries.forEach { status ->
            val isSelected = selectedStatus == status
            val targetBg = when {
                !isSelected -> Color.Transparent
                status == AttendanceStatus.PRESENT -> EmeraldGreen
                status == AttendanceStatus.LATE -> WarmAmber
                else -> CoralRed
            }
            val bgColor by animateColorAsState(
                targetValue = targetBg,
                animationSpec = spring(),
                label = "att_segment_bg"
            )
            val textColor = if (isSelected) SurfaceWhite else Slate500
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 44.dp, minHeight = 40.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onSelect(status) }
                    )
                    .testTag("att_${studentIdTag}_${status.shortLabel}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = status.shortLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun TutorDeskTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    testTag: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) DeepSlateNavy else Slate200
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Slate700
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceWhite)
                .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Slate900),
                cursorBrush = SolidColor(DeepSlateNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
                    .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
            )
        }
    }
}

@Composable
fun BdtCurrencyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "1500",
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) DeepSlateNavy else Slate200
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Slate700
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceWhite)
                .border(borderWidth, borderColor, RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(SurfaceLow)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "৳",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate500
                )
            }
            VerticalDivider(thickness = 1.dp, color = Slate200)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TabularBodyStyle,
                        color = Slate400
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(6)
                        onValueChange(digits)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TabularBodyStyle.copy(color = Slate900),
                    cursorBrush = SolidColor(DeepSlateNavy),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                        .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
                )
            }
        }
    }
}

@Composable
fun TutorDeskPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepSlateNavy,
            contentColor = SurfaceWhite,
            disabledContainerColor = Slate300,
            disabledContentColor = SurfaceWhite
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TutorDeskSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Slate300),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfaceWhite,
            contentColor = Slate700
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TutorDeskDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = ""
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CoralBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CoralBg,
            contentColor = DestructiveText
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FilterPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = DeepSlateNavy,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) activeColor else SurfaceWhite
    val borderColor = if (selected) activeColor else Slate200
    val textColor = if (selected) SurfaceWhite else Slate700

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun TutorDeskTopBar(
    title: String,
    subtitle: String,
    userEmail: String?,
    onSignOut: () -> Unit,
    actionContent: @Composable (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = DeepSlateNavy,
        contentColor = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp
                    ),
                    color = SurfaceWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate300,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actionContent?.invoke()

                Box {
                    val initials = (userEmail?.firstOrNull()?.uppercaseChar() ?: 'T').toString()
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("account_menu_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(InteractiveBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.labelLarge,
                                color = SurfaceWhite
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = SurfaceWhite
                    ) {
                        if (!userEmail.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = userEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                },
                                onClick = { },
                                enabled = false
                            )
                            HorizontalDivider(color = Slate200)
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Sign out",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = DestructiveText
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign out",
                                    tint = DestructiveText
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onSignOut()
                            },
                            modifier = Modifier.testTag("sign_out_menu_item")
                        )
                    }
                }
            }
        }
    }
}

data class NavTabSpec(
    val tab: TutorTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val TutorNavTabs = listOf(
    NavTabSpec(TutorTab.BATCHES, "Batches", Icons.Filled.Class, Icons.Outlined.Class, "nav_batches"),
    NavTabSpec(TutorTab.ATTENDANCE, "Attendance", Icons.Filled.Checklist, Icons.Outlined.Checklist, "nav_attendance"),
    NavTabSpec(TutorTab.FEES, "Fees", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_fees"),
    NavTabSpec(TutorTab.STUDENTS, "Students", Icons.Filled.People, Icons.Outlined.People, "nav_students")
)

@Composable
fun TutorDeskBottomNav(
    selectedTab: TutorTab,
    onSelectTab: (TutorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceWhite.copy(alpha = 0.96f),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(thickness = 1.dp, color = Slate200)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TutorNavTabs.forEach { spec ->
                    val isSelected = selectedTab == spec.tab
                    val tint = if (isSelected) DeepSlateNavy else Slate400
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onSelectTab(spec.tab) }
                            )
                            .testTag(spec.testTag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) spec.selectedIcon else spec.unselectedIcon,
                            contentDescription = spec.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = spec.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = tint,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DeepSlateNavy else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
