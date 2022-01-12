package com.example.backgroundtask.view

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.backgroundtask.R
import com.example.backgroundtask.adapter.CityAdapter
import com.example.backgroundtask.background.BatteryReceiver
import com.example.backgroundtask.database.DataHelperCityList
import com.example.backgroundtask.model.CityDetails
import com.example.backgroundtask.model.CityListSpace
import java.util.*

class CityScreenActivity : Activity() {
    private var rvCityList: RecyclerView? = null
    private var db = DataHelperCityList(this@CityScreenActivity)
    private var adapter: CityAdapter? = null
    private var batteryReceiver: BatteryReceiver? = null
    private val city = ArrayList<CityDetails>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_screen)
        rvCityList = findViewById(R.id.rvCityList)
        createCityDataList()
        setAdapterRecyclerView()
    }

    private fun setAdapterRecyclerView() {
        adapter = CityAdapter(city, this@CityScreenActivity)
        rvCityList!!.adapter = adapter
        rvCityList!!.addItemDecoration(CityListSpace(40))
        rvCityList!!.addItemDecoration(
            DividerItemDecoration(
                this@CityScreenActivity,
                DividerItemDecoration.VERTICAL
            )
        )
        rvCityList!!.layoutManager = LinearLayoutManager(this@CityScreenActivity)
    }

    private fun createCityDataList() {
        val getData = db.cityData
        if (getData.count == 0) {
            Toast.makeText(this@CityScreenActivity, "No City Bookmarked Now", Toast.LENGTH_SHORT)
                .show()
        }
        while (getData.moveToNext()) {
            city.add(
                CityDetails(
                    getData.getInt(0),
                    getData.getString(3),
                    getData.getDouble(5),
                    getData.getInt(6),
                    getData.getDouble(1),
                    getData.getDouble(2),
                    getData.getString(4),
                    getData.getString(7)
                )
            )
        }
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