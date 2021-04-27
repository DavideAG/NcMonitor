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
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.android.synthetic.main.activity_settings.*


const val VERSION = BuildConfig.VERSION_NAME
const val AUTHOR_WEBSITE = "http://giorgiodavide.it"


class Settings : WearableActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        version_number_placeholder.text = VERSION
        creator_webpage.setOnClickListener{ launchBrowserOnPhone() }
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

        // launch the browser in the phone
        val intentBrowser = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse(AUTHOR_WEBSITE))

        RemoteIntent.startRemoteActivity(this, intentBrowser, null)
    }
}