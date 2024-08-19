package com.example.weather_app.interfaces

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageView
import com.example.weather_app.MainActivity
import java.util.Calendar

// Contains general methods that could be used by many handlers
interface ClassMethods {

    val activity: MainActivity

    fun getScreenHeightInPixels(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun dpToPx(context: Context, dp: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun calculateExtraSpace(context: Context): Float {
        val screenHeightPx = getScreenHeightInPixels(context).toFloat()
        val standardHeightPx = dpToPx(context, 891f)

        return screenHeightPx - standardHeightPx
    }

    // Changes the image of an ImageView
    fun setImage(imageToChange: ImageView, imageName: String){ // Changes the image of an ImageView

        try {
            // Construct the drawable resource name by adding "icons_" prefix
            val resourceId = activity.resources.getIdentifier(imageName, "drawable", activity.packageName)

            if (resourceId != 0) {
                // Set the image resource to the ImageView
                imageToChange.setImageResource(resourceId)
            } else {
                throw Resources.NotFoundException("Image not found in drawable: $imageName")
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
    }

    // Returns the desired day of the week
    fun getDayOfWeek(dayOffset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset) // Offsetting the day by the desired amount

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }
}