package com.example.weather_app.classes

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.weather_app.MainActivity
import com.example.weather_app.R
import com.example.weather_app.errorHandling.ResponseTypes
import com.google.gson.JsonObject
import java.io.IOException
import android.view.animation.AnimationUtils
import android.widget.LinearLayout

class MainUiHandler(override val activity: MainActivity, private val currentJson: JsonObject, private val forecastJson: JsonObject): HandlerInterface {

    fun startUI() {

        // Getting maps containing the info of each Json
        val currentMap = getCurrentData(currentJson)
        val forecastMap = getForecastData(forecastJson)

        if (currentMap != null || forecastMap != null) { // Making sure the json array was proper

            // We have confirmed they aren't null so we can safely cast them as not nullable
            currentMap as Map<String, Any>
            forecastMap as Map<String, Any>

            activity.setContentView(R.layout.weather_main) // Loading the main UI
            setVisibility(View.INVISIBLE) // Making everything invisible

            // Applying the data to the UI
            applyDataCurrent(currentMap)
            applyDataForecast(forecastMap)

            setVisibility(View.VISIBLE) // Now that the data has loaded we make everything visible
            // Getting the rootLayout that contains all elements
            val rootLayout = activity.findViewById<ViewGroup>(R.id.main_layout)
            val animation = AnimationUtils.loadAnimation(activity, R.anim.fade1in)

            applyAnimationToViews(rootLayout, animation) // Playing the fade in animation
        }else{
            val errorMessage = ResponseTypes.error("Json data array want bigger than 0")
            handleError(errorMessage) // Displaying error screen
        }
    }

