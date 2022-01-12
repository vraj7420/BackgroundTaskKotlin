package com.example.backgroundtask.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataHelperCityList(context: Context?) :
    SQLiteOpenHelper(context, "AddCityListNewOne.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create Table CityList(cityId NUMBER,latitude NUMBER,longitude NUMBER,cityName TEXT,weatherDescription TEXT,temperature NUMBER,humidity NUMBER,windSpeed TEXT)")
    }

    val cityData: Cursor
        get() {
            val db = this.writableDatabase
            return db.rawQuery("Select * From CityList", null)
        }

    fun getDateCityFromID(id: Int): Cursor {
        val db = this.writableDatabase
        return db.rawQuery("select * from CityList Where cityID=?", arrayOf(id.toString()))
    }

    fun insertData(
        cityName: String?,
        latitude: Double,
        longitude: Double,
        weatherDescription: String?,
        temperature: Double,
        humidity: Int,
        windSpeed: String?
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("CityID", id)
        id += 1
        contentValues.put("cityName", cityName)
        contentValues.put("weatherDescription", weatherDescription)
        contentValues.put("latitude", latitude)
        contentValues.put("longitude", longitude)
        contentValues.put("temperature", temperature)
        contentValues.put("humidity", humidity)
        contentValues.put("windSpeed", windSpeed)
        val result = db.insert("CityList", null, contentValues)
        return result != -1L
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun deleteCity(id: Int): Boolean {
        val db = this.writableDatabase
        return db.delete("CityList", "CityID" + "=?", arrayOf(id.toString())) > 0
    }

    companion object {
        var id = 1
    }
}