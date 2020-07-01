package com.example.watchcovout_app

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.fragment_time.*
import java.util.*

class TimeFragment : Fragment() {
    private lateinit var mContext: Context
    var detached : Boolean = true
    lateinit var adapter: VisitTimeAdapter
    lateinit var layoutManager : LinearLayoutManager
    val timeList = arrayListOf<VisitTime>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager

        adapter = VisitTimeAdapter((activity as HomeActivity), timeList)
        recyclerView.adapter = adapter

        editTextDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(mContext, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                var dayS = "$dayOfMonth"
                var monthS = "${monthOfYear+1}"
                if (dayOfMonth < 10){
                    dayS = "0$dayOfMonth"
                }

                if (monthOfYear < 9){
                    monthS = "0${monthOfYear+1}"
                }
                val selectedTime = ""+ year + "-"+ monthS + "-" + dayS
                editTextDate.setText( selectedTime )
                (activity as HomeActivity).selectedDate = selectedTime
                getBestTimes(selectedTime, (activity as HomeActivity).chosenPlace!!.id)
            }, year, month, day)

            dpd.show()
        }
    }

    fun getBestTimes(selectedTime : String, placeId : String){
        val urlData = "${resources.getString(R.string.host)}/api/v0/places/$placeId/best?date=$selectedTime"

        // Request a string response from the provided URL.
        val jsonRequestData = JsonArrayRequest(
            Request.Method.GET, urlData, null,
            Response.Listener { response ->

                if (response.length() == 0){
                    Toast.makeText(mContext, "There is no places yet", Toast.LENGTH_SHORT).show()
                }else{
                    timeList.clear()
                    for (i in 0 until response.length()){
                        val item = response.getJSONObject(i)
                        val time = item.getString("time")
                        val nbrPeople = item.getInt("numberOfVisitors")
                        val visitTime = VisitTime(time, nbrPeople, false)
                        timeList.add(visitTime)
                    }

                    adapter.notifyDataSetChanged()
                }
            },
            Response.ErrorListener { Log.d("Error", "Request error") })

        RequestHandler.getInstance(mContext).addToRequestQueue(jsonRequestData)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        detached = false
    }

    override fun onDetach() {
        super.onDetach()
        detached = true
    }
}
