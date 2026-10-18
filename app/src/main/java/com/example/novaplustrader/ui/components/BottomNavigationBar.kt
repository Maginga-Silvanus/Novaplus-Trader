package com.example.novaplustrader.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.navigation.Screen
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeGlow
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    NavItem(Screen.Home.route, "Home", Icons.Default.Home),
    NavItem(Screen.Markets.route, "Markets", Icons.Default.ShowChart),
    NavItem("center_action", "", Icons.Default.Add), // Center prominent +
    NavItem(Screen.Crypto.route, "Crypto", Icons.Default.CurrencyBitcoin),
    NavItem(Screen.Activity.route, "Activity", Icons.Default.Analytics)
)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    onCenterPlusClick: () -> Unit,
    isQuickMenuExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val plusRotation by animateFloatAsState(
        targetValue = if (isQuickMenuExpanded) 45f else 0f,
        label = "PlusButtonRotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDarkCard)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    if (item.route == "center_action") {
                        // Prominent Center Plus Button Placeholder
                        Box(modifier = Modifier.size(52.dp))
                    } else {
                        val isSelected = currentRoute == item.route
                        val iconColor = if (isSelected) ElectricLime else TextMuted

                        Column(
                            modifier = Modifier
                                .clickable { onNavigateToRoute(item.route) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = item.title,
                                color = iconColor,
                                fontSize = 9.sp,
                                maxLines = 1,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Prominent Center Plus FAB
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(56.dp)
                .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = ElectricLimeGlow, spotColor = ElectricLimeGlow)
                .clip(CircleShape)
                .background(ElectricLime)
                .clickable { onCenterPlusClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick Actions",
                tint = BackgroundDark,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(plusRotation)
            )
        }
    }
}
