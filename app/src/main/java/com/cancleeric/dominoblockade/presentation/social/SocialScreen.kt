package com.cancleeric.dominoblockade.presentation.social

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cancleeric.dominoblockade.domain.model.ChallengeInvitation
import com.cancleeric.dominoblockade.domain.model.Friend
import com.cancleeric.dominoblockade.domain.model.FriendRequest
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.mlkit.common.MlKitException
import com.google.android.gms.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.android.gms.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private const val PADDING_DP = 16
private const val SPACING_DP = 8
private const val QR_SIZE_PX = 420

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    onBack: () -> Unit,
    onNavigateToLobby: (String) -> Unit,
    challengeIdFromDeepLink: String? = null,
    challengeActionFromDeepLink: String? = null,
    modifier: Modifier = Modifier,
    viewModel: SocialViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var challengeMode by rememberSaveable { mutableStateOf("Classic") }
    val context = LocalContext.current

    LaunchedEffect(challengeIdFromDeepLink, challengeActionFromDeepLink) {
        viewModel.applyChallengeAction(challengeIdFromDeepLink, challengeActionFromDeepLink)
    }
    LaunchedEffect(uiState.navigateToLobbyRoomId) {
        uiState.navigateToLobbyRoomId?.let { roomId ->
            onNavigateToLobby(roomId)
            viewModel.consumeLobbyNavigation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends & Challenges") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)
        ) {
            item {
                UsernameSection(
                    username = uiState.username,
                    isSaving = uiState.isSavingUsername,
                    onUsernameChanged = viewModel::setUsername,
                    onSave = viewModel::saveUsername
                )
            }
            item {
                QrSection(
                    qrValue = uiState.qrValue,
                    onScan = {
                        startQrScan(
                            context = context,
                            onValue = viewModel::onQrScanned
                        )
                    }
                )
            }
            item {
                SearchSection(
                    query = uiState.searchQuery,
                    results = uiState.searchResults,
                    onQueryChanged = viewModel::setSearchQuery,
                    onSendRequest = viewModel::sendFriendRequest
                )
            }
            item {
                OutlinedTextField(
                    value = challengeMode,
                    onValueChange = { challengeMode = it },
                    label = { Text("Challenge Game Mode") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                InboxSection(
                    friendRequests = uiState.incomingFriendRequests,
                    challenges = uiState.pendingChallenges,
                    onRespondFriendRequest = viewModel::respondToFriendRequest,
                    onRespondChallenge = viewModel::respondToChallenge
                )
            }
            item {
                FriendsSection(
                    friends = uiState.friends,
                    onChallenge = { friend -> viewModel.sendChallenge(friend, challengeMode) }
                )
            }
            item {
                val error = uiState.error
                if (!error.isNullOrBlank()) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun UsernameSection(
    username: String,
    isSaving: Boolean,
    onUsernameChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(PADDING_DP.dp), verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
            Text(text = "Your Username", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(onClick = onSave, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
                Text(text = if (isSaving) "Saving..." else "Save Username")
            }
        }
    }
}

@Composable
private fun QrSection(qrValue: String, onScan: () -> Unit) {
    val bitmap = remember(qrValue) { qrValue.takeIf { it.isNotBlank() }?.let(::generateQrBitmap) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(PADDING_DP.dp), verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
            Text(text = "Add Friend with QR", style = MaterialTheme.typography.titleMedium)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Friend QR code",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                )
            } else {
                Text("QR will appear after authentication")
            }
            Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) { Text("Scan Friend QR") }
        }
    }
}

@Composable
private fun SearchSection(
    query: String,
    results: List<Friend>,
    onQueryChanged: (String) -> Unit,
    onSendRequest: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(PADDING_DP.dp), verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
            Text(text = "Search Players", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            results.forEach { result ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(result.username, modifier = Modifier.weight(1f))
                    Button(onClick = { onSendRequest(result.uid) }) { Text("Add") }
                }
            }
        }
    }
}

@Composable
private fun InboxSection(
    friendRequests: List<FriendRequest>,
    challenges: List<ChallengeInvitation>,
    onRespondFriendRequest: (String, Boolean) -> Unit,
    onRespondChallenge: (String, Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(PADDING_DP.dp), verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
            Text(text = "Inbox", style = MaterialTheme.typography.titleMedium)
            if (friendRequests.isEmpty() && challenges.isEmpty()) {
                Text("No pending friend requests or challenges")
            }
            friendRequests.forEach { request ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${request.fromUsername} sent a friend request", modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
                        Button(onClick = { onRespondFriendRequest(request.id, true) }) { Text("Accept") }
                        Button(onClick = { onRespondFriendRequest(request.id, false) }) { Text("Reject") }
                    }
                }
            }
            challenges.forEach { challenge ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "${challenge.challengerName} challenged you (${challenge.gameMode})",
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
                        Button(onClick = { onRespondChallenge(challenge.id, true) }) { Text("Accept") }
                        Button(onClick = { onRespondChallenge(challenge.id, false) }) { Text("Decline") }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsSection(
    friends: List<Friend>,
    onChallenge: (Friend) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(PADDING_DP.dp), verticalArrangement = Arrangement.spacedBy(SPACING_DP.dp)) {
            Text(text = "Friends", style = MaterialTheme.typography.titleMedium)
            if (friends.isEmpty()) {
                Text("No friends yet")
            }
            friends.forEach { friend ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${friend.username} • ${if (friend.isOnline) "Online" else "Offline"}")
                    Button(onClick = { onChallenge(friend) }) { Text("Challenge") }
                }
            }
        }
    }
}

private fun generateQrBitmap(content: String): Bitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX)
    val bitmap = Bitmap.createBitmap(QR_SIZE_PX, QR_SIZE_PX, Bitmap.Config.RGB_565)
    for (x in 0 until QR_SIZE_PX) {
        for (y in 0 until QR_SIZE_PX) {
            bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

private fun startQrScan(context: Context, onValue: (String) -> Unit) {
    val activity = context.findActivity() ?: return
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    val scanner = GmsBarcodeScanning.getClient(activity, options)
    scanner.startScan()
        .addOnSuccessListener { barcode ->
            barcode.rawValue?.let(onValue)
        }
        .addOnFailureListener { error ->
            if (error is MlKitException && error.errorCode == CommonStatusCodes.CANCELED) return@addOnFailureListener
        }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
