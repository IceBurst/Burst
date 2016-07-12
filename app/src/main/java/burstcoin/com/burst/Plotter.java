package burstcoin.com.burst;

import java.io.IOException;

/**
 * Created by IceBurst on 7/11/2016.
 */
public class Plotter {
    /*static {
        System.loadLibrary("plot");
    }*/


    private IntProvider callback;
    private String numericID = "";

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

            // Sample ./plot32 -k 1232353462354235 -d /storage/sdcard1/Android/data/com.termux/ -s 0 -n 10000 -m 1024 -t 4
            // My Simple Plot 1 Thread, 256MB memory, 100MB Plot
            String mExe =  "/data/data/burstcoin.com.burst/lib/plot32.so -k "+numericID+" -d /storage/sdcard1/Android/data/com.termux/ -s 0 -n 400 -m 256 -t 1";
            try {
                Process process = Runtime.getRuntime().exec(mExe);
            } catch (IOException e) {

            }
            //DataOutputStream os = new DataOutputStream(process.getOutputStream());
            //DataInputStream osRes = new DataInputStream(process.getInputStream());



        }
    }
}
