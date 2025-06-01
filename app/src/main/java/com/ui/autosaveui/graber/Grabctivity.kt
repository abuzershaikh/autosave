package com.ui.autosaveui.graber

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ui.autosaveui.R // Make sure this R is correct for your project

class Grabctivity : AppCompatActivity() {

    private lateinit var extractedNumbersListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val extractedTextsList = mutableListOf<String>()
    private lateinit var enableServiceButton: Button

    private val textExtractedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MyAccessibilityService.ACTION_TEXT_EXTRACTED) {
                val extractedText = intent.getStringExtra(MyAccessibilityService.EXTRA_EXTRACTED_TEXT)
                extractedText?.let {
                    // Here's where you'd add your number extraction logic
                    // For now, we'll just add the raw text
                    val numbers = extractNumbersFromText(it)
                    if (numbers.isNotEmpty()) {
                        extractedTextsList.addAll(numbers)
                        adapter.notifyDataSetChanged()
                        extractedNumbersListView.smoothScrollToPosition(extractedTextsList.size - 1)
                        // FIX: Corrected string interpolation for numbers
                        Log.d("Grabctivity", "Numbers added: $numbers")
                    } else {
                        Log.d("Grabctivity", "No numbers found in extracted text.")
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to a simple layout with a ListView and a button
        setContentView(R.layout.activity_grabctivity) // You'll need to create this XML layout

        extractedNumbersListView = findViewById(R.id.extractedNumbersListView)
        enableServiceButton = findViewById(R.id.enableServiceButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, extractedTextsList)
        extractedNumbersListView.adapter = adapter

        enableServiceButton.setOnClickListener {
            // Guide user to accessibility settings
            showAccessibilityServiceDialog()
        }

        // Register the broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            textExtractedReceiver,
            IntentFilter(MyAccessibilityService.ACTION_TEXT_EXTRACTED)
        )
    }
    override fun onResume() {
        super.onResume()
        checkAccessibilityServiceStatus()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the broadcast receiver to prevent leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(textExtractedReceiver)
    }
    /**
     * Checks if the MyAccessibilityService is enabled.
     */
    private fun checkAccessibilityServiceStatus() {
        val serviceName = packageName + "/" + MyAccessibilityService::class.java.name
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val isServiceEnabled = enabledServices?.contains(serviceName) == true
        if (isServiceEnabled) {
            enableServiceButton.text = "Accessibility Service Enabled"
            enableServiceButton.isEnabled = false
            Toast.makeText(this, "Accessibility Service is ON", Toast.LENGTH_SHORT).show()
        } else {
            enableServiceButton.text = "Enable Accessibility Service"
            enableServiceButton.isEnabled = true
            Toast.makeText(this, "Accessibility Service is OFF. Please enable it.", Toast.LENGTH_LONG).show()
        }
    }
    /**
     * Shows a dialog to guide the user to enable the accessibility service.
     */
    private fun showAccessibilityServiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Accessibility Service")
            .setMessage("To allow this app to read screen content, please enable '${getString(R.string.app_name)}' in your device's Accessibility settings.")
            .setPositiveButton("Go to Settings") { dialog, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Simple function to extract potential numbers from a given text.
     * You'll need to refine this regex for specific phone number formats.
     */
    private fun extractNumbersFromText(text: String): List<String> {
        val numbers = mutableListOf<String>()
        // This regex attempts to find sequences of digits that might be phone numbers.
        // It's a very basic example and might need significant refinement for real-world use.
        // For WhatsApp, numbers might appear with country codes, spaces, or dashes.
        val phoneRegex = "\\b\\+?\\d[\\d\\s-()]{7,}\\b".toRegex() // Matches digits, spaces, hyphens, parentheses, optional leading +
        phoneRegex.findAll(text).forEach { match ->
            val potentialNumber = match.value.replace("[\\s-()]".toRegex(), "") // Clean up the number
            // Add more validation here (e.g., check length, starts with certain digits)
            if (potentialNumber.length >= 7) { // Basic length check
                numbers.add(potentialNumber)
            }
        }
        return numbers.distinct() // Return unique numbers
    }
}