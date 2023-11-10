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
import android.net.Uri
import android.os.Bundle
import android.support.wearable.activity.ConfirmationActivity
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.android.synthetic.main.activity_main.cpu_load_placeholder
import kotlinx.android.synthetic.main.activity_settings.*


const val VERSION = BuildConfig.VERSION_NAME
const val AUTHOR_WEBSITE = "http://giorgiodavide.it"
val CPU_CORES = (1..32).toList()


class Settings : WearableActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        version_number_placeholder.text = VERSION
        cpuSpinnerInit()
        creator_webpage.setOnClickListener{ launchBrowserOnPhone() }
    }

    /* This method will configure the CPU spinner
     */
    private fun cpuSpinnerInit()
    {
        // set the elements of the spinner (CPU_CORES
        val adapter: ArrayAdapter<Int> = ArrayAdapter<Int>(this, android.R.layout.simple_spinner_item, CPU_CORES)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cpu_spinner.adapter = adapter

        // configure the selected N_CORES
        val selectedItemIndex = CPU_CORES.indexOf(N_CORES)
        cpu_spinner.setSelection(selectedItemIndex)

        // handle the new N_CORES value
        cpu_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                N_CORES = CPU_CORES[position]
                Log.d("selected cpu cores", "$N_CORES")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /* This method will launch the phone browser
     */
    private fun launchBrowserOnPhone()
    {
        // start the "Open on Phone" animation
        val intentAnimation = Intent(this, ConfirmationActivity::class.java)
        intentAnimation.putExtra(
            ConfirmationActivity.EXTRA_ANIMATION_TYPE,
            ConfirmationActivity.OPEN_ON_PHONE_ANIMATION
        )
        startActivity(intentAnimation)

        // launch the phone browser
        val intentBrowser = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse(AUTHOR_WEBSITE))

        RemoteIntent.startRemoteActivity(this, intentBrowser, null)
    }
}