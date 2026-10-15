package com.example.novaplustrader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.domain.model.access.AppFeature
import com.example.novaplustrader.domain.model.access.EligibilityStatus
import com.example.novaplustrader.domain.model.access.FeatureAccessModel
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@Composable
fun FeatureGate(
    feature: AppFeature,
    accessModel: FeatureAccessModel,
    modifier: Modifier = Modifier,
    onActionClick: ((String) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (accessModel.isAvailable) {
        content()
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LockedFeatureCard(
                feature = feature,
                accessModel = accessModel,
                onActionClick = onActionClick
            )
        }
    }
}

@Composable
fun LockedFeatureCard(
    feature: AppFeature,
    accessModel: FeatureAccessModel,
    onActionClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ElectricLimeSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Feature Restricted",
                tint = ElectricLime,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = feature.displayName,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        EligibilityStatusBadge(status = accessModel.status)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = accessModel.userMessage,
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        if (!accessModel.actionRoute.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = when (accessModel.status) {
                    EligibilityStatus.KYC_REQUIRED, EligibilityStatus.AGE_VERIFICATION_REQUIRED -> "COMPLETE KYC VERIFICATION"
                    EligibilityStatus.ACCOUNT_SUSPENDED, EligibilityStatus.RISK_RESTRICTED -> "CONTACT SUPPORT DESK"
                    else -> "VIEW DETAILS"
                },
                onClick = { onActionClick(accessModel.actionRoute) }
            )
        }
    }
}

@Composable
fun EligibilityStatusBadge(
    status: EligibilityStatus,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (status) {
        EligibilityStatus.AVAILABLE -> ElectricLime
        EligibilityStatus.KYC_REQUIRED, EligibilityStatus.KYC_REVIEW, EligibilityStatus.MAINTENANCE -> ElectricLime
        EligibilityStatus.ACCOUNT_SUSPENDED, EligibilityStatus.RISK_RESTRICTED, EligibilityStatus.FEATURE_DISABLED -> FinancialRed
        else -> TextMuted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label.uppercase(),
            color = badgeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}
