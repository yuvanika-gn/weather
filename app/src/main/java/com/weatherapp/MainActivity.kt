package com.weatherapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvTemperature: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvChanceofRain: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvVisibility: TextView
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var weatherApiService: WeatherApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sharedPreference = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        val currentCity =
            intent.getStringExtra("CITY_NAME") ?: getLastSavedCityName()

        tvTemperature = findViewById(R.id.tvTemperature)
        tvDescription = findViewById(R.id.tvDescription)
        tvCity = findViewById(R.id.tvCity)
        tvWind = findViewById(R.id.tvWind)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvVisibility = findViewById(R.id.tvVisibility)
//        tvChanceofRain = findViewById(R.id.tvChanceofRain)

        lottieAnimationView = findViewById(R.id.lottieAnimation_Main)
        floatingActionButton = findViewById(R.id.fab)

        floatingActionButton.setOnClickListener {
            openSearchActivity()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApiService = retrofit.create(WeatherApiService::class.java)

        if (currentCity != null) {
            searchWeather(currentCity)
            saveCityName(currentCity)
        }
    }

    private fun searchWeather(cityName: String) {
        val apiKey = "YOUR_API_KEY_HERE"

        CoroutineScope(Dispatchers.IO).launch {
            val response = weatherApiService.getWeather(cityName, apiKey)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()

                    weatherResponse?.let {
                        updateWeatherUI(it.main?.temp, it.weather?.getOrNull(0)?.description, it.name)
                        updateAdditionalWeatherUI(it)
                    }

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to get weather data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateWeatherUI(
        temperature: Double?,
        description: String?,
        city: String?
    ) {

        temperature?.let {
            val celsius = it - 273.15
            tvTemperature.text = "${celsius.toInt()}°C"
        }

        description?.let {
            tvDescription.text = it
            updateLottieAnimation(it)
        }

        tvCity.text = city ?: "Unknown"
    }

    private fun updateLottieAnimation(description: String) {
        val animationResId = when (description.lowercase(Locale.getDefault())) {
            "clear sky" -> R.raw.clear_sky
            "rain" -> R.raw.rainy
            "snow" -> R.raw.snow
            "sunny" -> R.raw.sunny
            else -> R.raw.clear_sky
        }

        lottieAnimationView.setAnimation(animationResId)
        lottieAnimationView.playAnimation()
    }

    private fun updateAdditionalWeatherUI(weatherResponse: WeatherResponse) {

        weatherResponse.rain?.let {
            val chance = it.oneHour ?: 0.0
            tvChanceofRain.text = "Chance of Rain: $chance%"
        }

        weatherResponse.wind?.let {
            tvWind.text = "Wind Speed: ${it.speed} m/s"
        }

        weatherResponse.main?.let {
            tvHumidity.text = "Humidity: ${it.humidity}%"
        }

        tvVisibility.text = "Visibility: ${weatherResponse.visibility ?: 0} meters"
    }

    private fun openSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }

    private fun saveCityName(cityName: String) {
        sharedPreference.edit().putString("CITY_NAME", cityName).apply()
    }

    private fun getLastSavedCityName(): String? {
        return sharedPreference.getString("CITY_NAME", null)
    }

    interface WeatherApiService {
        @GET("weather")
        suspend fun getWeather(
            @Query("q") cityName: String,
            @Query("appid") apiKey: String
        ): Response<WeatherResponse>
    }

    data class WeatherResponse(
        val main: Main?,
        val weather: List<Weather>?,
        val name: String?,
        val rain: Rain?,
        val wind: Wind?,
        val visibility: Int?,
        @SerializedName("uvi")
        val uvIndex: Double?
    )

    data class Main(
        val temp: Double?,
        val humidity: Int?
    )

    data class Weather(
        val description: String?
    )

    data class Rain(
        @SerializedName("1h")
        val oneHour: Double?
    )

    data class Wind(
        val speed: Double?
    )
}