package com.example.weather_app.network

import com.example.weather_app.errorHandling.ResponseTypes
import java.net.URL

// Bellow we override the key and url values in the parent class
class WeatherApi(val lat: String = "40.18479", val lon: String = "-80.99565") : Request {

    // Default params are for Athens

    /*override val API_url: URL
        get() = constructUrl() // changing the url to the correct one*/

    companion object{ // here we store the url and key
        const val current_url: String = "https://api.weatherbit.io/v2.0/current" // tells you current weather
        const val forecast_url: String = "https://api.weatherbit.io/v2.0/forecast/daily" // tells weather forecast

        private const val key: String = "4e13091f6f574e19997faa7f9b33c2db" // or bb56ea1fb1fc483f8fa00b574719e924
    }

    override suspend fun requestData(): ResponseTypes {
        return super.requestData()
    }

    override var API_url: URL = constructUrl(current_url) // default url

    fun setURL(url: String){ // Changes the url used
        API_url = constructUrl(url)
    }

    // Protected utility method to construct the URL
    private fun constructUrl(url: String): URL {
        return URL("$url?lat=$lat&lon=$lon&key=$key")
    }
}
