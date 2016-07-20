package burstcoin.com.burst.burstcoin.com.burst.plotting;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import burstcoin.com.burst.BurstUtil;
import burstcoin.com.burst.IntProvider;

/**
 * Created by IceBurst on 7/11/2016.
 */

public class Plotter {

    private IntProvider callback;
    private String numericID = "";

    private final static String TAG = "Plotter";

    public Plotter (IntProvider cb, String nID) {
        this.numericID = nID;
        this.callback = cb;
    }

    public void plot1GB() {

        callback.notice("PLOTTER", "SUCCESS", "1GB PLOT CREATED");
    }

    public void brutalTestPlot() {

        String[] mCards = BurstUtil.getStorageDirectories();
        if (mCards.length > 0) {
            // /data/data/your.package.full.name/lib, with executable permissions.

            /*    This is the bbad way calling an external process
            // Sample ./plot32 -k 1232353462354235 -d /storage/sdcard1/Android/data/com.termux/ -s 0 -n 10000 -m 1024 -t 4
            // My Simple Plot 1 Thread, 256MB memory, 100MB Plot
            // ToDo: This will crash with the hard coded path of -d parm
            String mExe =  "/data/data/burstcoin.com.burst/lib/libplot32.so -k "+numericID+" -d "+mCards[0]+" -s 0 -n 400 -m 256 -t 1";
            Log.d(TAG, "Going to Try:"+mExe);
            try {
                Process process = Runtime.getRuntime().exec(mExe);
                // /data/data/burstcoin.com.burst/lib/plot32.so -k 6335396967509650518 -d /storage/sdcard -s 0 -n 400 -m 256 -t 1
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                DataInputStream osRes = new DataInputStream(process.getInputStream());
                //Log.d(TAG,"Exit Value:" + Integer.toString(process.exitValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //DataOutputStream os = new DataOutputStream(process.getOutputStream());
            //DataInputStream osRes = new DataInputStream(process.getInputStream());
            */
        }
    }
}
