package com.multisegure.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.multisegure.MultiSegureApplication
import com.multisegure.data.model.BrowserProfile
import com.multisegure.data.repository.ProfileRepository
import com.multisegure.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    profileId: Int,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as MultiSegureApplication
    val repository = remember { ProfileRepository(application.database.profileDao()) }
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(repository))
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var proxyHost by remember { mutableStateOf("") }
    var proxyPort by remember { mutableStateOf("") }
    var proxyUsername by remember { mutableStateOf("") }
    var proxyPassword by remember { mutableStateOf("") }
    var proxyType by remember { mutableStateOf("NONE") }
    var userAgent by remember { mutableStateOf("") }
    var isIncognito by remember { mutableStateOf(false) }
    var isJavaScriptEnabled by remember { mutableStateOf(true) }
    var blockTrackers by remember { mutableStateOf(false) }

    val proxyTypes = listOf("NONE", "HTTP", "HTTPS", "SOCKS4", "SOCKS5")
    val userAgents = listOf(
        "" to "Default",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36" to "Chrome Windows",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36" to "Chrome Mac",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36" to "Chrome Linux",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1" to "Safari iPhone",
        "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36" to "Chrome Android"
    )

    LaunchedEffect(profileId) {
        if (profileId != 0) {
            viewModel.loadProfile(profileId)
        }
    }

    val currentProfile by viewModel.currentProfile.collectAsState()

    LaunchedEffect(currentProfile) {
        currentProfile?.let { p ->
            name = p.name
            proxyHost = p.proxyHost
            proxyPort = if (p.proxyPort > 0) p.proxyPort.toString() else ""
            proxyUsername = p.proxyUsername
            proxyPassword = p.proxyPassword
            proxyType = p.proxyType
            userAgent = p.userAgent
            isIncognito = p.isIncognito
            isJavaScriptEnabled = p.isJavaScriptEnabled
            blockTrackers = p.blockTrackers
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileId == 0) "Nuevo Perfil" else "Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val profile = BrowserProfile(
                                id = profileId,
                                name = name.ifBlank { "Perfil ${System.currentTimeMillis()}" },
                                proxyHost = proxyHost,
                                proxyPort = proxyPort.toIntOrNull() ?: 0,
                                proxyUsername = proxyUsername,
                                proxyPassword = proxyPassword,
                                proxyType = proxyType,
                                userAgent = userAgent,
                                isIncognito = isIncognito,
                                isJavaScriptEnabled = isJavaScriptEnabled,
                                blockTrackers = blockTrackers
                            )
                            scope.launch {
                                viewModel.saveProfile(profile) { onFinish() }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Perfil *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("User-Agent", style = MaterialTheme.typography.titleSmall)
            userAgents.forEach { (ua, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = userAgent == ua,
                            onClick = { userAgent = ua },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = userAgent == ua,
                        onClick = null
                    )
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            Text("Configuración Proxy", style = MaterialTheme.typography.titleMedium)

            proxyTypes.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = proxyType == type,
                            onClick = { proxyType = type },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = proxyType == type,
                        onClick = null
                    )
                    Text(
                        text = type,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (proxyType != "NONE") {
                OutlinedTextField(
                    value = proxyHost,
                    onValueChange = { proxyHost = it },
                    label = { Text("Host del Proxy") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = proxyPort,
                    onValueChange = { proxyPort = it.filter { c -> c.isDigit() } },
                    label = { Text("Puerto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = proxyUsername,
                    onValueChange = { proxyUsername = it },
                    label = { Text("Usuario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = proxyPassword,
                    onValueChange = { proxyPassword = it },
                    label = { Text("Contraseña (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            HorizontalDivider()

            Text("Opciones del Perfil", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Modo Incógnito")
                Switch(
                    checked = isIncognito,
                    onCheckedChange = { isIncognito = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("JavaScript habilitado")
                Switch(
                    checked = isJavaScriptEnabled,
                    onCheckedChange = { isJavaScriptEnabled = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bloquear trackers")
                Switch(
                    checked = blockTrackers,
                    onCheckedChange = { blockTrackers = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
