package com.example.weather_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.weather_app.classes.LoadingHandler
import com.example.weather_app.classes.MainUiHandler
import com.example.weather_app.errorHandling.ResponseTypes
import com.google.gson.JsonObject
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

private const val DebugTag: String = "FIXING"

class MainActivity : ComponentActivity() { // activity is a screen

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        lifecycleScope.launch{loadingUI()}
    }

    private suspend fun loadingUI() { // Function that creates the loading screen

        var loadingHandler: LoadingHandler? = LoadingHandler(this)
        loadingHandler as LoadingHandler // Casting it as a non nullable

        val jsonList: List<ResponseTypes?> = loadingHandler.startUI() // Returns a list in the form of (currentJson, forecastJson)

        if (jsonList[0] is ResponseTypes.success) { // Checking if the API didn't return null or an error

            val currentResponse = jsonList[0]
            val forecastResponse = jsonList[1]

            // Casting them as success type
            currentResponse as ResponseTypes.success
            forecastResponse as ResponseTypes.success

            loadingHandler = null // For garbage collect

            mainUI(
                currentResponse.message,
                forecastResponse.message
            ) // starting the mainUI function
        }
    }

    private fun mainUI(currentJson: JsonObject, forecastJson: JsonObject){ // Function that creates the main screen

        val mainHandler = MainUiHandler(this)
        mainHandler.startUI(currentJson, forecastJson)
    }
}

