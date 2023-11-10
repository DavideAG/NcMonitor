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

import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection



const val Byte = 1024
var N_CORES = 4   /* number of cores in your server. 4 for RPi4. */
var CPU_LOAD = 0.0


class MainActivity : WearableActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()
        val extras = intent.extras
        if (extras != null)
            showResults(extras.getString("response")!!, extras.getString("serverURL")!!)

        pullToRefresh.setOnRefreshListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            pullToRefresh.isRefreshing = false
            finish()
        }

        // used to start the options activity
        setting_icon.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }

    /* Refresh the cpu_load_placeholder with the new N_CORES.
     * We need to handle the new number of CPU cores to compute the CPU load
     */
    override fun onRestart() {
        super.onRestart()
        cpu_load_placeholder.text = "%.2f".format((CPU_LOAD / N_CORES) * 100)
    }

    /* This method shows the results retrieved from
     * the server. View are populated correctly.
     * In case of error a message is displayed
     */
    private fun showResults(response: String, serverURL: String)
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

            if (statusCode == HttpURLConnection.HTTP_OK) {
                status_code_response_layout.visibility = View.GONE
                status_message_response_layout.visibility = View.GONE
                server_name_url.visibility = View.VISIBLE
                server_name_url.text = serverURL

                val dataObject = responseObject.getJSONObject("data")
                val nextcloudObject = dataObject.getJSONObject("nextcloud")
                val systemObject = nextcloudObject.getJSONObject("system")

                // cpu load
                CPU_LOAD = systemObject.getJSONArray("cpuload")[0] as Double
                //var cpuLoad = systemObject.getJSONArray("cpuload")[0]
                //if (CPU_LOAD is Int) CPU_LOAD = CPU_LOAD
                // ram
                val ramTotal = systemObject.getLong("mem_total")
                val ramFree = systemObject.getLong("mem_free")
                // swap
                val swapTotal = systemObject.getLong("swap_total")
                val swapFree = systemObject.getLong("swap_free")
                // disk
                val diskFree = systemObject.getLong("freespace")

                updateViews(ramTotal-ramFree, ramTotal,
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

    /* This method is used to format the disk usage
     */
    fun Double.format(digits: Int) = "%.${digits}f".format(this)

    /* This method is used to populate the views
     * using the information coming from the server.
     */
    private fun updateViews(
        ramBusy: Long,
        ramTotal: Long,
        swapBusy: Long,
        swapTotal: Long,
        diskFree: Long)
    {

        // Todo: to move as option and let this. We can't supposing the number of cpu cores
        //       and it has to be specified by the final user using a specific option menu.
        val cpuLoadPercentage = (CPU_LOAD / N_CORES) * 100

        cpu_load_placeholder.text =  "%.2f".format(cpuLoadPercentage)
        ram_used_placeholder.text = (ramBusy / Byte).toString()
        ram_total_placeholder.text = (ramTotal / Byte).toString()
        swap_used_placeholder.text = (swapBusy / Byte).toString()
        swap_total_placeholder.text = (swapTotal / Byte).toString()

        val p = humanReadableByteCountBin(diskFree)
        disk_free_placeholder.text = p.first
        disk_unit.text = " " + p.second
    }

    /* This function is used to convert the free disk
     * space in an human friendly measure
     */
    private fun humanReadableByteCountBin(bytes: Long): Pair<String, String> {
        return when {
            bytes == Long.MIN_VALUE || bytes < 0 -> Pair("N/A", "")
            bytes < 1024L -> Pair("$bytes", "B")
            bytes <= 0xfffccccccccccccL shr 40 -> Pair("%.1f".format(bytes.toDouble() / (0x1 shl 10)), "KiB")
            bytes <= 0xfffccccccccccccL shr 30 -> Pair("%.1f".format(bytes.toDouble() / (0x1 shl 20)), "MiB")
            bytes <= 0xfffccccccccccccL shr 20 -> Pair("%.1f".format(bytes.toDouble() / (0x1 shl 30)), "GiB")
            bytes <= 0xfffccccccccccccL shr 10 -> Pair("%.1f".format(bytes.toDouble() / (0x1 shl 40)), "TiB")
            bytes <= 0xfffccccccccccccL -> Pair("%.1f".format((bytes shr 10).toDouble() / (0x1 shl 40)), "PiB")
            else -> Pair("%.1f".format((bytes shr 20).toDouble() / (0x1 shl 40)), "EiB")
        }
    }
}
