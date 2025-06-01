
package com.ui.autosaveui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.ui.autosaveui.ui.theme.AutosaveUiTheme
class NumberListActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "MyNumberPrefs"
    private val KEY_NUMBERS = "stored_numbers"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setContent {
            AutosaveUiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NumberListScreen(
                        // Pass functions for saving and loading numbers
                        onSaveNumber = { number -> saveNumber(number) },
                        onLoadNumbers = { loadNumbers() },
                        onClearNumbers = { clearAllNumbers() }
                    )
                }
            }
        }
    }

    // Function to save a new number
    private fun saveNumber(newNumber: Int) {
        val currentNumbers = loadNumbers().toMutableSet()
        currentNumbers.add(newNumber.toString()) // SharedPreferences stores Set<String>
        with(sharedPreferences.edit()) {
            putStringSet(KEY_NUMBERS, currentNumbers)
            apply() // Asynchronously saves the changes
        }
    }

    // Function to load all numbers
    private fun loadNumbers(): List<String> {
        return sharedPreferences.getStringSet(KEY_NUMBERS, emptySet())?.toList() ?: emptyList()
    }

    // Function to clear all numbers
    private fun clearAllNumbers() {
        with(sharedPreferences.edit()) {
            remove(KEY_NUMBERS)
            apply()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberListScreen(
    onSaveNumber: (Int) -> Unit,
    onLoadNumbers: () -> List<String>,
    onClearNumbers: () -> Unit
) {
    // MutableState for the list of numbers displayed in the UI
    val numbers = remember { mutableStateListOf<String>() }

    // MutableState for the input field
    var newNumberInput by remember { mutableStateOf("") }

    // Load numbers when the composable is first launched
    LaunchedEffect(Unit) {
        numbers.clear()
        numbers.addAll(onLoadNumbers())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Number List") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input field and Save button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newNumberInput,
                    onValueChange = { newNumberInput = it },
                    label = { Text("Enter Number") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number // Specify numeric keyboard
                    )
                )
                Button(
                    onClick = {
                        newNumberInput.toIntOrNull()?.let {
                            onSaveNumber(it)
                            // Update the displayed list after saving
                            numbers.clear()
                            numbers.addAll(onLoadNumbers())
                            newNumberInput = "" // Clear input field
                        }
                    },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clear All button
            Button(
                onClick = {
                    onClearNumbers()
                    numbers.clear() // Clear the displayed list
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear All Numbers")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display list of numbers
            if (numbers.isNotEmpty()) {
                LazyColumn {
                    items(numbers) { number ->
                        Text(
                            text = number,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                        Divider()
                    }
                }
            } else {
                Text("No numbers saved yet.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AutosaveUiTheme {
        NumberListScreen(
            onSaveNumber = { /* Do nothing for preview */ },
            onLoadNumbers = { listOf("123", "456") }, // Sample data for preview
            onClearNumbers = { /* Do nothing for preview */ }
        )
    }
}