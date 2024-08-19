package com.example.weather_app.classes

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import android.util.Log
import androidx.lifecycle.lifecycleScope

import java.io.BufferedReader
import java.io.InputStreamReader
import com.opencsv.CSVReader

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.weather_app.MainActivity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

// Resets the flag in SharedPreferences that indicates whether the database is populated
fun resetDatabaseFlag(context: Context) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("city_database_prefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putBoolean("isDatabasePopulated", false)
        apply()
    }
}

// Function to copy the pre-populated database from the assets folder to the database directory
private fun copyDatabaseFromAssets(context: Context) {
    val dbPath = context.getDatabasePath("city_database").path

    // Check if the database already exists
    if (!File(dbPath).exists()) {
        context.assets.open("city_database").use { inputStream ->
            FileOutputStream(dbPath).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                // Read from input stream and write to output stream
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
            }
        }
    }
}

// Function to read the CSV file and populate the database with city data
fun readCSVAndPopulateDatabase(context: Context, activity: MainActivity) {
    // Launch a coroutine in the lifecycleScope of the activity to perform database operations
    activity.lifecycleScope.launch(Dispatchers.IO) {
        val sharedPreferences =
            context.getSharedPreferences("city_database_prefs", Context.MODE_PRIVATE)
        // Check if the database has already been populated
        val isDatabasePopulated = sharedPreferences.getBoolean("isDatabasePopulated", false)
        val cityDAO = AppDatabase.getDatabase(context).cityDao()

        // If the database is already populated, exit the function
        if (isDatabasePopulated) {
            return@launch
            //cityDAO.clearAllCities() // deleting all data for testing purposes
        }

        // Open the CSV file from the assets folder
        val inputStream = context.assets.open("cities_all.csv")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val csvReader = CSVReader(bufferedReader)

        // Skip the header row of the CSV file
        csvReader.readNext()

        try {
            var line: Array<String>?
            // Loop through each line of the CSV file
            while (csvReader.readNext().also { line = it } != null) {
                // Check if the line has the expected number of columns
                if (line!!.size >= 7) {
                    try {
                        // Parse the CSV line and create a City object
                        val cityId = line!![0].toLong()
                        val cityName = line!![1]
                        val stateCode = line!![2]
                        val countryCode = line!![3]
                        val countryFull = line!![4]
                        val lat = line!![5].toDouble()
                        val lon = line!![6].toDouble()

                        val city = City(cityId, cityName, stateCode, countryCode, countryFull, lat, lon)

                        // Insert the city into the database
                        cityDAO.insertCity(city)
                        Log.d("CSV_LOG", "City inserted: $cityName")
                    } catch (e: NumberFormatException) {
                        // Handle number format exceptions
                        Log.e("CSV_PARSER", "Error parsing line: ${line!!.joinToString()}", e)
                    }
                } else {
                    // Log invalid line formats
                    Log.e("CSV_PARSER", "Invalid line format: ${line!!.joinToString()}")
                }
            }
        } catch (e: Exception) {
            // Handle exceptions during CSV reading
            Log.e("CSV_PARSER", "Exception while reading CSV", e)
        } finally {
            // Close the CSV reader and log the completion
            Log.d("CSV_PARSER", "Finished reading CSV")
            csvReader.close()
        }

        // Update SharedPreferences to indicate that the database has been populated
        with(sharedPreferences.edit()) {
            putBoolean("isDatabasePopulated", true)
            apply()
        }
    }
}

// Defines the City entity, which corresponds to a table in the database
@Entity(tableName = "city")
data class City(
    @PrimaryKey val city_id: Long,  // Primary key for the table, unique for each city
    val city_name: String,          // Name of the city
    val state_code: String,         // State code of the city
    val country_code: String,       // Country code of the city
    val country_full: String,       // Full name of the country
    val lat: Double,                // Latitude of the city
    val lon: Double                 // Longitude of the city
)

// Define the DAO (Data Access Object) interface with methods to interact with the database
@Dao
interface CityDao {
    // Insert a city into the database. If there's a conflict (e.g., duplicate city_id), replace the existing entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCity(city: City)

    // Query to search for cities by name. The search is case-insensitive and supports partial matches
    @Query("SELECT * FROM city WHERE city_name LIKE :searchQuery LIMIT 10")
    fun searchCitiesByName(searchQuery: String): List<City>

    // Query to retrieve a city by its ID
    @Query("SELECT * FROM city WHERE city_id = :cityId LIMIT 1")
    fun getCityById(cityId: Long): City?

    // Query to retrieve a city by its name
    @Query("SELECT * FROM city WHERE city_name = :cityName LIMIT 1")
    fun getCityByName(cityName: String): City?

    // Query to delete all cities from the database
    @Query("DELETE FROM city")
    suspend fun clearAllCities()
}

// Define the Room database class, which should be abstract and extend RoomDatabase
@Database(entities = [City::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Abstract method to get an instance of the DAO
    abstract fun cityDao(): CityDao

    // Singleton pattern to ensure only one instance of the database exists
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Method to get the database instance. If it's not created yet, build it.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Synchronized block to ensure only one thread can access this at a time.

                // Create a new instance of the database, using the pre-populated database from the assets.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "city_database" // Name of the database file in the app's internal storage.
                )
                    .createFromAsset("city_database") // Copy the pre-populated database from the assets folder.
                    .build()

                // Assign the newly created instance to the INSTANCE variable.
                INSTANCE = instance
                // Return the newly created instance.
                instance
            }
        }
    }
}

