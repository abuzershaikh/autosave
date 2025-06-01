package com.ui.autosaveui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ui.autosaveui.model.ContactEntry

class ContactViewModel : ViewModel() {

    val contacts = mutableStateListOf<ContactEntry>()
    val seriesName = mutableStateOf("Default")
    val startNumber = mutableStateOf("1")

    fun updateContactNames() {
        val startNum = startNumber.value.toIntOrNull() ?: 1
        contacts.forEachIndexed { index, contact ->
            contact.name = "${seriesName.value} ${startNum + index}"
        }
    }

    fun loadContacts(numbers: List<String>) {
        Log.d("ContactViewModel", "loadContacts received numbers: $numbers, Count: ${numbers.size}")
        // contacts.clear() // Optional
        numbers.forEach { number ->
            contacts.add(ContactEntry(number = number, name = ""))
        }
        Log.d("ContactViewModel", "ViewModel contacts after adding: ${contacts.map { it.number }}, Count: ${contacts.size}")
        if (numbers.isNotEmpty()) {
            updateContactNames()
        }
    }

    fun generateCsvData(): String {
        val header = "Name,Number"
        val rows = contacts.map { "${it.name},${it.number}" }
        return header + "\n" + rows.joinToString("\n")
    }

    fun generateVcfData(): String {
        val vcfBuilder = StringBuilder()
        contacts.forEach { contact ->
            vcfBuilder.append("BEGIN:VCARD\n")
            vcfBuilder.append("VERSION:3.0\n")
            vcfBuilder.append("N:${contact.name};;;\n") // Simplified: Using name as FN and N
            vcfBuilder.append("FN:${contact.name}\n")
            vcfBuilder.append("TEL;TYPE=CELL:${contact.number}\n")
            vcfBuilder.append("END:VCARD\n")
        }
        return vcfBuilder.toString()
    }
}
