package com.example.ncmonitor

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Credentials.basic
import org.json.JSONObject
import okhttp3.*
import org.json.JSONException
import java.io.IOException



const val PREF_NAME = "USER"
const val PREF_KEY = "profile"
const val PERMISSION_REQUEST = 10

class LoginActivity : WearableActivity()
{

    var client = OkHttpClient()
    var request = OkHttpRequest(client)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Enables Always-on
        setAmbientEnabled()

        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val userInstance = sharedPref.getString(PREF_KEY, null)

        if (userInstance != null) {
            //There is an instance of a user on the watch, retrieve data
            val userJson = JSONObject(userInstance)

            requestNcStatus(
                userJson.get("serverURL").toString(),
                userJson.get("username").toString(),
                userJson.get("password").toString()
            )
        }

        checkAndRequestPermissions()
        btn_next.setOnClickListener { onNextClicked() }
    }

    private fun checkAndRequestPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                android.Manifest.permission.INTERNET
            )
            val permissionsDenied = arrayListOf<String>()
            for (permission in permissions)
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED)
                    permissionsDenied.add(permission)

            if(permissionsDenied.isNotEmpty())
                requestPermissions(permissionsDenied.toTypedArray(), PERMISSION_REQUEST)
        }
    }

    private fun showProgressBar()
    {
        login_scrollview_layout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun navigateToResults(responseData: String)
    {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("response", responseData)
        startActivity(intent)
    }

    private fun requestNcStatus(serverURL :String, username :String, password :String)
    {
        Log.d("inputs","ServerURL: $serverURL \nnc_username: $username\nnc_password: $password")
        showProgressBar()
        val credentials = basic(username, password)

        request.GET(credentials, serverURL, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val userInfo = JSONObject()
                userInfo.put("serverURL", serverURL)
                userInfo.put("username", username)
                userInfo.put("password", password)

                val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

                with(sharedPref.edit()) {
                    putString(PREF_KEY, userInfo.toString())
                    apply()
                }

                val responseData = response.body()?.string()

                try {
                    // navigate to the next activity
                    navigateToResults(responseData!!)
                    // now destroying the current activity
                    finish()
                } catch (e: JSONException) {
                    Log.d("requestNcStatus","JsonException!!")
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                //todo: handle it showing info to the user
                login_scrollview_layout.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        })

    }

    private fun onNextClicked()
    {
        if (nc_server_input.text.isNotEmpty() &&
            nc_username_input.text.isNotEmpty() &&
            nc_password_input.text.isNotEmpty()) {

            var serverURL = nc_server_input.text.toString()
            if (!URLUtil.isValidUrl(serverURL)) { //invalid url
                Log.d("inputs","ServerURL: $serverURL \ninvalid url")
                return
            }

            if (serverURL.last() != '/') {
                serverURL += '/'
            }

            serverURL += "ocs/v2.php/apps/serverinfo/api/v1/info?format=json"

            requestNcStatus(serverURL,
                nc_username_input.text.toString(), nc_password_input.text.toString())
        }
    }

}