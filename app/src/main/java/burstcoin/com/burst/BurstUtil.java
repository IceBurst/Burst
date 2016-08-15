package burstcoin.com.burst;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import static java.lang.Character.digit;

/**
 * Created by IceBurst on 7/6/2016.
 *
 * A Set of static methods to complete simple tasks by sending out to a utility class
 * instead of inlining a snipettes of code
 */

@TargetApi(18)
public class BurstUtil {
    static String TAG = "BurstTools";
    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");

    private IntProvider provider;

    public BurstUtil(IntProvider c) {
        this.provider = c;
    }

    public BurstUtil() {}

    // Create a Human readable Deadline from a provided BigInteger
    public static String BigIntToHumanReadableDate(BigInteger mBI) {
        String mDate = "";
        long mDL = mBI.longValue();

        final long SecInMin = 60;
        final long SecInHr = 3600;
        final long SecInDay = 86400;
        final long SecInMon = 2592000;      // Defined as 30 Days
        final long SecInYear = 31536000;    // defined as 365 days
        if(mDL > SecInYear) {
            long mYR = (mDL / SecInYear);
            mDate = String.valueOf(mYR) + " Years";
            mDL = mDL - (SecInYear * mYR);
        }
        if(mDL > SecInMon) {
            long mMon = (mDL / SecInMon);
            mDate += " " + String.valueOf(mMon) + " Months";
            mDL = mDL - (SecInMon * mMon);
        }
        if(mDL > SecInDay) {
            long mDay = (mDL / SecInDay);
            mDate += " " + String.valueOf(mDay) + " Days";
            mDL = mDL - (SecInDay * mDay);
        }
        if (mDL > SecInHr) {
            long mHR = (mDL / SecInHr);
            mDate += " " + String.valueOf(mHR) + " Hrs";
            mDL = mDL - (SecInHr * mHR);
        }
        if (mDL > SecInMin) {
            long mMin = (mDL / SecInMin);
            mDate += " " + String.valueOf(mMin) + " Mins";
            mDL = mDL - (SecInMin * mMin);
        }
        mDate += " " + mDL + " Sec";

        return mDate;
    }

