package com.ui.autosaveui.viewmodel

import com.ui.autosaveui.model.ContactEntry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ContactViewModelTest {

    private lateinit var viewModel: ContactViewModel

    @Before
    fun setUp() {
        viewModel = ContactViewModel()
    }

    @Test
    fun `loadContacts initializes contacts and updates names`() {
        val numbers = listOf("+123", "+456")
        viewModel.loadContacts(numbers)

        assertEquals(2, viewModel.contacts.size)
        assertEquals("+123", viewModel.contacts[0].number)
        assertEquals("Default 1", viewModel.contacts[0].name)
        assertEquals("+456", viewModel.contacts[1].number)
        assertEquals("Default 2", viewModel.contacts[1].name)
    }

    @Test
    fun `updateContactNames updates names based on seriesName and startNumber`() {
        viewModel.contacts.addAll(listOf(
            ContactEntry("Old Name 1", "+111"),
            ContactEntry("Old Name 2", "+222")
        ))
        viewModel.seriesName.value = "Work"
        viewModel.startNumber.value = "10"
        viewModel.updateContactNames()

        assertEquals("Work 10", viewModel.contacts[0].name)
        assertEquals("Work 11", viewModel.contacts[1].name)
    }

    @Test
    fun `updateContactNames handles non-integer startNumber gracefully`() {
        viewModel.contacts.addAll(listOf(ContactEntry("", "+111")))
        viewModel.seriesName.value = "Test"
        viewModel.startNumber.value = "abc" // Not an integer
        viewModel.updateContactNames()

        assertEquals("Test 1", viewModel.contacts[0].name) // Should default to 1
    }

    @Test
    fun `generateCsvData returns correct CSV string`() {
        viewModel.contacts.addAll(listOf(
            ContactEntry("John Doe", "12345"),
            ContactEntry("Jane Smith", "67890")
        ))
        val expectedCsv = "Name,Number\nJohn Doe,12345\nJane Smith,67890"
        assertEquals(expectedCsv, viewModel.generateCsvData())
    }

    @Test
    fun `generateVcfData returns correct VCF string`() {
        viewModel.contacts.addAll(listOf(
            ContactEntry("John Doe", "12345")
        ))
        val expectedVcf = """
            BEGIN:VCARD
            VERSION:3.0
            N:John Doe;;;
            FN:John Doe
            TEL;TYPE=CELL:12345
            END:VCARD
        """.trimIndent().trim()

        val actualVcf = viewModel.generateVcfData().lines().filter { it.isNotBlank() }.joinToString("\n")
        val normalizedExpectedVcf = expectedVcf.lines().filter { it.isNotBlank() }.joinToString("\n")

        assertEquals(normalizedExpectedVcf, actualVcf)
    }

    @Test
    fun `generateVcfData returns correct VCF string for multiple contacts`() {
        viewModel.contacts.addAll(listOf(
            ContactEntry("John Doe", "12345"),
            ContactEntry("Jane Smith", "67890")
        ))
        val expectedVcf = """
            BEGIN:VCARD
            VERSION:3.0
            N:John Doe;;;
            FN:John Doe
            TEL;TYPE=CELL:12345
            END:VCARD
            BEGIN:VCARD
            VERSION:3.0
            N:Jane Smith;;;
            FN:Jane Smith
            TEL;TYPE=CELL:67890
            END:VCARD
        """.trimIndent().trim()

        val actualVcf = viewModel.generateVcfData().lines().filter { it.isNotBlank() }.joinToString("\n")
        val normalizedExpectedVcf = expectedVcf.lines().filter { it.isNotBlank() }.joinToString("\n")

        assertEquals(normalizedExpectedVcf, actualVcf)
    }
}
