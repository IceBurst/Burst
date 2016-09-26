package burstcoin.com.burst.tools;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by IceBurst on 8/20/2016.
 */

// Used for retrieving the status of power to the device
public class PowerTool {
    final static String TAG = "PowerTool";

    public static boolean isOnPower() {
        Context context = BurstContext.getAppContext();
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (acCharge || usbCharge) {
            Log.d(TAG, "Device is charging");
            return true;
        }
        Log.d(TAG, "Device is not charging");
        return false;
    }
}
