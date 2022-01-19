package com.example.backgroundtask.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.backgroundtask.R
import com.example.backgroundtask.adapter.CityListAdapter.CityViewHolder
import com.example.backgroundtask.database.DataHelperCityList
import com.example.backgroundtask.model.CityDetails
import com.example.backgroundtask.view.WeatherDetailsActivity
import com.google.android.gms.maps.model.LatLng
import java.util.*

class CityListAdapter(var cityList: ArrayList<CityDetails>, var ctx: Context) :
    RecyclerView.Adapter<CityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val recyclerInflater = LayoutInflater.from(ctx)
        @SuppressLint("InflateParams") val recyclerView =
            recyclerInflater.inflate(R.layout.city_list_recyler_view_layout, null)
        return CityViewHolder(recyclerView)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val cityTemp = cityList[position]
        holder.city.text = cityTemp.cityName
        holder.latitude.text = String.format("%.5f", cityTemp.latitude)
        holder.longitude.text = String.format("%.5f", cityTemp.longitude)
    }

    override fun getItemCount(): Int {
        return cityList.size
    }

    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener/*, OnLongClickListener */{
        var city: TextView
        var latitude: TextView
        var longitude: TextView
         var imgBtnDelete:ImageButton
        override fun onClick(v: View) {
            val position = adapterPosition
            val i = Intent(ctx, WeatherDetailsActivity::class.java)
            val temp = cityList[position]
            val id = temp.id
            i.putExtra("id", id)
            ctx.startActivity(i)
        }

/*        override fun onLongClick(v: View): Boolean {
            val position = adapterPosition
            val temp = cityList[position]
            val id = temp.id
            val tempLatLong = LatLng(temp.latitude, temp.longitude)
            val builder =
                AlertDialog.Builder(ctx, android.R.style.Theme_Material_Light_Dialog_Alert)
            builder.setTitle(" Delete")
            builder.setMessage("Do you Want  Delete Bookmark City?")
            builder.setPositiveButton("yes") { _: DialogInterface?, _: Int ->
                val deleteCity = DataHelperCityList(
                    ctx
                )
                val delete = deleteCity.deleteCity(id)
                if (delete) {
                    deletedLatLong.add(tempLatLong)
                    cityList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
            builder.setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
            val alertDialog = builder.create()
            alertDialog.show()
            return true
        }
*/
        init {
            itemView.setOnClickListener(this)
            //itemView.setOnLongClickListener(this)
            city = itemView.findViewById<View>(R.id.tvCityName) as TextView
            latitude = itemView.findViewById(R.id.tvLatitude)
            longitude = itemView.findViewById(R.id.tvLongitude)
            imgBtnDelete=itemView.findViewById(R.id.imgBtnDelete)
            imgBtnDelete.setOnClickListener {
                val position = adapterPosition
                val temp = cityList[position]
                val id = temp.id
                val tempLatLong = LatLng(temp.latitude, temp.longitude)
                val builder =
                    AlertDialog.Builder(ctx, android.R.style.Theme_Material_Light_Dialog_Alert)
                builder.setTitle(" Delete")
                builder.setMessage("Do you Want  Delete Bookmark City?")
                builder.setPositiveButton("yes") { _: DialogInterface?, _: Int ->
                    val deleteCity = DataHelperCityList(
                        ctx
                    )
                    val delete = deleteCity.deleteCity(id)
                    if (delete) {
                        deletedLatLong.add(tempLatLong)
                        cityList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
                builder.setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                val alertDialog = builder.create()
                alertDialog.show()

            }
        }
    }

    companion object {
        @JvmField
        var deletedLatLong: MutableList<LatLng> = ArrayList()
    }
}