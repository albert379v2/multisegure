package com.multisegure.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.multisegure.MultiSegureApplication
import com.multisegure.data.model.BrowserProfile
import com.multisegure.data.repository.ProfileRepository
import com.multisegure.ui.browser.BrowserActivity
import com.multisegure.ui.profile.ProfileEditActivity
import com.multisegure.ui.theme.MultiSegureTheme
import com.multisegure.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiSegureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfilesScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as MultiSegureApplication
    val repository = remember { ProfileRepository(application.database.profileDao()) }
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(repository))

    var profiles by remember { mutableStateOf(listOf<BrowserProfile>()) }
    var showDeleteDialog by remember { mutableStateOf<BrowserProfile?>(null) }

    LaunchedEffect(Unit) {
        viewModel.allProfiles.collectLatest { list ->
            profiles = list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MultiSegure") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, ProfileEditActivity::class.java))
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Perfil")
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sin perfiles",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Crea un perfil para empezar a navegar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onClick = {
                            val intent = Intent(context, BrowserActivity::class.java).apply {
                                putExtra("profile_id", profile.id)
                                putExtra("profile_name", profile.name)
                                putExtra("proxy_host", profile.proxyHost)
                                putExtra("proxy_port", profile.proxyPort)
                                putExtra("proxy_username", profile.proxyUsername)
                                putExtra("proxy_password", profile.proxyPassword)
                                putExtra("proxy_type", profile.proxyType)
                                putExtra("user_agent", profile.userAgent)
                                putExtra("incognito", profile.isIncognito)
                                putExtra("javascript", profile.isJavaScriptEnabled)
                                putExtra("block_trackers", profile.blockTrackers)
                            }
                            context.startActivity(intent)
                        },
                        onEdit = {
                            val intent = Intent(context, ProfileEditActivity::class.java).apply {
                                putExtra("profile_id", profile.id)
                            }
                            context.startActivity(intent)
                        },
                        onDelete = { showDeleteDialog = profile }
                    )
                }
            }
        }
    }

    showDeleteDialog?.let { profile ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Perfil") },
            text = { Text("¿Eliminar ${profile.name}? Se perderán todos los datos de sesión.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProfile(profile)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: BrowserProfile,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profile.proxyType != "NONE") {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(profile.proxyType) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    if (profile.isIncognito) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Incógnito") }
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
