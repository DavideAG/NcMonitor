/*
 * NcMonitor WearOS application
 *
 * @author Davide Antonino Giorgio
 * Copyright (C) 2020 Davide Antonino Giorgio
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package davideag.wearos.ncmonitor

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials.basic
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection



const val SERVERINFO_API = "ocs/v2.php/apps/serverinfo/api/v1/info?format=json"
const val PERMISSION_REQUEST = 10
const val PREF_KEY = "profile"
const val PREF_NAME = "USER"


class LoginActivity : WearableActivity()
{
    private var client = OkHttpClient()
    private var request = OkHttpRequest(client)


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Enables Always-on
        setAmbientEnabled()

        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val userInstance = sharedPref.getString(PREF_KEY, null)

        // hiding the password using dots
        nc_password_input.transformationMethod = PasswordTransformationMethod.getInstance()

        if (userInstance != null) {
            //There is an instance of a user on the watch, retrieve data
            val userJson = JSONObject(userInstance)
            requestNcStatus(
                userJson.get("serverURL").toString(),
                userJson.get("username").toString(),
                userJson.get("password").toString(),
                userJson.get("domain").toString(),
                this
            )
        }

        checkAndRequestPermissions()
        btn_next.setOnClickListener { onNextClicked() }
    }

    /* This method is used to request permissions
     */
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
                requestPermissions(permissionsDenied.toTypedArray(),
                    PERMISSION_REQUEST
                )
        }
    }

    /* This method shows the progress bar during the request phase
     * and hide the login information
     */
    private fun showProgressBar()
    {
        runOnUiThread {
            login_scrollview_layout.visibility = View.GONE
            loading_layout.visibility = View.VISIBLE
        }
    }

    /* This method shows the login information
     * and hide the progress bar
     */
    private fun showLoginFields()
    {
        runOnUiThread {
            login_scrollview_layout.visibility = View.VISIBLE
            loading_layout.visibility = View.GONE
        }
    }

    /* This method is used to navigate to the result activity
     * when the response comes from your NC server
     */
    private fun navigateToResults(responseData: String, domain :String = "ServerDomain")
    {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("response", responseData)
        intent.putExtra("serverURL", domain)
        startActivity(intent)
    }

    /* This method is used to request the server status to your
     * NC instance. Then, if the return code is HttpURLConnection.HTTP_OK
     * results are passed to the next activity.
     */
    private fun requestNcStatus(serverURL: String, username: String, password: String, domain: String, context: Context)
    {
        showProgressBar()
        val credentials = basic(username, password)

        request.GET(credentials, serverURL, object: Callback {
            override fun onResponse(call: Call?, response: Response) {

                val responseData = response.body()?.string()
                try {
                    val json = JSONObject(responseData!!)
                    val responseObject = json.getJSONObject("ocs")
                    val metaObject = responseObject.getJSONObject("meta")
                    val statusCode = metaObject.getInt("statuscode")

                    if (statusCode == HttpURLConnection.HTTP_OK) {
                        val userInfo = JSONObject()
                        userInfo.put("serverURL", serverURL)
                        userInfo.put("username", username)
                        userInfo.put("password", password)
                        userInfo.put("domain", domain)

                        val sharedPref =
                            getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString(PREF_KEY, userInfo.toString())
                            apply()
                        }

                        // navigate to the next activity
                        navigateToResults(responseData, domain)
                        // now destroying the current activity
                        finish()
                    } else {
                        showLoginFields()
                        // navigate to the next activity
                        navigateToResults(responseData)
                    }
                } catch (e: JSONException) {
                    Log.e("requestNcStatus","JsonException!!")
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(
                            context,
                            "Check the URL, maybe it's not a NC instance", Toast.LENGTH_SHORT
                        ).show()
                    }
                    showLoginFields()
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                showLoginFields()
                if (e != null) {
                    runOnUiThread {
                        Toast.makeText(
                            context,
                            "Error during the host resolution, check the URL or your internet connectivity", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    /* This method is used to take the input information from the
     * user and build the correct request parameters.
     */
    private fun onNextClicked()
    {
        if (nc_server_input.text.isNotEmpty() &&
            nc_username_input.text.isNotEmpty() &&
            nc_password_input.text.isNotEmpty()) {

            var serverURL = attachProtocol()
            serverURL += nc_server_input.text.toString()

            if (!URLUtil.isValidUrl(serverURL)) { //invalid url
                Log.d("inputs","ServerURL: $serverURL \ninvalid url")
                return
            }
            if (serverURL.last() != '/') {
                serverURL += '/'
            }
            serverURL += SERVERINFO_API
            requestNcStatus(serverURL,
                nc_username_input.text.toString(),
                nc_password_input.text.toString(),
                nc_server_input.text.toString(),
                this)
        } else {
            Toast.makeText(this,
                "All fields are mandatory", Toast.LENGTH_SHORT).show()
        }
    }

    /* This method is used to take the input information regarding
     * the protocol to use (http or https) from the radio buttons.
     */
    private fun attachProtocol(): String
    {
        return if (radio_button_http.isChecked) {
            "http://"
        } else {
            "https://"
        }
    }

}