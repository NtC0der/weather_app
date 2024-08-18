package com.example.weather_app.errorHandling

import android.widget.TextView
import com.example.weather_app.MainActivity
import com.example.weather_app.R

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope

import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FatalError { // Class used when a fatal error occurs and the app needs to stop

    fun displayError(activity: MainActivity, errorMessage: ResponseTypes.error) { // displays the error window

        activity.runOnUiThread {
            activity.setContentView(R.layout.error_screen) // making the error screen appear

            val errorText = errorMessage.message // Getting the error message
            Log.d("FatalError", errorText) // Logging it
            val copyButton = activity.findViewById<ImageView>(R.id.copyImg)

            copyButton.setOnClickListener {

                val clipboard =
                    activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager // Gets the ClipboardManager

                val clip = ClipData.newPlainText(
                    "label",
                    errorText
                )// Creates a ClipData object with the text to copy

                clipboard.setPrimaryClip(clip) // Sets the clipboard data

                // Pop up that lets the user know data has been copied
                Snackbar.make(activity.findViewById<LinearLayout>(R.id.error_main), "Text copied to clipboard", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}