    // Returns a map with the data of the current weather json
    private fun getCurrentData(jsonData: JsonObject): Map<String, Any>? { // Gets the useful data out of the jsonObjects

        // Extracts data from jsonData
        val dataArray = jsonData.getAsJsonArray("data")

        if (dataArray.size() > 0) {
            val firstDataObject = dataArray[0].asJsonObject

            val cityName = firstDataObject.get("city_name").asString
            val temp = firstDataObject.get("temp").asFloat
            val windSpeed = firstDataObject.get("wind_spd").asFloat
            val humidity = firstDataObject.get("rh").asInt // percentage
            val clouds = firstDataObject.get("clouds").asInt // percentage

            val weatherObject = firstDataObject.getAsJsonObject("weather")
            val weatherDescription = weatherObject.get("description").asString
            val weatherImgID = weatherObject.get("icon").asString // Used to display the correct icon

            val currentMap = mapOf(
                "city" to cityName,
                "temp" to temp,
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
    private fun getForecastData(jsonData: JsonObject): MutableMap<String, Map<String, Any>>? {
        // Extracts data from jsonData
        val dataArray = jsonData.getAsJsonArray("data")

        if (dataArray.size() > 0) {
            val forecastMap: MutableMap<String, Map<String, Any>> = mutableMapOf()

            // Looping through each day info in the json
            dataArray.forEachIndexed{index, dayInfo ->

                val dayJson = dayInfo.asJsonObject

                val minTemp = dayJson.get("min_temp").asFloat
                val maxTemp = dayJson.get("max_temp").asFloat
                val windSpeed = dayJson.get("wind_spd").asFloat
                val humidity = dayJson.get("rh").asInt // percentage
                val clouds = dayJson.get("clouds").asInt // percentage

                val weatherObject = dayJson.getAsJsonObject("weather")
                val weatherDescription = weatherObject.get("description").asString
                val weatherImgID = weatherObject.get("icon").asString // Used to display the correct icon

                val dayMap = mapOf(
                    "min_temp" to minTemp,
                    "max_temp" to maxTemp,
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

    // Applies the json data to the current weather UI elements
    @SuppressLint("SetTextI18n", "ResourceType")
    private fun applyDataCurrent(currentMap: Map<String, Any>){

        val currentTitle = activity.findViewById<TextView>(R.id.weather_info)
        val weatherDescription = currentMap["weatherDesc"]

        weatherDescription as String // Casting the description as a string
        currentTitle.text = weatherDescription

        val cityTitle = activity.findViewById<TextView>(R.id.city_name)
        val cityName = currentMap["city"]

        cityName as String // Casting the city name as a string
        cityTitle.text = cityName

        val currentIMG = activity.findViewById<ImageView>(R.id.currentImage)
        val imageID = currentMap["weatherIMG"]

        imageID as String // Casting the image name as a string
        setImage(currentIMG, imageID)

        val temperatureTitle = activity.findViewById<TextView>(R.id.temperature_real)
        val temperature = currentMap["temp"]

        // Casting the temperature as a float and then making it into a string
        temperature as Float
        temperature.toString()

        val defaultTemp = activity.getString(R.string.temperature_real)
        temperatureTitle.text = "$defaultTemp ${temperature}C"

        val windTitle = activity.findViewById<TextView>(R.id.wind_speed)
        val windSpeed = currentMap["wind"]

        // Casting the wind speed as a float and then making it into a string
        windSpeed as Float
        windSpeed.toString()

        val defaultWind = activity.getString(R.string.wind_speed)
        windTitle.text = "$defaultWind $windSpeed m/s"

        val humidityTitle = activity.findViewById<TextView>(R.id.humidity)
        val humidity = currentMap["humidity"]

        // Casting the humidity as an int and then making it into a string
        humidity as Int
        humidity.toString()

        val defaultHum = activity.getString(R.string.humidity)
        humidityTitle.text = "$defaultHum ${humidity}%"

        val cloudsTitle = activity.findViewById<TextView>(R.id.clouds)
        val cloudCoverage = currentMap["clouds"]

        // Casting the clouds coverage as an int and then making it into a string
        cloudCoverage as Int
        cloudCoverage.toString()

        val defaultClouds = activity.getString(R.string.clouds_coverage)
        cloudsTitle.text = "$defaultClouds ${cloudCoverage}%"
    }

    // Applies the json data to the current weather UI elements
    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun applyDataForecast(forecastMap: MutableMap<String, Map<String, Any>>?) {

        for (i in 1..7){ // running one time for each day forecasted (7 days)

            val dayMap = forecastMap?.get("day$i") // getting the map of this loop's day

            val tempTitle = when (i) {
                1 -> activity.findViewById<TextView>(R.id.for_temp1)
                2 -> activity.findViewById<TextView>(R.id.for_temp2)
                3 -> activity.findViewById<TextView>(R.id.for_temp3)
                4 -> activity.findViewById<TextView>(R.id.for_temp4)
                5 -> activity.findViewById<TextView>(R.id.for_temp5)
                6 -> activity.findViewById<TextView>(R.id.for_temp6)
                7 -> activity.findViewById<TextView>(R.id.for_temp7)
                else -> throw IllegalArgumentException("Invalid index: $i")
            }
            val windTitle = when (i) {
                1 -> activity.findViewById<TextView>(R.id.for_wind1)
                2 -> activity.findViewById<TextView>(R.id.for_wind2)
                3 -> activity.findViewById<TextView>(R.id.for_wind3)
                4 -> activity.findViewById<TextView>(R.id.for_wind4)
                5 -> activity.findViewById<TextView>(R.id.for_wind5)
                6 -> activity.findViewById<TextView>(R.id.for_wind6)
                7 -> activity.findViewById<TextView>(R.id.for_wind7)
                else -> throw IllegalArgumentException("Invalid index: $i")
            }
            val humTitle = when (i) {
                1 -> activity.findViewById<TextView>(R.id.for_hum1)
                2 -> activity.findViewById<TextView>(R.id.for_hum2)
                3 -> activity.findViewById<TextView>(R.id.for_hum3)
                4 -> activity.findViewById<TextView>(R.id.for_hum4)
                5 -> activity.findViewById<TextView>(R.id.for_hum5)
                6 -> activity.findViewById<TextView>(R.id.for_hum6)
                7 -> activity.findViewById<TextView>(R.id.for_hum7)
                else -> throw IllegalArgumentException("Invalid index: $i")
            }
            val cloudsTitle = when (i) {
                1 -> activity.findViewById<TextView>(R.id.for_cloud1)
                2 -> activity.findViewById<TextView>(R.id.for_cloud2)
                3 -> activity.findViewById<TextView>(R.id.for_cloud3)
                4 -> activity.findViewById<TextView>(R.id.for_cloud4)
                5 -> activity.findViewById<TextView>(R.id.for_cloud5)
                6 -> activity.findViewById<TextView>(R.id.for_cloud6)
                7 -> activity.findViewById<TextView>(R.id.for_cloud7)
                else -> throw IllegalArgumentException("Invalid index: $i")
            }
            val imageView = when (i) {
                1 -> activity.findViewById<ImageView>(R.id.day_img1)
                2 -> activity.findViewById<ImageView>(R.id.day_img2)
                3 -> activity.findViewById<ImageView>(R.id.day_img3)
                4 -> activity.findViewById<ImageView>(R.id.day_img4)
                5 -> activity.findViewById<ImageView>(R.id.day_img5)
                6 -> activity.findViewById<ImageView>(R.id.day_img6)
                7 -> activity.findViewById<ImageView>(R.id.day_img7)
                else -> throw IllegalArgumentException("Invalid index: $i")
            }

            val minTemp = dayMap?.get("min_temp")
            val maxTemp = dayMap?.get("max_temp")
            val defaultTemp = activity.getString(R.string.short_temp)

            tempTitle.text = "$defaultTemp $minTemp - ${maxTemp}C"

            val windSpeed = dayMap?.get("wind")
            val defaultWind = activity.getString(R.string.short_wind_spd)
            windTitle.text = "$defaultWind ${windSpeed}m/s"

            val humidity = dayMap?.get("humidity")
            val defaultHum = activity.getString(R.string.short_humidity)
            humTitle.text = "$defaultHum ${humidity}%"

            val clouds = dayMap?.get("clouds")
            val defaultClouds = activity.getString(R.string.clouds_coverage)
            cloudsTitle.text = "$defaultClouds ${clouds}%"

            val imageID = dayMap?.get("weatherIMG")
            imageID as String // Casting the id as a string so it can be used in the setImage()

            setImage(imageView, imageID)
        }
    }

    private fun setImage(imageToChange: ImageView, imageName: String){ // Changes the image of an ImageView

        try {
            // Construct the drawable resource name by adding "icons_" prefix
            val resourceId = activity.resources.getIdentifier(imageName, "drawable", activity.packageName)

            if (resourceId != 0) {
                // Set the image resource to the ImageView
                imageToChange.setImageResource(resourceId)
            } else {
                throw Resources.NotFoundException("Image not found in drawable/icons: $imageName")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Changes visibility of all elements
    private fun setVisibility(visibility: Int){

        // Getting all the root elements and changing their visibility
        val headerElement = activity.findViewById<LinearLayout>(R.id.main_header)
        headerElement.visibility = visibility

        val bodyElement = activity.findViewById<LinearLayout>(R.id.main_body)
        bodyElement.visibility = visibility

        val footerElement = activity.findViewById<LinearLayout>(R.id.main_footer)
        footerElement.visibility = visibility
    }
    // Will be used to apply fade in anim
    private fun applyAnimationToViews(viewGroup: ViewGroup, animation: android.view.animation.Animation) {
        for (i in 0 until viewGroup.childCount) {
            val view = viewGroup.getChildAt(i)
            when (view) {
                is TextView -> view.startAnimation(animation)
                is ImageView -> view.startAnimation(animation)
                is ViewGroup -> applyAnimationToViews(view, animation) // Recursively apply to children
            }
        }
    }
}