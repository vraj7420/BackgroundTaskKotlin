package com.example.backgroundtask.background

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.backgroundtask.database.DataHelperCityList
import com.example.backgroundtask.view.MainActivity
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.util.*

class BookmarkCityAsyncTask(private var ctx: Context) : AsyncTask<LatLng?, String?, Void?>() {
    private var requestQueue: RequestQueue? = null
    private var latLngOfClickLocation: LatLng? = null
    private var cityName = ""
    private var pdWaiting: ProgressDialog? = null
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
     override fun doInBackground(vararg params: LatLng?): Void? {
        latLngOfClickLocation =params[0]
        requestQueue = Volley.newRequestQueue(ctx)
        val tempUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latLngOfClickLocation!!.latitude + "&lon=" + latLngOfClickLocation!!.longitude + "&appid=" + "fae7190d7e6433ec3a45285ffcf55c86"
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, tempUrl, null, { response: JSONObject ->
                Log.d("response get", response.toString())
                var output = ""
                try {
                    cityName += response.getString("name")
                    Log.d("City", cityName)
                    val markerOptions = MarkerOptions()
                    markerOptions.position(latLngOfClickLocation!!)
                    markerOptions.title(cityName)
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    val cityAdd = MainActivity.mMap?.addMarker(markerOptions)
                    cityAdd?.let { markerDelete.add(it) }
                    val jsonArrayWeather = response.getJSONArray("weather")
                    val jsonObjectWeather = jsonArrayWeather.getJSONObject(0)
                    val weatherDescription = jsonObjectWeather.getString("description")
                    val jsonObjectMain = response.getJSONObject("main")
                    val temperature = jsonObjectMain.getDouble("temp") - 273.15
                    val humidity = jsonObjectMain.getInt("humidity")
                    val jsonObjectWind = response.getJSONObject("wind")
                    val windSpeed = jsonObjectWind.getString("speed")
                    output += "City" + cityName + "Weather Description" + weatherDescription + "temperature" + temperature + "C" + "humidity" + humidity + "%" + " Wind Speed" + windSpeed + "m/s"
                    Log.d("output", output)
                    val db = DataHelperCityList(ctx)
                    val temp = db.insertData(
                        cityName,
                        latLngOfClickLocation!!.latitude,
                        latLngOfClickLocation!!.longitude,
                        weatherDescription,
                        temperature,
                        humidity,
                        windSpeed
                    )
                    Log.d("insert Data", temp.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }) { }
        requestQueue!!.add(jsonObjectRequest)
        return null
    }

    override fun onPostExecute(unused: Void?) {
        super.onPostExecute(unused)
        pdWaiting!!.progress = 10
        pdWaiting!!.dismiss()
    }

    companion object {
        @JvmField
        var markerDelete = ArrayList<Marker>()
    }


}