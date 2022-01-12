package com.example.backgroundtask.background

import android.content.BroadcastReceiver
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import com.example.backgroundtask.R

class BatteryReceiver(
    private var menuItemBatteryPercentage: MenuItem,
    private var menuItemBatteryPercentageText: MenuItem
) : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val percentage = intent.getIntExtra("level", 0)
        Log.d("battery", percentage.toString())
        menuItemBatteryPercentage.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menuItemBatteryPercentage.setIcon(R.drawable.ic_battery_full)
        menuItemBatteryPercentage.title = "$percentage%"
        menuItemBatteryPercentageText.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menuItemBatteryPercentageText.title = "$percentage%"
    }
}