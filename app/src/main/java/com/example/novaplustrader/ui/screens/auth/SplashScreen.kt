package com.example.novaplustrader.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.novaplustrader.domain.model.auth.AuthUiState
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(viewModel) {
        viewModel.checkCurrentUser()
        val authState = viewModel.uiState.first {
            it is AuthUiState.Authenticated || it is AuthUiState.Unauthenticated
        }
        delay(1600L) // 1.6s premium splash duration
        when (authState) {
            is AuthUiState.Authenticated -> onNavigateToHome()
            else -> onNavigateToWelcome()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(SurfaceDarkCard)
                    .border(1.dp, SurfaceBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.novaplustrader.R.mipmap.my_icon_launcher_foreground),
                    contentDescription = "Splash Logo",
                    modifier = Modifier.size(68.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "NovaPlus+ Trader",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Elite Match Trading & Bitcoin Terminal",
                color = TextMuted,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