    // Generate a Hex Equivilant from a String
    public static byte[] stringToBytes(String input) {
        int length = input.length();
        byte[] output = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            output[i / 2] = (byte) ((digit(input.charAt(i), 16) << 4) | digit(input.charAt(i+1), 16));
        }
        return output;
    }

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
        String[] mCards = getStorageDirectories();
        if (mCards.length == 0)         // added 12-July-2016, incase there is no valid cards
            return 0;

        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            // We can read and write the media
            //StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            StatFs stat = new StatFs(mCards[0]);
            long bytesAvailable = stat.getAvailableBytes();
            long megsAvailable = bytesAvailable / 1048576;
            DecimalFormat roundingFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
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

    public static String getPathToSD() {
        String mPath = "";
        String[] mCards = getStorageDirectories();
        if (mCards.length > 0) {
            mPath = mCards[0];
        }
        return mPath;
    }

    // ToDo: Testing 8-July on Hardware, return .51 <-- Is This correct for a simulated SD, thought it should be 2GB?
    // We are looking internal memory that is registering as External, need to fix this some how, we only want real SD cards
    // We Get these results with no card as well!!
    public static double getTotalSpaceInGB() {
        String state = Environment.getExternalStorageState();
        String[] mCards = getStorageDirectories();
        //File mStoragePath = Environment.getExternalStorageDirectory();
        // isExternalStorageRemovable comes back false, thats how we know this is the wrong memory space.
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) )
        {
            // We can read or write the media
            // This below is Bad, we need to write something more elegant
            for (String path : mCards) {
                Log.d(TAG, "Found Storage at:" + path);           // This is just a diagnostic to check for SD Card paths
            }
            Log.d(TAG, "EXTERNAL_STORAGE: "+System.getenv("EXTERNAL_STORAGE"));
            Log.d(TAG, "SECONDARY_STORAGE: "+System.getenv("SECONDARY_STORAGE") );

            if (mCards.length == 0)         // added 12-July-2016, in case there is no valid cards
                return 0;
            // ToDo: Need to setup StatFS better
            /* Test Results
             * Running 5.1 Nexus5X Emulated with 8GB SD, returned /storage/sdcard which was correct
             */
            StatFs stat = new StatFs(mCards[0]);
            long megsAvailable = stat.getTotalBytes() / 1048576;
            DecimalFormat roundingFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
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
                        provider.notice("GOTNUMERICID", "SUCCESS", numericID);
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

    public void getRewardIDFromNumericID(final String numericID, final Context context) {

        String URL = "http://mobile.burst-team.us:8125/burst?requestType=getRewardRecipient&account=" + numericID;
        Log.d(TAG, "GetRewardID:"+URL);
        GetAsync jsonCall = new GetAsync(URL) {
            @Override
            protected void onPostExecute (JSONObject json) {
                if (json != null) {
                    try {
                        String rewardID = json.getString("rewardRecipient");
                        provider.notice("GOTREWARDID", "SUCCESS", rewardID);
                    } catch (JSONException e) {
                        provider.notice("GOTREWARDID", "JSON EXCEPTION");
                    }
                } else {
                    provider.notice("GOTREWARDID", "JSON NULL");
                }
            }
        };
        jsonCall.execute();
    }

    public static void setRewardAssignment(final String mNumericID, final String mPassPhrase) {

        final String url = "https://wallet.burst-team.us:8125/burst";
        ///burst?requestType=setRewardRecipient&account=" + mNumericID + "&secretPhrase=" + mPassPhrase

        PostAsync jsonCall = new PostAsync() {
            @Override
            protected JSONObject doInBackground(String... args) {
                try {
                    String URL = args[0];
                    HashMap<String, String> params = new HashMap<>();
                    int mParmCt = args.length;
                    for (int i = 1; i < mParmCt; i=i+2) {
                        params.put(args[i], args[i+1]);
                    }

                    Log.d(TAG, "POST:" + URL);
                    for (Map.Entry<String, String> p : params.entrySet()) {
                        Log.d(TAG,"POST K->V:" +p.getKey() + "->" + p.getValue());
                    }

                    JSONObject json = jsonParser.makeHttpRequest(
                            URL, "POST", params);

                    if (json != null) {
                        Log.d(TAG, "PostAsync result"+ json.toString());
                        return json;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            /*
            @Override
            protected void onPostExecute (JSONObject json) {
                if (json != null) {
                    try {
                        String rewardID = json.getString("rewardRecipient");
                        provider.notice("GOTREWARDID", "SUCCESS", rewardID);
                    } catch (JSONException e) {
                        provider.notice("GOTREWARDID", "JSON EXCEPTION");
                    }
                } else {
                    provider.notice("GOTREWARDID", "JSON NULL");
                }
            }*/
        };
        jsonCall.execute(url, "requestType","setRewardRecipient","recipient",mNumericID, "secretPhrase", mPassPhrase, "deadline","1440","feeNQT","100000000");
        //jsonCall.execute(url, "requestType","setRewardRecipient","recipient",mNumericID, "secretPhrase", mPassPhrase );
    }

    // Get the Free Memory on the device
    public long getFreeMemoryInMB(Context c) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.availMem / 1048576L;
    }

    // Get total memory on the device less the kernel
    public long getTotalMemoryInMB(Context c) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.totalMem / 1048576L;
    }

    public static String[] getStorageDirectories()
    {
        // Final set of paths
        final Set<String> rv = new HashSet<String>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if(TextUtils.isEmpty(rawEmulatedStorageTarget))
        {
            // Device has physical external storage; use plain paths.
            if(TextUtils.isEmpty(rawExternalStorage))
            {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            }
            else
            {
                rv.add(rawExternalStorage);
            }
        }
        else
        {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                rawUserId = "";
            }
            else
            {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPORATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try
                {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                }
                catch(NumberFormatException ignored)
                {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if(TextUtils.isEmpty(rawUserId))
            {
                rv.add(rawEmulatedStorageTarget);
            }
            else
            {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if(!TextUtils.isEmpty(rawSecondaryStoragesStr))
        {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }

}
