package com.example.smritisetu.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smritisetu.data.AppLanguage
import com.example.smritisetu.data.AuthManager
import com.example.smritisetu.data.LocalAppStrings
import com.example.smritisetu.data.UserRole
import com.example.smritisetu.theme.GlassCard
import com.example.smritisetu.theme.getGlassGradientBrush
import com.example.smritisetu.theme.isAppInDarkTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by AuthManager.currentUser.collectAsState()
    val themeMode by AuthManager.themeMode.collectAsState()
    val selectedLanguage by AuthManager.selectedLanguage.collectAsState()
    val darkTheme = isAppInDarkTheme(themeMode)
    val strings = LocalAppStrings.current

    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var selectedRole by remember { mutableStateOf(currentUser?.role ?: UserRole.CAREGIVER) }
    var currentLang by remember { mutableStateOf(selectedLanguage) }
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Role
                        Text(
                            text = "Role",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = selectedRole == UserRole.CAREGIVER,
                                onClick = { selectedRole = UserRole.CAREGIVER },
                                label = { Text(strings.roleCaregiver) },
                                leadingIcon = if (selectedRole == UserRole.CAREGIVER) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedRole == UserRole.PATIENT,
                                onClick = { selectedRole = UserRole.PATIENT },
                                label = { Text(strings.rolePatient) },
                                leadingIcon = if (selectedRole == UserRole.PATIENT) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Language Preference Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isLanguageDropdownExpanded,
                            onExpandedChange = { isLanguageDropdownExpanded = !isLanguageDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = "${currentLang.nativeName} (${currentLang.displayName})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.language) },
                                leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = isLanguageDropdownExpanded,
                                onDismissRequest = { isLanguageDropdownExpanded = false }
                            ) {
                                AppLanguage.entries.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text("${lang.nativeName} - ${lang.displayName}") },
                                        onClick = {
                                            currentLang = lang
                                            isLanguageDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        AuthManager.updateProfile(name, phone, currentLang.displayName, selectedRole)
                        AuthManager.setLanguage(currentLang)
                        scope.launch {
                            snackbarHostState.showSnackbar("Profile updated successfully")
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
