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


const val PERMISSION_REQUEST = 10

/* your Nextcloud username */
const val USERNAME = ""

/* your Nextcloud password */
const val PASSWORD = ""

/* your Nextcloud endpoint. ?format=json is required */
const val URL = ""

/* number of cores in your server. 4 for RPi4. */
const val N_CORES = 4



class MainActivity : WearableActivity() {

    var client = OkHttpClient()
    var request = OkHttpRequest(client)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()

        checkAndRequestPermissions()

        requestNcStatus()
    }

    private fun checkAndRequestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val permissions = arrayOf(
                android.Manifest.permission.INTERNET
            )

            val permissionsDenied = arrayListOf<String>()

            for (permission in permissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    permissionsDenied.add(permission)
                }
            }

            if(permissionsDenied.isNotEmpty())
                requestPermissions(permissionsDenied.toTypedArray(), PERMISSION_REQUEST)
        }
    }

    private fun requestNcStatus() {

        val credentials = basic(USERNAME, PASSWORD)

        request.GET(credentials, URL, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                runOnUiThread{
                    try {
                        val json = JSONObject(responseData!!)
                        Log.d("requestNcStatus","Request Successful!!")
                        Log.d("json_response", json.toString())
                        warning_icon.visibility = View.GONE
                        ok_icon.visibility = View.VISIBLE

                        val responseObject = json.getJSONObject("ocs")
                        val metaObject = responseObject.getJSONObject("meta")
                        status_code_response_used_placeholder.text = metaObject.getInt("statuscode").toString()
                        status_message_response_placeholder.text = metaObject.getString("message")

                        val dataObject =  responseObject.getJSONObject("data")
                        val nextcloudObject =  dataObject.getJSONObject("nextcloud")
                        val systemObject =  nextcloudObject.getJSONObject("system")
                        val cpuLoadArray = systemObject.getJSONArray("cpuload")


                        // TODO: sistemare questa percentuale
                        // https://medium.com/devops-process-and-tools/how-to-measure-cpu-load-v-cpu-percentage-and-examples-da6057e39111
                        cpu_load_placeholder.text = (((cpuLoadArray[0] as Double)*N_CORES)/100).toString()


                        //val docs = json.getJSONArray("docs")
                        //this@MainActivity.fetchComplete()
                    } catch (e: JSONException) {
                        Log.d("requestNcStatus","JsonException!!")
                        warning_icon.visibility = View.GONE
                        error_icon.visibility = View.VISIBLE
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("requestNcStatus","Request Failure!!")
                runOnUiThread {
                    warning_icon.visibility = View.GONE
                    error_icon.visibility = View.VISIBLE
                    cpu_layout.visibility = View.GONE
                    ram_layout.visibility = View.GONE
                    swap_layout.visibility = View.GONE
                    disk_layout.visibility = View.GONE
                    status_code_response_used_placeholder.text = "404"
                    status_message_response_placeholder.text = "Server not found"
                }
            }
        })


    }
}
