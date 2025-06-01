package com.ui.autosaveui.graber

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager // For sending data to Activity

class MyAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_TEXT_EXTRACTED = "com.ui.autosaveui.ACTION_TEXT_EXTRACTED"
        const val EXTRA_EXTRACTED_TEXT = "extracted_text"
        private const val TAG = "MyAccessibilityService"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service Connected!")

        val info = AccessibilityServiceInfo().apply {
            // Listen for window content changes, text changes, and window state changes
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_SCROLLED // Keep scrolled for dynamic content

            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

            // Explicitly set package names for WhatsApp and WhatsApp Business
            packageNames = arrayOf("com.whatsapp", "com.whatsapp.w4b")

            // Flags to retrieve view IDs and interactive windows
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

            notificationTimeout = 100
        }
        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Log the basic event details.
        Log.d(TAG, "Event Type: ${event.eventType}, Package: ${event.packageName}, Class: ${event.className}, Text: ${event.text}")

        // Ensure the event is from our target packages
        if (event.packageName != "com.whatsapp" && event.packageName != "com.whatsapp.w4b") {
            return
        }

        // Get the root node of the active window
        val rootNode: AccessibilityNodeInfo? = rootInActiveWindow
        rootNode?.let {
            val extractedTexts = mutableSetOf<String>() // Use a Set to avoid duplicate entries
            traverseAndExtractText(it, extractedTexts)

            if (extractedTexts.isNotEmpty()) {
                val combinedText = extractedTexts.joinToString("\n")
                Log.i(TAG, "Successfully Extracted Text:\n$combinedText") // Use INFO for successful extractions
                sendExtractedTextToActivity(combinedText)
            } else {
                Log.d(TAG, "No new text extracted for this event.")
            }
            it.recycle() // Recycle the root node to avoid memory leaks
        }
    }

    /**
     * Recursively traverses the AccessibilityNodeInfo tree to extract all visible text.
     * Checks text, contentDescription, and children.
     */
    private fun traverseAndExtractText(node: AccessibilityNodeInfo, extractedTexts: MutableSet<String>) {
        // 1. Check for text in the current node
        if (node.text != null && node.text.isNotBlank()) {
            val text = node.text.toString().trim()
            if (text.isNotEmpty()) {
                extractedTexts.add(text)
                Log.d(TAG, "Extracted (text): \"$text\" from ${node.className} with ID: ${node.viewIdResourceName ?: "N/A"}")
            }
        }

        // 2. Check for contentDescription in the current node (often holds important labels)
        if (node.contentDescription != null && node.contentDescription.isNotBlank()) {
            val contentDesc = node.contentDescription.toString().trim()
            if (contentDesc.isNotEmpty()) {
                extractedTexts.add(contentDesc)
                Log.d(TAG, "Extracted (contentDescription): \"$contentDesc\" from ${node.className} with ID: ${node.viewIdResourceName ?: "N/A"}")
            }
        }

        // 3. Recursively check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                // Ensure the child is visible on screen before recursing deeper
                if (child.isVisibleToUser) {
                    traverseAndExtractText(child, extractedTexts)
                }
                child.recycle() // Always recycle child nodes
            }
        }
    }

    /**
     * Sends the extracted text to Grabctivity using LocalBroadcastManager.
     */
    private fun sendExtractedTextToActivity(text: String) {
        val intent = Intent(ACTION_TEXT_EXTRACTED).apply {
            putExtra(EXTRA_EXTRACTED_TEXT, text)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service Interrupted!")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility Service Destroyed!")
    }
}