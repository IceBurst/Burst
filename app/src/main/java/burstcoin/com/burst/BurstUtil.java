package burstcoin.com.burst;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by IceBurst on 7/6/2016.
 *
 * A Set of static methods to complete simple tasks by sending out to a utility class
 * instead of inlining a snipettes of code
 */

@TargetApi(18)
public class BurstUtil {
    static String TAG = "BurstTools";
    private IntProvider provider;

    public BurstUtil(IntProvider c) {
        this.provider = c;
    }
    // ToDo: Requires Testing
    // Could be very useful for setting up plot sizes
    // Query and return the amount of freespace on the SD card in nounces
    public static long getFreeSpaceInNounces() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getAvailableBytes();
        return bytesAvailable / (long)262144;
    }

    // ToDo: Test on 2nd live device I get .37 on my emulated device
    // Return Free space in GB to show on Plotting Screen
    public static double getFreeSpaceInGB() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            // We can read and write the media
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long bytesAvailable = (long)stat.getAvailableBytes();
            long megsAvailable = bytesAvailable / 1048576;
            DecimalFormat roundingFormat = new DecimalFormat("#.##");
            roundingFormat.setRoundingMode(RoundingMode.DOWN);
            return Double.parseDouble(roundingFormat.format(((double)megsAvailable / (double)1024)));
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            // We can only read the media so treat as none free
            return 0;
        }
        else  // No external media
        {
            return 0;
        }
    }

    // ToDo: Testing 8-July on Hardware, return .51 <-- Is This correct for a simulated SD, thought it should be 2GB?
    // We Get these results with no card as well!!
    public static double getTotalSpaceInGB() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) )
        {
            // We can read or write the media
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long megsAvailable = (long)stat.getTotalBytes() / 1048576;
            DecimalFormat roundingFormat = new DecimalFormat("#.##");
            roundingFormat.setRoundingMode(RoundingMode.DOWN);
            return Double.parseDouble(roundingFormat.format(((double)megsAvailable / (double)1024)));
        }
        else
        {
            // No external media
            return 0;
        }
    }

    // Get the NumericID from the Internal Memory of the Device
    public static String getNumericIDFromLocal(String burstID, Context context) {
        try {
            String fileName = burstID + ".nid";
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String numericID = bufferedReader.readLine();
            bufferedReader.close();
            isr.close();
            return numericID;
        }
        // IOException is thrown when file DNE, return empty set
        catch (IOException e) {
            return "";
        }
    }

    // Write the NumericID to the internal storage
    public static void writeNumericIDToLocal(String burstID, String numericID, Context context) {
        try {
            String fileName = burstID + ".nid";
            FileOutputStream fos  = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            String outputline = numericID + '\n';
            fos.write(outputline.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get Burst NumericID from Online API
    public void getNumericIDFromBurstID(final String burstID, final Context context) {

        String URL = "https://wallet.burst-team.us:8125/burst?requestType=rsConvert&account=" + burstID;

        GetAsync jsonCall = new GetAsync(URL) {
            @Override
            protected void onPostExecute (JSONObject json) {
                if (json != null) {
                    try {
                        String numericID = json.getString("account");
                        writeNumericIDToLocal(burstID, numericID, context);
                        provider.notice("GOTNUMERICID", "SUCCESS");
                    } catch (JSONException e) {
                        provider.notice("GOTNUMERICID", "JSON EXCEPTION");
                    }
                } else {
                    provider.notice("GOTNUMERICID", "JSON NULL");
                }
            }
        };
        jsonCall.execute(burstID);
    }
}
