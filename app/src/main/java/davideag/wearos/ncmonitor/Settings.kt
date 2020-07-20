package davideag.wearos.ncmonitor

import android.os.Bundle
import android.support.wearable.activity.WearableActivity

class Settings : WearableActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }
}