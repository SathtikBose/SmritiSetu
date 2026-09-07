package com.example.smritisetu.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smritisetu.data.AuthManager
import com.example.smritisetu.data.LocalAppStrings
import com.example.smritisetu.theme.GlassCard
import com.example.smritisetu.theme.getGlassGradientBrush
import com.example.smritisetu.theme.isAppInDarkTheme
import kotlinx.coroutines.launch

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by AuthManager.currentUser.collectAsState()
    val themeMode by AuthManager.themeMode.collectAsState()
    val darkTheme = isAppInDarkTheme(themeMode)
    val strings = LocalAppStrings.current

    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var gender by remember { mutableStateOf(currentUser?.gender ?: "") }
    var ageText by remember { mutableStateOf(currentUser?.age?.takeIf { it > 0 }?.toString() ?: "") }
    var avatarUri by remember { mutableStateOf(currentUser?.avatarUri) }
    var isUploadingAvatar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isUploadingAvatar = true
            avatarUri = uri.toString()
            AuthManager.uploadAvatar(context, uri) { result ->
                isUploadingAvatar = false
                if (result.isSuccess) {
                    avatarUri = result.getOrNull()
                    scope.launch { snackbarHostState.showSnackbar("Profile picture uploaded successfully!") }
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Profile picture saved locally") }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.editProfile) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(getGlassGradientBrush(darkTheme))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Avatar with Camera / Gallery trigger
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    darkTheme = darkTheme
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingAvatar) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(36.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp
                                    )
                                } else if (!avatarUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(avatarUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Photo",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }

                            // Camera / Edit badge
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = strings.choosePhotoSource,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isUploadingAvatar) "Uploading to Cloud..." else strings.choosePhotoSource,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                        )
                    }
                }

                // Profile Fields Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    darkTheme = darkTheme
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(strings.fullName) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(strings.phoneNumber) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Gender Selection
                        Text(
                            text = strings.gender,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = gender == "Male",
                                onClick = { gender = "Male" },
                                label = { Text(strings.genderMale) },
                                leadingIcon = if (gender == "Male") {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = gender == "Female",
                                onClick = { gender = "Female" },
                                label = { Text(strings.genderFemale) },
                                leadingIcon = if (gender == "Female") {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = gender == "Other",
                                onClick = { gender = "Other" },
                                label = { Text(strings.genderOther) },
                                leadingIcon = if (gender == "Other") {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Age Input
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { if (it.all { char -> char.isDigit() }) ageText = it },
                            label = { Text(strings.age) },
                            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Save Button
                Button(
                    onClick = {
                        val ageInt = ageText.toIntOrNull() ?: 0
                        AuthManager.updateProfile(name, phone, gender, ageInt, avatarUri)
                        scope.launch {
                            snackbarHostState.showSnackbar(strings.profileUpdated)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.save, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
