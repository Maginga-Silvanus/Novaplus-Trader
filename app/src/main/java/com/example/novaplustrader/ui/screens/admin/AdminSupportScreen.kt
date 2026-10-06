package com.example.novaplustrader.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.EmptyState
import com.example.novaplustrader.ui.components.LoadingState
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.screens.admin.components.AdminFilterChipBar
import com.example.novaplustrader.ui.screens.admin.components.AdminHeaderBar
import com.example.novaplustrader.ui.screens.admin.components.AdminSearchBar
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportTicketModel
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportTicketStatus
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialGreenGlow
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.FinancialRedGlow
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportScreen(
    viewModel: AdminSupportViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val statusFilterOptions = listOf("ALL", "OPEN", "IN_PROGRESS", "PENDING_USER", "RESOLVED", "CLOSED")
    val priorityFilterOptions = listOf("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Support Desk Queue",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            AdminSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = "Search ticket ID, user or subject..."
            )
        }

        AdminFilterChipBar(
            options = statusFilterOptions,
            selectedOption = uiState.selectedStatusFilter,
            onOptionSelected = viewModel::onStatusFilterSelected
        )

        AdminFilterChipBar(
            options = priorityFilterOptions,
            selectedOption = uiState.selectedPriorityFilter,
            onOptionSelected = viewModel::onPriorityFilterSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.actionSuccessMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElectricLimeSurface)
                    .border(1.dp, ElectricLime, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = uiState.actionSuccessMessage!!,
                    color = ElectricLime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (uiState.isLoading && uiState.tickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        } else if (uiState.tickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No Support Tickets",
                    message = "No tickets match the selected status or priority filter."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.tickets, key = { it.ticketId }) { ticket ->
                    AdminTicketRow(
                        ticket = ticket,
                        onClick = { viewModel.selectTicket(ticket) }
                    )
                }
            }
        }

        // Ticket Details & Response BottomSheet
        if (uiState.selectedTicketDetails != null) {
            val t = uiState.selectedTicketDetails!!

            ModalBottomSheet(
                onDismissRequest = { viewModel.selectTicket(null) },
                containerColor = BackgroundDark,
                scrimColor = OverlayDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ticket #${t.ticketId}",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        SecondaryButton(
                            text = "Assign to Me",
                            onClick = { viewModel.assignToMe("Operator Sarah") },
                            modifier = Modifier.width(130.dp)
                        )
                    }

                    Text(
                        text = "Subject: ${t.subject} (${t.userName})",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    // Message list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDarkElevated)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(t.messages, key = { it.id }) { msg ->
                            val isInternal = msg.isInternalNote
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isInternal) FinancialRedGlow else SurfaceDarkCard)
                                    .border(1.dp, if (isInternal) FinancialRed else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isInternal) "[INTERNAL OPERATIONAL NOTE] ${msg.authorName}" else msg.authorName,
                                        color = if (isInternal) FinancialRed else ElectricLime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = msg.timestamp, color = TextMuted, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = msg.message, color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }

                    // Reply form
                    OutlinedTextField(
                        value = uiState.replyText,
                        onValueChange = viewModel::updateReplyText,
                        label = { Text("Write response or internal note...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.toggleInternalNote(!uiState.isInternalNote) }
                    ) {
                        Checkbox(
                            checked = uiState.isInternalNote,
                            onCheckedChange = viewModel::toggleInternalNote,
                            colors = CheckboxDefaults.colors(checkedColor = FinancialRed)
                        )
                        Text(
                            text = "Internal Note Only (Hidden from end user)",
                            color = if (uiState.isInternalNote) FinancialRed else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecondaryButton(
                            text = "Mark Resolved",
                            onClick = { viewModel.updateTicketStatus(AdminSupportTicketStatus.RESOLVED) },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = if (uiState.isInternalNote) "Add Note" else "Send Reply",
                            onClick = viewModel::submitReply,
                            isLoading = uiState.isSubmittingReply,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminTicketRow(
    ticket: AdminSupportTicketModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElectricLimeSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = ticket.category,
                    tint = ElectricLime,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ticket.subject,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricLimeSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticket.status.label,
                            color = ElectricLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${ticket.userName} • Assigned: ${ticket.assignedOperator}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "Priority: ${ticket.priority.label} • Updated: ${ticket.lastUpdate}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Details",
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
