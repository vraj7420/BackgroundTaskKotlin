package com.example.backgroundtask.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import com.example.backgroundtask.R
import com.example.backgroundtask.background.BatteryReceiver
import com.example.backgroundtask.database.DataHelperCityList
import com.example.backgroundtask.model.CityDetails

class WeatherDetails : Activity() {
    var id = 0
    private val db = DataHelperCityList(this@WeatherDetails)
    private var c: Cursor? = null
    private var cityDetails: CityDetails? = null
    private var batteryReceiver: BatteryReceiver? = null
    private var tvCity: TextView? = null
    private var tvLat: TextView? = null
    private var tvLong: TextView? = null
    private var tvWeather: TextView? = null
    private var tvTemperature: TextView? = null
    private var tvHumidity: TextView? = null
    private var tvWindSpeed: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_details)
        init()
        val i = intent
        id = i.extras!!.getInt("id")
        c = db.getDateCityFromID(id)
        createDataOfWeather()
        setDataOfWeather()
    }

    private fun init() {
        tvCity = findViewById(R.id.tvCity)
        tvLat = findViewById(R.id.tvLatitude)
        tvLong = findViewById(R.id.tvLongitude)
        tvWeather = findViewById(R.id.tvWeather)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWindSpeed = findViewById(R.id.tvWindSpeed)
        tvTemperature = findViewById(R.id.tvTemperature)
    }

    private fun createDataOfWeather() {
        if (c!!.count == 0) {
            Toast.makeText(this@WeatherDetails, "No City Right Now", Toast.LENGTH_SHORT).show()
        }
        while (c!!.moveToNext()) {
            cityDetails = CityDetails(
                c!!.getInt(0),
                c!!.getString(3),
                c!!.getDouble(5),
                c!!.getInt(6),
                c!!.getDouble(1),
                c!!.getDouble(2),
                c!!.getString(4),
                c!!.getString(7)
            )
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun setDataOfWeather() {
        tvCity!!.text = cityDetails!!.cityName
        tvLat!!.text = String.format("%.2f", cityDetails!!.latitude)
        tvLong!!.text = String.format("%.2f", cityDetails!!.longitude)
        tvWeather!!.text = cityDetails!!.weatherDescription
        tvTemperature!!.text = String.format("%.2f", cityDetails!!.temperature)+"C"
        tvHumidity!!.text = cityDetails!!.humidity.toString()+"%"
        tvWindSpeed!!.text = cityDetails!!.windSpeed+"m/s"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_for_all_screen, menu)
        val menuItemBatteryPercentage = menu.findItem(R.id.menuItemBatteryPercentage)
        val menuItemBatteryPercentageText = menu.findItem(R.id.menuItemBatteryPercentageText)
        batteryReceiver = BatteryReceiver(menuItemBatteryPercentage, menuItemBatteryPercentageText)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return true
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(batteryReceiver)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
}