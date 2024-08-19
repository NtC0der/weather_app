package com.example.weather_app.classes

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.example.weather_app.MainActivity
import com.example.weather_app.R
import androidx.lifecycle.lifecycleScope
import android.util.Log

import com.example.weather_app.errorHandling.ResponseTypes
import com.example.weather_app.network.WeatherApi

import kotlinx.coroutines.launch
import com.example.weather_app.interfaces.HandlerInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val DebugTag: String = "mistake"

class LoadingHandler(override val activity: MainActivity): HandlerInterface {

    init{
        try {
            activity.setContentView(R.layout.activity_load) // Loading the load UI

            val lottieAnimationView = activity.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
            lottieAnimationView?.playAnimation() // Start the loading animation

            //resetDatabaseFlag(activity) // Has the code forget the database was already created
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun startUI(): List<ResponseTypes?>{

        loadStartAnims() // Loading the element anims in a different thread

        var jsonList: List<ResponseTypes?> = listOf(ResponseTypes.error("If you see this. lifecyclescope failed."))

        jsonList = withContext(Dispatchers.IO) {
            loadData() // Loads the weather data and returns a list with the jsonObjs
        }

        delay(3000)// Waiting at least 3 seconds for the anims and data to load

        if (jsonList[0] is ResponseTypes.success) { // Checking if the APIs worked properly

            // Ensure we're on the main thread before updating the UI
            activity.lifecycleScope.launch(Dispatchers.Main) {
                loadFadeOutAnims() // Makes all elements fade out
            }
        }

        delay(1000) // Waiting for the fade out anims to play
        return jsonList
    }

    private suspend fun loadData(): List<ResponseTypes?> {

        return withContext(Dispatchers.IO) {

            var listToBeReturned: List<ResponseTypes?> =
                listOf(ResponseTypes.error("Something unexpected happened"))

            try {

                val weatherApi = WeatherApi() // uses default URL which is for current weather
                val responseDataCurrent = weatherApi.requestData()

                weatherApi.setURL(WeatherApi.forecast_url) // changes URL for forecast

                val responseDataForecast = weatherApi.requestData()

                if (responseDataCurrent is ResponseTypes.error) { // current API failed
                    handleError(responseDataCurrent) // displaying the error screen
                } else if (responseDataForecast is ResponseTypes.error) { // forecast API failed
                    handleError(responseDataForecast) // displaying the error screen
                } else { // both APIs succeeded
                    responseDataCurrent as ResponseTypes.success // casting it as success type since it isn't error
                    responseDataForecast as ResponseTypes.success

                    val responseJsonCurrent = responseDataCurrent.message
                    val responseJsonForecast = responseDataForecast.message

                    Log.d(DebugTag, "$responseJsonCurrent")
                    Log.d(DebugTag, "$responseJsonForecast")

                    // Returning the jsonObjs in a ResponseTypes form
                    val successCurrent = ResponseTypes.success(responseJsonCurrent)
                    val successForecast = ResponseTypes.success(responseJsonForecast)

                    listToBeReturned = listOf(successCurrent, successForecast)
                }

            } catch (e: Exception) {
                e.printStackTrace()

                val errorMessage =
                    ResponseTypes.error("Something went wrong with accessing the APIs")
                handleError(errorMessage) // displaying the error screen

                listToBeReturned =
                    listOf(errorMessage) // Assigning the error message so that it can be returned
            }
            return@withContext listToBeReturned
        }
    }

    private fun loadStartAnims(){ // Loads the UI elements anims

        val fadeElements = getElements()
        val fadeAnim = AnimationUtils.loadAnimation(activity, R.anim.fade1in)
        val titleAnim = AnimationUtils.loadAnimation(activity, R.anim.load_custom_anim)

        fadeElements.forEach { // animating each element in the list
            it.startAnimation(fadeAnim)
        }

        // Adding the anim to the logo and the title
        val title = fadeElements[0]
        val logo = fadeElements[2]

        // Playing the animations
        title.startAnimation(titleAnim)
        logo.startAnimation(titleAnim)
    }

    private fun loadFadeOutAnims(){ // Makes everything fadeout

        // Getting the list and adding the lottie to it
        val fadeElements = getElements()
        fadeElements.add(activity.findViewById<TextView>(R.id.lottieAnimationView))

        val fadeAnim = AnimationUtils.loadAnimation(activity, R.anim.fade1out)

        fadeElements.forEach{
            it.startAnimation(fadeAnim)
        }
    }

    private fun getElements(): MutableList<View> {// returns the elements that will be animated
        val title = activity.findViewById<TextView>(R.id.appTitle)
        val credits = activity.findViewById<TextView>(R.id.appCredits)

        val logo = activity.findViewById<ImageView>(R.id.appLogo)

        val fadeElements = mutableListOf(title, credits, logo) // making a list containing all the elements to be animated

        return fadeElements
    }
}