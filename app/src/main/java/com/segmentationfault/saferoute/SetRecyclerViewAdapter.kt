package com.segmentationfault.saferoute

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.segmentationfault.saferoute.models.Acceleration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SetRecyclerViewAdapter (private val context: Context, private val accelerations: MutableList<Acceleration>):
    RecyclerView.Adapter<SetRecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.acceleration_row, parent, false)
        return MyViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertTime(timeStamp: String): String {
        val instant = Instant.parse(timeStamp)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
        val result = formatter.format(instant)
        Log.i("abc", result)
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.time.text = convertTime(accelerations[position].time)
        holder.accelerationAmount.text = accelerations[position].accelerationAmount.toString()

    }

    override fun getItemCount(): Int {
        return accelerations.size
    }

    class MyViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        val time: TextView = itemView.findViewById(R.id.time)
        val accelerationAmount: TextView = itemView.findViewById(R.id.accelerationAmount)
    }

    fun updateAccelerations(newAccelerations: List<Acceleration>) {
        accelerations.clear()
        accelerations.addAll(newAccelerations)
        notifyDataSetChanged()
    }
}