package com.example.weather_app.classes

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.weather_app.MainActivity
import com.example.weather_app.R
import com.example.weather_app.errorHandling.ResponseTypes
import com.google.gson.JsonObject
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.weather_app.interfaces.ClassMethods
import com.example.weather_app.interfaces.HandlerInterface
import com.example.weather_app.network.WeatherApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainUiHandler(override val activity: MainActivity): HandlerInterface, ClassMethods {

    private val weatherApi = WeatherApi()

    private lateinit var db: AppDatabase
    private lateinit var cityDao: CityDao
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var textEntry: AutoCompleteTextView

    init {
        // Initializing the database
        try {

            db = AppDatabase.getDatabase(activity)
            cityDao = db.cityDao()
        }catch (e: Exception){
            e.printStackTrace()

            val errorMessage = ResponseTypes.error("Couldn't initialize the database: ${e.message}")
            handleError(errorMessage)
        }
    }

    fun startUI(currentJson: JsonObject, forecastJson: JsonObject) {

        // Getting maps containing the info of each Json
        val currentMap = weatherApi.getCurrentData(currentJson)
        val forecastMap = weatherApi.getForecastData(forecastJson)

        if (currentMap == null || forecastMap == null) { // Making sure the json array was proper
            val errorMessage = ResponseTypes.error("Json data array want bigger than 0")
            handleError(errorMessage) // Displaying error screen

            return
        }

        val rootLayout = activity.findViewById<View>(android.R.id.content)
        rootLayout as ViewGroup // Casting it as the correct type

        val animationDurationMs: Long

        if (rootLayout.id == R.id.main_layout) { // if this isn't the first time showing the weather

            val animation = AnimationUtils.loadAnimation(activity, R.anim.fade_cycle)
            applyAnimationToViews(rootLayout, animation) // Playing the fade in animation
            animationDurationMs = 2000
        }else{ // First time showing weather
            activity.setContentView(R.layout.weather_main) // Loading the main UI

            val animation = AnimationUtils.loadAnimation(activity, R.anim.fade1in)
            applyAnimationToViews(rootLayout, animation) // Playing the fade in animation
            animationDurationMs = 1000
        }

        setVisibility(View.INVISIBLE) // Making everything invisible

        adjustSpaceSizes()

        // Applying the data to the UI
        applyDataCurrent(currentMap)
        applyDataForecast(forecastMap)

        setVisibility(View.VISIBLE) // Now that the data has loaded we make everything visible
        val animation = AnimationUtils.loadAnimation(activity, R.anim.fade1in)

        rootLayout.isEnabled = false // Making it so the user cant click buttons while the fade in
        applyAnimationToViews(rootLayout, animation) // Playing the fade in animation

        textEntry = activity.findViewById<AutoCompleteTextView>(R.id.searchText)

        // Initializes the adapter
        adapter = ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        textEntry.setAdapter(adapter)

        // Letting the code know that the text has been changed
        textEntry.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchCities(s.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        val searchButton = activity.findViewById<ImageView>(R.id.searchButton)

        // Runs the function which will later on display the info of the city pressed
        searchButton.setOnClickListener {

            searchPressed(textEntry.text)
        }

        activity.lifecycleScope.launch {
            delay(animationDurationMs)
            rootLayout.isEnabled = true // Now that everything's fine we re-enable the layout
        }
    }

    // Spaces out linear layouts
    private fun adjustSpaceSizes() {

        val spaceForEach = calculateSpaceForEach()

        val space1 = activity.findViewById<Space>(R.id.space1)
        val space2 = activity.findViewById<Space>(R.id.space2)
        val space3 = activity.findViewById<Space>(R.id.space3)

        val params1 = space1.layoutParams
        val params2 = space2.layoutParams
        val params3 = space3.layoutParams

        // Assuming you want to set the height of these views
        params1.height = (params1.height + spaceForEach).toInt()
        params2.height = (params2.height + spaceForEach).toInt()
        params3.height = (params3.height + spaceForEach).toInt()

        space1.layoutParams = params1
        space2.layoutParams = params2
        space3.layoutParams = params3
    }

    // Calculates how much each size will be added to each space
    private fun calculateSpaceForEach(): Float {
        val extraSpace = calculateExtraSpace(activity)
        return if (extraSpace > 0) extraSpace / 3 else 0f
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

        val temperatureFeelTitle = activity.findViewById<TextView>(R.id.temperature_feel)
        val temperatureFeel = currentMap["temp_feel"]

        // Casting the temperature feel as a float and then making it into a string
        temperatureFeel as Float
        temperatureFeel.toString()

        val defaultTempFeel = activity.getString(R.string.temp_feel)
        temperatureFeelTitle.text = "$defaultTempFeel ${temperatureFeel}C"

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

            val weatherDescTitle = when (i) {
                1 -> activity.findViewById<TextView>(R.id.weather_title1)
                2 -> activity.findViewById<TextView>(R.id.weather_title2)
                3 -> activity.findViewById<TextView>(R.id.weather_title3)
                4 -> activity.findViewById<TextView>(R.id.weather_title4)
                5 -> activity.findViewById<TextView>(R.id.weather_title5)
                6 -> activity.findViewById<TextView>(R.id.weather_title6)
                7 -> activity.findViewById<TextView>(R.id.weather_title7)
                else -> throw IllegalArgumentException("Invalid index: $i")
            }

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

            val tempFeelTitle = when (i) {
                1 -> activity.findViewById<TextView>(R.id.for_temp_feel1)
                2 -> activity.findViewById<TextView>(R.id.for_temp_feel2)
                3 -> activity.findViewById<TextView>(R.id.for_temp_feel3)
                4 -> activity.findViewById<TextView>(R.id.for_temp_feel4)
                5 -> activity.findViewById<TextView>(R.id.for_temp_feel5)
                6 -> activity.findViewById<TextView>(R.id.for_temp_feel6)
                7 -> activity.findViewById<TextView>(R.id.for_temp_feel7)
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
            val dayTitle = when (i) {
                1 -> activity.findViewById<TextView>(R.id.day1)
                2 -> activity.findViewById<TextView>(R.id.day2)
                3 -> activity.findViewById<TextView>(R.id.day3)
                4 -> activity.findViewById<TextView>(R.id.day4)
                5 -> activity.findViewById<TextView>(R.id.day5)
                6 -> activity.findViewById<TextView>(R.id.day6)
                7 -> activity.findViewById<TextView>(R.id.day7)
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

            val desiredDay = getDayOfWeek(i) // Gets the day of the week the forecast reffers to
            dayTitle.text = desiredDay

            val weatherDesc = dayMap?.get("weatherDesc")
            weatherDescTitle.text = "$weatherDesc"

            val minTemp = dayMap?.get("min_temp")
            val maxTemp = dayMap?.get("max_temp")
            val defaultTemp = activity.getString(R.string.short_temp)

            tempTitle.text = "$defaultTemp $minTemp - ${maxTemp}C"

            val minTempFeel = dayMap?.get("min_temp_feel")
            val maxTempFeel = dayMap?.get("max_temp_feel")
            val defaultTempFeel = activity.getString(R.string.temp_feel)

            tempFeelTitle.text = "$defaultTempFeel $minTempFeel - ${maxTempFeel}C"

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

    // Changes visibility of all elements
    private fun setVisibility(visibility: Int){

        // Getting all the root elements and changing their visibility
        val headerElement = activity.findViewById<LinearLayout>(R.id.main_header)
        headerElement.visibility = visibility

        val bodyElement1 = activity.findViewById<LinearLayout>(R.id.main_body1)
        bodyElement1.visibility = visibility

        val bodyElement2 = activity.findViewById<LinearLayout>(R.id.main_body2)
        bodyElement2.visibility = visibility

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

    // Runs every time the search text box is edited
    private fun searchCities(query: String) {
        // Uses Coroutine to perform database query on a background thread
        activity.lifecycleScope.launch {
            val cityNames = withContext(Dispatchers.IO) {
                // Executes the search query in the DAO to find cities matching the user's input.
                // The query uses wildcards (%) to allow partial matching of city names.

                if (cityDao.getCityByName(query) == null) { // Exact match doesn't exist
                    cityDao.searchCitiesByName("%$query%").map { "${it.city_name}, ${it.country_full}" }
                }else{ // Exact match exists, no need to display dropdown menu
                    listOf()
                }
            }
            adapter.clear()
            adapter.addAll(cityNames)  // Adds all the city names returned from the search query to the adapter.
            adapter.notifyDataSetChanged()

            if (cityNames.isNotEmpty()) { // it means it succesfully founda match
                textEntry.showDropDown()
            }
        }
    }

    // Runs when the user presses the search button
    private fun searchPressed(text: Editable) {

        val searchQuery = text.toString()
        val cityName = searchQuery.split(",")[0] // Extracting city name out of query

        activity.lifecycleScope.launch(Dispatchers.IO) {
            // Perform the database query on the IO thread to avoid blocking the main UI thread.
            val city = withContext(Dispatchers.IO) {
                cityDao.getCityByName(cityName)
            }

            // If the city exists, retrieve the latitude and longitude.
            if (city != null) {
                val latitude = city.lat
                val longitude = city.lon

                // Getting the new current weather json
                weatherApi.setURL(WeatherApi.current_url, latitude.toString(), longitude.toString())
                val currentJson = weatherApi.requestData()

                //Getting the new forecast
                weatherApi.setURL(WeatherApi.forecast_url, latitude.toString(), longitude.toString())
                val forecastJson = weatherApi.requestData()

                if (currentJson is ResponseTypes.success && forecastJson is ResponseTypes.success) {
                    // Refreshing the UI
                    activity.lifecycleScope.launch(Dispatchers.Main){startUI(currentJson.message, forecastJson.message)}
                }else if (currentJson is ResponseTypes.error){ // Current API faced an error
                    handleError(currentJson)
                }else if (forecastJson is ResponseTypes.error){ // Forecast API faced an error
                    handleError(forecastJson)
                }

            } else {
                // Handle the case where the city is not found.
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(activity, "City not found!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}