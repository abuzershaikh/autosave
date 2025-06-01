package com.ui.autosaveui.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface

class ContactActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_NUMBERS = "EXTRA_NUMBERS"

        fun newIntent(context: Context, numbers: ArrayList<String>): Intent {
            return Intent(context, ContactActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_NUMBERS, numbers)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extractedNumbers = intent.getStringArrayListExtra(EXTRA_NUMBERS) ?: emptyList<String>()

        setContent {
            MaterialTheme { // Assuming MaterialTheme is used, adjust if another theme is set up
                Surface {
                    ContactScreen(extractedNumbers = extractedNumbers)
                }
            }
        }
    }
}
