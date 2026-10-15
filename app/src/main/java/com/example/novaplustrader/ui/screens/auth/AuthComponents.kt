package com.example.novaplustrader.ui.screens.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun NovaBrandMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CurrencyBitcoin,
            contentDescription = "Bitcoin",
            tint = ElectricLime,
            modifier = Modifier.padding(10.dp).size(44.dp)
        )
    }
}

@Composable
fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(SurfaceBorder))
        Text(
            text = "OR",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Box(Modifier.weight(1f).height(1.dp).background(SurfaceBorder))
    }
}

@Composable
fun GoogleSignInButton(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onBeforeSignIn: () -> Boolean = { true },
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showAddAccount by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .alpha(if (enabled) 1f else 0.5f)
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled && !isLoading) {
                if (!onBeforeSignIn()) return@clickable
                viewModel.clearError()
                showAddAccount = false
                isLoading = true
                scope.launch {
                    try {
                        val clientIdResource = context.resources.getIdentifier(
                            "default_web_client_id",
                            "string",
                            context.packageName
                        )
                        check(clientIdResource != 0) {
                            "Google sign-in needs a Web client ID. Enable Google in Firebase Authentication, then download the updated google-services.json."
                        }
                        val serverClientId = context.getString(clientIdResource)
                        check(serverClientId.isNotBlank()) {
                            "Google sign-in needs a Web client ID. Enable Google in Firebase Authentication, then download the updated google-services.json."
                        }

                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(serverClientId)
                            .setAutoSelectEnabled(false)
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                        val result = CredentialManager.create(context)
                            .getCredential(context, request)
                        val credential = result.credential
                        check(
                            credential is CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            "Google returned an unsupported sign-in credential."
                        }
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.signInWithGoogle(googleCredential.idToken, onSuccess)
                    } catch (e: NoCredentialException) {
                        showAddAccount = true
                        viewModel.showError("No Google account is available on this device. Add an account to continue.")
                    } catch (e: GetCredentialException) {
                        viewModel.showError(e.localizedMessage ?: "Google sign-in could not be completed.")
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        viewModel.showError(e.localizedMessage ?: "Google sign-in could not be completed.")
                    } finally {
                        isLoading = false
                    }
                }
            }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = ElectricLime,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "G",
                color = Color(0xFF4285F4),
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isLoading) "Checking Google accounts..." else "Continue with Google",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    if (showAddAccount) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add a Google account to this device",
            color = ElectricLime,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_ADD_ACCOUNT)
                                .putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (e: ActivityNotFoundException) {
                        viewModel.showError("This device does not provide an account setup screen.")
                    }
                }
                .padding(vertical = 8.dp, horizontal = 12.dp)
        )
    }
}
