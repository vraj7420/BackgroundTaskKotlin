package com.example.backgroundtask.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.backgroundtask.R
import com.example.backgroundtask.background.BatteryReceiver
import com.example.backgroundtask.database.DataHelperCityList
import com.example.backgroundtask.model.CityDetails
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class WeatherDetailsActivity : Activity() {
    var id = 0
    private val db = DataHelperCityList(this@WeatherDetailsActivity)
    private var c: Cursor? = null
    private var cityDetails: CityDetails? = null
    private var batteryReceiver: BatteryReceiver? = null

    companion object {
        var tvCity: TextView? = null
        var tvLat: TextView? = null
        var tvLong: TextView? = null
        var tvWeather: TextView? = null
        var tvTemperature: TextView? = null
        var tvHumidity: TextView? = null
        var tvWindSpeed: TextView? = null
    }


    private lateinit var getWeather: WeatherDetailsAsyncTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_details)
        init()
        val i = intent
        id = i.extras!!.getInt("id")
        c = db.getDateCityFromID(id)
        createDataOfWeather()
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
        var cityName: String = ""
        var lat: Double? = null
        var long: Double? = null
        if (c!!.count == 0) {
            Snackbar.make(findViewById(R.id.mainActivity),"No City Right Now",Snackbar.LENGTH_SHORT).show()
            Toast.makeText(this@WeatherDetailsActivity, "No City Right Now", Toast.LENGTH_SHORT).show()
        }
        while (c!!.moveToNext()) {
            cityName = c!!.getString(3)
            lat = c!!.getDouble(1)
            long = c!!.getDouble(2)

        }
        tvLat?.text = String.format("%.5f", lat)
        tvLong?.text = String.format("%.5f", long)
        getWeather = WeatherDetailsAsyncTask(this@WeatherDetailsActivity)
        getWeather.execute(cityName)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")

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


    class WeatherDetailsAsyncTask(private var ctx: Context) : AsyncTask<String, String, Void>() {
        private var requestQueue: RequestQueue? = null
        private var pdWaiting: ProgressDialog? = null
        private var cityName: String? = null
        private var output = ""
        private var weatherDescription = ""
        private var temperature = ""
        private var humidity = ""
        private var windSpeed = ""

        override fun onPreExecute() {
            pdWaiting = ProgressDialog(ctx)
            pdWaiting!!.setCanceledOnTouchOutside(false)
            pdWaiting!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            pdWaiting!!.setCancelable(true)
            pdWaiting!!.max = 10
            pdWaiting!!.progress = 0
            pdWaiting!!.setMessage("Please Wait ....")
            pdWaiting!!.show()
        }


        override fun onPostExecute(unused: Void?) {
            super.onPostExecute(unused)
            pdWaiting!!.progress = 10
            pdWaiting!!.dismiss()


        }

        override fun doInBackground(vararg params: String?): Void? {
            cityName = params[0]
            requestQueue = Volley.newRequestQueue(ctx)
            val tempUrl =
                "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=fae7190d7e6433ec3a45285ffcf55c86"
            val jsonObjectRequest =
                JsonObjectRequest(Request.Method.GET, tempUrl, null, { response: JSONObject ->
                    Log.d("response get", response.toString())
                    try {
                        val jsonArrayWeather = response.getJSONArray("weather")
                        val jsonObjectWeather = jsonArrayWeather.getJSONObject(0)
                        weatherDescription += jsonObjectWeather.getString("description")
                        val jsonObjectMain = response.getJSONObject("main")
                        temperature += jsonObjectMain.getDouble("temp") - 273.15
                        humidity += jsonObjectMain.getInt("humidity")
                        val jsonObjectWind = response.getJSONObject("wind")
                        windSpeed += jsonObjectWind.getString("speed")
                        tvCity?.text = cityName
                        tvWeather?.text = weatherDescription
                        tvWindSpeed?.text = windSpeed + "m/s"
                        tvHumidity?.text = "$humidity%"
                        tvTemperature?.text = temperature + "C"
                        output += "City" + cityName + "Weather Description" + weatherDescription + "temperature" + temperature + "C" + "humidity" + humidity + "%" + " Wind Speed" + windSpeed + "m/s"
                        Log.d("output", output)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }) { }
            requestQueue!!.add(jsonObjectRequest)
            return null
        }

    }

}