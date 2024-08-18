package com.example.weather_app.classes

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.weather_app.MainActivity
import com.example.weather_app.errorHandling.FatalError
import com.example.weather_app.errorHandling.ResponseTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val DebugTag = "FIXING"

interface HandlerInterface { // All handlers will use this interface

    val activity: MainActivity

    fun handleError(errorMessage: ResponseTypes.error){ // Makes the error screen appear with the correct error message
        val errorScreen = FatalError()

        Log.d(DebugTag, errorMessage.message) // printing the error

        errorScreen.displayError(activity, errorMessage) // displaying the error screen
    }
}