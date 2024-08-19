package com.example.weather_app.network

import com.example.weather_app.errorHandling.ResponseTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.net.MalformedURLException

interface Request {

    var API_url: URL

    suspend fun requestData(): ResponseTypes { // Method used to request API data

        var responseMessage: ResponseTypes = ResponseTypes.error("An unexpected error occurred.")

        try {
            val url = API_url
            Log.d("DebugTag", url.toString())

            val connection = withContext(Dispatchers.IO) {
                url.openConnection() as HttpURLConnection
            }

            Log.d("DebugTag", "Opened")

            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            Log.d("DebugTag", "Response Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = withContext(Dispatchers.IO) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                }
                try {
                    val gson = Gson()  // Creating a Gson instance here
                    val jsonResponse = gson.fromJson(response, JsonObject::class.java)

                    Log.d("DebugTag", "we good")

                    responseMessage = ResponseTypes.success(jsonResponse)
                } catch (e: JsonSyntaxException) {
                    responseMessage = ResponseTypes.error("Error: Invalid JSON response.")
                }
            } else {
                responseMessage = ResponseTypes.error("Error: Unable to fetch data from the API")
            }

            connection.disconnect()
        } catch (e: MalformedURLException) {
            Log.e("DebugTag", "Invalid URL: ${e.message}", e)
            responseMessage = ResponseTypes.error("Error: Invalid URL")
        } catch (e: IOException) {
            Log.e("DebugTag", "Network Error: ${e.message}", e)
            responseMessage = ResponseTypes.error("Error: Network error")
        } catch (e: Exception) {
            Log.e("DebugTag", "Unexpected Error: ${e.message}", e)
            responseMessage = handleException(e)
        }
        return responseMessage
    }

    suspend fun postData(jsonBody: String): ResponseTypes { // Method used to post data to API

        var responseMessage: ResponseTypes = ResponseTypes.error("An unexpected error occurred.")

        try {
            val url = API_url

            val connection = withContext(Dispatchers.IO) {
                url.openConnection() as HttpURLConnection
            }

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            withContext(Dispatchers.IO) {
                connection.outputStream.bufferedWriter().use { it.write(jsonBody) }
            }

            val responseCode = connection.responseCode
            Log.d("DebugTag", "Response Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = withContext(Dispatchers.IO) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                }
                try {
                    val gson = Gson()  // Creating a Gson instance here
                    val jsonResponse = gson.fromJson(response, JsonObject::class.java)

                    responseMessage = ResponseTypes.success(jsonResponse)
                } catch (e: JsonSyntaxException) {
                    responseMessage = ResponseTypes.error("Error: Invalid JSON response.")
                }
            } else {
                responseMessage = ResponseTypes.error("Error: Unable to post data to the API")
            }

            connection.disconnect()
        } catch (e: MalformedURLException) {
            Log.e("DebugTag", "Invalid URL: ${e.message}", e)
            responseMessage = ResponseTypes.error("Error: Invalid URL")
        } catch (e: IOException) {
            Log.e("DebugTag", "Network Error: ${e.message}", e)
            responseMessage = ResponseTypes.error("Error: Network error")
        } catch (e: Exception) {
            Log.e("DebugTag", "Unexpected Error: ${e.message}", e)
            responseMessage = handleException(e)
        }

        return responseMessage
    }

    // Error handling method
    private fun handleException(e: Exception): ResponseTypes.error {
        e.printStackTrace()
        return ResponseTypes.error("Exception: ${e.message}")
    }
}