package com.example.ncmonitor

/**
 * Created by Davide Antonino Giorgio on 02-07-2020.
 */

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okhttp3.Credentials.basic
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.math.round

const val GB = 1073741824
const val Byte = 1024

/* number of cores in your server. 4 for RPi4. */
const val N_CORES = 4


class MainActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()

        val extras = intent.extras
        if (extras != null)
            showResults(extras.getString("response")!!)

        //requestNcStatus()
    }


    private fun showResults(response: String)
    {
        try {
            val json = JSONObject(response)
            Log.d("requestNcStatus","Request Successful!!")
            Log.d("json_response", json.toString())
            warning_icon.visibility = View.GONE
            ok_icon.visibility = View.VISIBLE

            val responseObject = json.getJSONObject("ocs")
            val metaObject = responseObject.getJSONObject("meta")

            val statusCode = metaObject.getInt("statuscode")

            if (statusCode == 200) {
                status_code_response_layout.visibility = View.GONE
                status_message_response_layout.visibility = View.GONE

                val dataObject =  responseObject.getJSONObject("data")
                val nextcloudObject =  dataObject.getJSONObject("nextcloud")
                val systemObject =  nextcloudObject.getJSONObject("system")

                // cpu load
                val cpuLoad = systemObject.getJSONArray("cpuload")[0] as Double
                // ram
                val ramTotal = systemObject.getLong("mem_total")
                val ramFree = systemObject.getLong("mem_free")
                // swap
                val swapTotal = systemObject.getLong("swap_total")
                val swapFree = systemObject.getLong("swap_free")
                // disk
                val diskFree = systemObject.getDouble("freespace")

                updateViews(cpuLoad, ramTotal-ramFree, ramTotal,
                    swapTotal-swapFree, swapTotal, diskFree)


            } else {
                cpu_layout.visibility = View.GONE
                ram_layout.visibility = View.GONE
                swap_layout.visibility = View.GONE
                disk_layout.visibility = View.GONE
                status_code_response_used_placeholder.text = statusCode.toString()
                status_message_response_placeholder.text = metaObject.getString("message")
            }

        } catch (e: JSONException) {
            Log.d("requestNcStatus","JsonException!!")
            warning_icon.visibility = View.GONE
            error_icon.visibility = View.VISIBLE
            e.printStackTrace()
        }
    }

    fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun updateViews(cpuLoad :Double, ramBusy :Long, ramTotal :Long, swapBusy :Long, swapTotal :Long, diskFree :Double)
    {

        val cpuLoad3Digit = Math.round(((cpuLoad*100)/N_CORES) * 1000.0) / 1000.0
        val cpuLoad2Digit = Math.round(cpuLoad3Digit * 100.0) / 100.0

        cpu_load_placeholder.text =  cpuLoad2Digit.toString()
        ram_used_placeholder.text = (ramBusy / Byte).toString()
        ram_total_placeholder.text = (ramTotal / Byte).toString()
        swap_used_placeholder.text = (swapBusy /Byte).toString()
        swap_total_placeholder.text = (swapTotal / Byte).toString()

        disk_used_placeholder.text = (diskFree / GB).format(2)
    }
}
