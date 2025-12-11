package com.example.compost2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.compost2.data.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyScreen(
    serviceType: String, // "openai" или "wordpress"
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { SettingsDataStore(context) }

    // Состояния
    var openAiKey by remember { mutableStateOf("") }
    var wpUrl by remember { mutableStateOf("") }
    var wpUsername by remember { mutableStateOf("") }
    var wpPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }

    val title = if (serviceType == "openai") "OpenAI Settings" else "WordPress Settings"

    // Загрузка данных
    LaunchedEffect(Unit) {
        if (serviceType == "openai") {
            openAiKey = dataStore.openAiKey.first() ?: ""
        } else {
            // Читаем правильные поля из DataStore
            wpUrl = dataStore.wpUrl.first() ?: ""
            wpUsername = dataStore.wpUsername.first() ?: ""
            wpPassword = dataStore.wpPassword.first() ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            if (serviceType == "openai") {
                // --- OPEN AI ---
                Text("Enter your OpenAI API Key (sk-...).")
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = openAiKey,
                    onValueChange = { openAiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (isPasswordVisible) "Hide" else "Show",
                            modifier = Modifier
                                .clickable { isPasswordVisible = !isPasswordVisible }
                                .padding(8.dp)
                        )
                    }
                )
            } else {
                // --- WORDPRESS ---
                Text("Enter your WordPress site details.")
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = wpUrl,
                    onValueChange = { wpUrl = it },
                    label = { Text("Site URL (https://...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = wpUsername,
                    onValueChange = { wpUsername = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = wpPassword,
                    onValueChange = { wpPassword = it },
                    label = { Text("Application Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (isPasswordVisible) "Hide" else "Show",
                            modifier = Modifier
                                .clickable { isPasswordVisible = !isPasswordVisible }
                                .padding(8.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (serviceType == "openai") {
                            dataStore.saveOpenAiKey(openAiKey)
                        } else {
                            // ИСПРАВЛЕНО: Вызываем правильный метод с 3 аргументами
                            dataStore.saveWordPressSettings(wpUrl, wpUsername, wpPassword)
                        }
                        Toast.makeText(context, "Settings Saved!", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings")
            }
        }
    }
}