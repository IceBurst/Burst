package burstcoin.com.burst.plotting;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import burstcoin.com.burst.BurstUtil;
import burstcoin.com.burst.IntProvider;
import burstcoin.com.burst.tools.BurstContext;
import burstcoin.com.burst.tools.PowerTool;

/**
 * Created by IceBurst on 7/11/2016.
 */

public class Plotter {

    private IntPlotStatus callback;
    private String mNumericID = "";
    private PlotFiles mPlotFiles;
    private String mPath = "";

    private final static String TAG = "Plotter";

    public Plotter (IntPlotStatus cb, String nID) {
        this.mNumericID = nID;
        this.callback = cb;
        mPath = BurstUtil.getPathToSD();
        mPlotFiles = new PlotFiles(mPath, mNumericID);
        Log.d(TAG, "Plotter Inited");
    }

    public Plotter (String nID) {
        this.mNumericID = nID;
        mPath = BurstUtil.getPathToSD();
        mPlotFiles = new PlotFiles(mPath, mNumericID);
        Log.d(TAG, "Plotter Inited without Callback");
    }

    public void reload() {
        mPlotFiles.rescan();
    }

    public int getPlotSize() {
        return mPlotFiles.size();
    }

    public void delete1GB() {
        mPlotFiles.deletePlot();
    }

    /* Old Legacy before we allowed multi GB of plotting in sequence
    public void plot1GB() {
        PlotFile mNewPlot = new PlotFile(callback);
        mNewPlot.setNumericID(mNumericID);
        mNewPlot.setStartNonce(mPlotFiles.size()*PlotFile.NonceToComplete);
        mNewPlot.plot();
    }  */

    public void plotGBs(int mGBs) {
        int mStartingGB = mPlotFiles.size();

        try {
            /*
            boolean mOnPower = PowerTool.isOnPower();
            boolean mCPULockedOn = false;
            PowerManager powerManager = (PowerManager) BurstContext.getAppContext().getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyWakelockTag");

            if (mOnPower) {
                wakeLock.acquire();
                mCPULockedOn = true;
            }
            */

            for (int i = 0; i<mGBs; i++) {
                try {
                    PlotFile mNewPlot = new PlotFile(callback);
                    mNewPlot.setNumericID(mNumericID);
                    mNewPlot.setStartNonce((mStartingGB + i) * PlotFile.NonceToComplete);
                    mNewPlot.plot();
                } catch (IOException e) {
                    callback.notice("TOAST", "Error: IOException Plotting");
                    Log.e(TAG,"STACK TRACE:", e);
                    break;
                }
                /*
                if (PowerTool.isOnPower() == false) {       // after each GB check to see if were plugged in
                    wakeLock.release();
                    mCPULockedOn = false;
                }*/
            }
            /*
            if (mCPULockedOn) {
                wakeLock.release();
            }
            */
        } catch (Exception e) {
            callback.notice("TOAST", "ERROR: Require access to power management");
        }
    }
}
