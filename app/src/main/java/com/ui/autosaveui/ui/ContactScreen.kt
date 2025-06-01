package com.ui.autosaveui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
// import androidx.compose.material3.MaterialTheme // Removed
// import androidx.compose.material3.Surface // Removed
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ui.autosaveui.viewmodel.ContactViewModel
import com.ui.autosaveui.model.ContactEntry
import android.util.Log

@Composable
fun ContactScreen(
    viewModel: ContactViewModel = viewModel(),
    extractedNumbers: List<String>
) {

    LaunchedEffect(extractedNumbers) {
        viewModel.loadContacts(extractedNumbers)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Series Name and Start From TextFields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = viewModel.seriesName.value,
                onValueChange = {
                    viewModel.seriesName.value = it
                    viewModel.updateContactNames()
                },
                label = { Text("Series Name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextField(
                value = viewModel.startNumber.value,
                onValueChange = {
                    viewModel.startNumber.value = it
                    viewModel.updateContactNames()
                },
                label = { Text("Start From") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Editable Contact List
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.contacts) { contact ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = contact.number,
                        onValueChange = { contact.number = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = contact.name,
                        onValueChange = { contact.name = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val csvData = viewModel.generateCsvData()
                // TODO: Implement actual file saving logic (e.g., using ActivityResultLauncher)
                Log.d("ContactScreen", "CSV Data:\n$csvData")
            }) {
                Text("Save as CSV")
            }
            Button(onClick = {
                val vcfData = viewModel.generateVcfData()
                // TODO: Implement actual file saving logic (e.g., using ActivityResultLauncher)
                Log.d("ContactScreen", "VCF Data:\n$vcfData")
            }) {
                Text("Save as VCF")
            }
        }
    }
}
