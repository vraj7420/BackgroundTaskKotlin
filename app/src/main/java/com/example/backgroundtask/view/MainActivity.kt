package com.example.backgroundtask.view

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.location.LocationListener
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.backgroundtask.R
import com.example.backgroundtask.adapter.CityListAdapter
import com.example.backgroundtask.background.BatteryReceiver
import com.example.backgroundtask.background.BookmarkCityAsyncTask
import com.example.backgroundtask.database.DataHelperCityList
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


class MainActivity : FragmentActivity(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    OnMarkerClickListener {

    private var connectivityManager: ConnectivityManager? = null
    private var networkInfo: NetworkInfo? = null
    private var mLastLocation: Location? = null
    private var mCurrLocationMarker: Marker? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var batteryReceiver: BatteryReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkLocationPermission()
        val mapFragment =
            (supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) === PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        mMap?.let { onMapReady(it) }
                    }
                } else {

                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    Toast.makeText(
                        this,
                        "Location Permission For Current Location",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                return
            }
        }
    }



    private fun checkInterNetConnectivity(): Boolean {
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        networkInfo = connectivityManager!!.activeNetworkInfo
        return networkInfo != null && networkInfo!!.isConnected
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        buildGoogleApiClient()
        val db = DataHelperCityList(this@MainActivity)
        val c: Cursor = db.cityData
        if (c.count == 0) {
            Toast.makeText(this@MainActivity, "No City Bookmarked Now", Toast.LENGTH_SHORT).show()
        }
        while (c.moveToNext()) {
            val latLng = LatLng(c.getDouble(1), c.getDouble(2))
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title(c.getString(3))
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            val storedCityMarker = mMap!!.addMarker(markerOptions)
            BookmarkCityAsyncTask.markerDelete.add(storedCityMarker)
        }


        mMap!!.setOnMapClickListener { latLng1: LatLng? ->
            var checkInternet = checkInterNetConnectivity()
            if (checkInternet) {
                val builder = AlertDialog.Builder(
                    this@MainActivity,
                    android.R.style.Theme_Material_Light_Dialog_Alert
                )
                builder.setTitle(getString(R.string.titleDialogBookmarked))
                builder.setMessage(getString(R.string.messageBookmarkedDialog))
                builder.setPositiveButton("yes") { _: DialogInterface?, _: Int ->
                    val bookmarkCity = BookmarkCityAsyncTask(this@MainActivity)
                    bookmarkCity.execute(latLng1)
                }
                builder.setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                val alertDialog = builder.create()
                alertDialog.show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "please Connect to on Internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }




    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 500000
        mLocationRequest!!.fastestInterval = 30000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient!!,
                mLocationRequest!!
            ) { location: Location -> this.onLocationChanged(location) }
        }
    }

    override fun onConnectionSuspended(i: Int) {}

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(getString(R.string.currentLocation))
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        mCurrLocationMarker = mMap!!.addMarker(markerOptions)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient!!) { location: Location ->
                this.onLocationChanged(
                    location
                )
            }
        }
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main_activity, menu)
        val menuItemBatteryPercentage = menu.findItem(R.id.menuItemBatteryPercentage)
        val menuItemBatteryPercentageText = menu.findItem(R.id.menuItemBatteryPercentageText)
        batteryReceiver = BatteryReceiver(menuItemBatteryPercentage, menuItemBatteryPercentageText)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuItemBookmarkedCity) {
            val intentGoCityScreen = Intent(this@MainActivity, CityScreenActivity::class.java)
            startActivity(intentGoCityScreen)
        }
        return true
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }


    override fun onStop() {
        super.onStop()
        unregisterReceiver(batteryReceiver)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        for (i in CityListAdapter.deletedLatLong.indices) {
            for (j in BookmarkCityAsyncTask.markerDelete.indices) {
                if (BookmarkCityAsyncTask.markerDelete[j].position.latitude == CityListAdapter.deletedLatLong[i].latitude && BookmarkCityAsyncTask.markerDelete[j].position.longitude == CityListAdapter.deletedLatLong[i].longitude) {
                    BookmarkCityAsyncTask.markerDelete[j].remove()
                }
            }
        }
    }

    companion object {
        var mMap: GoogleMap? = null
    }
}