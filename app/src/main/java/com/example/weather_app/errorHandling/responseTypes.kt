package com.example.weather_app.errorHandling

import com.google.gson.JsonObject

sealed class ResponseTypes{

    data class success(val message: JsonObject): ResponseTypes()
    data class error(val message: String): ResponseTypes()
}