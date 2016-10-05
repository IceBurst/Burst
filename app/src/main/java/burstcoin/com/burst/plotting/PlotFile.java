package burstcoin.com.burst.plotting;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import burstcoin.com.burst.BurstUtil;

/**
 * Created by tim on 7/19/2016.
 */
public class PlotFile {

    public static int NonceToComplete = 4096;  // This will have to be 4096 in the end

    private IntPlotStatus mCallback;
    private String mFileName;       // Complete File Name
    private String mNumericID;      // User numeric IC
    private long mStart;            // Starting Nonce
    private long mSz;               // Size in Nonce
    private long mStgr = 1;             // Stagger Size plotted with
    private boolean isPlotted = false;  // Default to not plotted
    private long address;           // Long version of numericID

    private static String TAG = "PlotFile";

    public PlotFile(IntPlotStatus cb) {
        mCallback = cb;
    }

    public PlotFile(String fName, int size) {
        mSz = size;
        mFileName = fName;
        isPlotted = true;
        String[] mParts = fName.split("_");
        mNumericID = mParts[0];
        address = parseUnsignedLong(mParts[0], 10);
        mStart = Long.parseLong(mParts[1]);
        mSz = Long.parseLong(mParts[2]);
        mStgr = Long.parseLong(mParts[3]);
        // Do some math to check that size is = to nonce * nonce size or error
    }

    public void setNumericID(String numericID) {
        mNumericID = numericID;
        // Run through a Bigint incase the 2^64 is greated than signed Long
        BigInteger bigNumericID = new BigInteger(numericID);
        address = bigNumericID.longValue();
    }

    public void setStartNonce(long start) {mStart = start;}
    public String getFileName() {
        return this.mFileName;
    }

    public void plot() throws IOException {
        // This needs to be Threaded out so we can cancel it....
        Long mNonce = mStart;
        FileOutputStream out;

        mCallback.notice("PLOTTING", "NONCE", "0");
        mFileName = mNumericID + '_' + Long.toString(mStart) + '_' + Long.toString(new Long(NonceToComplete)) + '_' + Long.toString(mStgr);
        String mPlotFile = BurstUtil.getPathToSD() + '/' + mFileName;
        try {
            Log.d(TAG, "Writing to:" + mPlotFile);
            out = new FileOutputStream(mPlotFile);
            //out = openFileOutput(mPlotFile, Context.MODE_WORLD_WRITEABLE);

        } catch (IOException ioex) {
            //Clear the plotting Window
            mCallback.notice("PLOTTING", "NONCE", Integer.toString(NonceToComplete));
            throw ioex;
        }
        int staggeramt = 1;     // We are going to plot in simple 1NONCE, 256K chunks
        try {
            byte[] outputbuffer = new byte[(int) (staggeramt * SinglePlot.PLOT_SIZE)];              // <-- this is 1 nonce in a byte[]
            for (int mWorkingNonce = 0; mWorkingNonce < NonceToComplete; mWorkingNonce++) {
                SinglePlot plot = new SinglePlot(address, mStart + mWorkingNonce);
                Log.d(TAG, "Plotting Nonce #:" + mWorkingNonce + " of " + NonceToComplete);
            /*
            for(int i = 0; i < SinglePlot.SCOOPS_PER_PLOT; i++) { // through 4096 crashing on the last entry
                Log.d (TAG,"0: This is iteration #:" + i);
                Log.d (TAG,"1: plot.data.length is:" + plot.data.length);
                Log.d (TAG,"2: starting copy from " + Long.toString(i * SinglePlot.SCOOP_SIZE) );
                Log.d (TAG,"3: outputbuffer has a size of: " + outputbuffer.length);
                Log.d (TAG,"4: Starting to copy at " + Integer.toString((int) ((i * SinglePlot.SCOOP_SIZE * staggeramt) + (1 * SinglePlot.SCOOP_SIZE))) + " of the output buffer");
                Log.d (TAG,"5: Trying to Copy "+ SinglePlot.SCOOP_SIZE + " bytes");
            }*/

                try {
                    out.write(plot.data);
                    out.flush();
                } catch (IOException ioex) {
                    Log.e(TAG, "IOException writing to" + mPlotFile);
                    mCallback.notice("TOAST", "FAILED TO WRITE TO STORAGE");
                    return;
                }
                mCallback.notice("PLOTTING", "NONCE", Integer.toString(mWorkingNonce));
            }
        } catch (Exception e) {
            mCallback.notice("TOAST", "FAILED TO GENERATE PLOT");
        }

        try{
            out.close();
        }catch(
            IOException ioex){return;
        }
        mCallback.notice("PLOTTING", "NONCE", Integer.toString(NonceToComplete));
    }
    public static long parseUnsignedLong(String s, int radix)
            throws NumberFormatException {
        BigInteger b= new BigInteger(s,radix);
        if(b.bitLength()>64)
            throw new NumberFormatException(s+" is to big!");
        return b.longValue();
    }

    public long getStaggeramt() {
        return 1;
    }

    public long getStartnonce() {
        return mStart;
    }

    public long getAddress () {return address; }
}
