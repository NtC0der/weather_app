package com.example.weather_app.network

import com.example.weather_app.errorHandling.ResponseTypes
import com.google.gson.JsonObject
import java.net.URL

class WeatherApi() : Request {


    companion object{ // here we store the url and key
        const val current_url: String = "https://api.weatherbit.io/v2.0/current" // tells you current weather
        const val forecast_url: String = "https://api.weatherbit.io/v2.0/forecast/daily" // tells weather forecast

        private const val key: String = "bb56ea1fb1fc483f8fa00b574719e924"
        //4e13091f6f574e19997faa7f9b33c2db or bb56ea1fb1fc483f8fa00b574719e924

        // Default params are for Athens
        private const val def_lat = "37.97945"
        private const val def_lon = "23.71622"
    }

    override suspend fun requestData(): ResponseTypes {
        return super.requestData()
    }

    override var API_url: URL = constructUrl(current_url, def_lat, def_lon) // default url

    // Returns a map with the data of the current weather json
    fun getCurrentData(jsonData: JsonObject): Map<String, Any>? { // Gets the useful data out of the jsonObjects

        // Extracts data from jsonData
        val dataArray = jsonData.getAsJsonArray("data")

        if (dataArray.size() > 0) {
            val firstDataObject = dataArray[0].asJsonObject

            val cityName = firstDataObject.get("city_name").asString
            val temp = firstDataObject.get("temp").asFloat
            val tempFeel = firstDataObject.get("app_temp").asFloat
            val windSpeed = firstDataObject.get("wind_spd").asFloat
            val humidity = firstDataObject.get("rh").asInt // percentage
            val clouds = firstDataObject.get("clouds").asInt // percentage

            val weatherObject = firstDataObject.getAsJsonObject("weather")
            val weatherDescription = weatherObject.get("description").asString
            val weatherImgID = weatherObject.get("icon").asString // Used to display the correct icon

            val currentMap = mapOf(
                "city" to cityName,
                "temp" to temp,
                "temp_feel" to tempFeel,
                "wind" to windSpeed,
                "humidity" to humidity,
                "clouds" to clouds,
                "weatherDesc" to weatherDescription,
                "weatherIMG" to weatherImgID
            )

            return currentMap
        }else{ // Something went wrong
            return null
        }
    }

    // Returns a map with the data of the current weather json
    fun getForecastData(jsonData: JsonObject): MutableMap<String, Map<String, Any>>? {
        // Extracts data from jsonData
        val dataArray = jsonData.getAsJsonArray("data")

        if (dataArray.size() > 0) {
            val forecastMap: MutableMap<String, Map<String, Any>> = mutableMapOf()

            // Looping through each day info in the json
            dataArray.forEachIndexed{index, dayInfo ->

                val dayJson = dayInfo.asJsonObject

                val minTemp = dayJson.get("min_temp").asFloat
                val maxTemp = dayJson.get("max_temp").asFloat
                val minTempFeel = dayJson.get("app_min_temp").asFloat
                val maxTempFeel = dayJson.get("app_max_temp").asFloat
                val windSpeed = dayJson.get("wind_spd").asFloat
                val humidity = dayJson.get("rh").asInt // percentage
                val clouds = dayJson.get("clouds").asInt // percentage

                val weatherObject = dayJson.getAsJsonObject("weather")
                val weatherDescription = weatherObject.get("description").asString
                val weatherImgID = weatherObject.get("icon").asString // Used to display the correct icon

                val dayMap = mapOf(
                    "min_temp" to minTemp,
                    "max_temp" to maxTemp,
                    "min_temp_feel" to minTempFeel,
                    "max_temp_feel" to maxTempFeel,
                    "wind" to windSpeed,
                    "humidity" to humidity,
                    "clouds" to clouds,
                    "weatherDesc" to weatherDescription,
                    "weatherIMG" to weatherImgID
                )

                // How the map will be registered in the parent map
                val mapKey = "day${(index+1)}" // adding 1 to the index because it starts from 0
                forecastMap[mapKey] = dayMap
            }

            return forecastMap
        }else{
            return null
        }
    }

    fun setURL(url: String, lat: String=def_lat, lon: String=def_lon){ // Changes the url used
        API_url = constructUrl(url, lat, lon)
    }

    // Protected utility method to construct the URL
    private fun constructUrl(url: String, lat: String, lon: String): URL {
        return URL("$url?lat=$lat&lon=$lon&key=$key")
    }
}
