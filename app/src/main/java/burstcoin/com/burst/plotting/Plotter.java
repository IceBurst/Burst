package burstcoin.com.burst.plotting;

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

    public void plot1GB() {
        PlotFile mNewPlot = new PlotFile(callback);
        mNewPlot.setNumericID(mNumericID);
        mNewPlot.setStartNonce(mPlotFiles.size()*PlotFile.NonceToComplete);
        mNewPlot.plot();
    }

    public void plotGBs(int mGBs) {
        int mStartingGB = mPlotFiles.size();
        for (int i = 0; i<mGBs; i++) {
            PlotFile mNewPlot = new PlotFile(callback);
            mNewPlot.setNumericID(mNumericID);
            mNewPlot.setStartNonce((mStartingGB+i) * PlotFile.NonceToComplete);
            mNewPlot.plot();
        }
    }
}
