package burstcoin.com.burst.plotting;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import burstcoin.com.burst.BurstUtil;

/**
 * Created by tim on 7/19/2016.
 */
public class PlotFile {

    public static int NonceToComplete = 1; //4096;  // This will have to be 4096 in the end

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
        address = Long.valueOf(numericID);
    }

    public void setStartNonce(long start) {mStart = start;}
    public String getFileName() {
        return this.mFileName;
    }

    public void plot() {
        // This needs to be Threaded out so we can cancel it....
        Long mNonce = mStart;
        FileOutputStream out;

        mCallback.notice("PLOTTING", "NONCE", "0");
        mFileName = mNumericID + '_' + Long.toString(mStart) + '_' + Long.toString(new Long(NonceToComplete)+mStart) + '_' + Long.toString(mStgr);
        String mPlotFile = BurstUtil.getPathToSD() + '/' + mFileName;
        try {
            Log.d(TAG, "Writing to:" + mPlotFile);
            out = new FileOutputStream(mPlotFile);
        } catch (IOException ioex) {

            return;
        }
        int staggeramt = 1;     // We are going to plot in simple 1NONCE, 256K chunks
        byte[] outputbuffer = new byte[(int) (staggeramt * SinglePlot.PLOT_SIZE)];              // <-- this is 1 nonce in a byte[]
        for (int mWorkingNonce = 0;mWorkingNonce < NonceToComplete ;mWorkingNonce++){
            SinglePlot plot = new SinglePlot(address, mWorkingNonce);
            Log.d(TAG, "Plotting Nonce #:" + mWorkingNonce + " of " + NonceToComplete);
            // Need to understand this a little better
            // why do we iterate through?  Why not just copy the whole block in?
            /*
            for(int i = 0; i < SinglePlot.SCOOPS_PER_PLOT; i++) { // through 4096 crashing on the last entry
                Log.d (TAG,"0: This is iteration #:" + i);
                Log.d (TAG,"1: plot.data.length is:" + plot.data.length);
                Log.d (TAG,"2: starting copy from " + Long.toString(i * SinglePlot.SCOOP_SIZE) );
                Log.d (TAG,"3: outputbuffer has a size of: " + outputbuffer.length);
                Log.d (TAG,"4: Starting to copy at " + Integer.toString((int) ((i * SinglePlot.SCOOP_SIZE * staggeramt) + (1 * SinglePlot.SCOOP_SIZE))) + " of the output buffer");
                Log.d (TAG,"5: Trying to Copy "+ SinglePlot.SCOOP_SIZE + " bytes");
                System.arraycopy(plot.data,                         // Source Array - should be one Nonce, let put some debugging in this Mo
                        i * SinglePlot.SCOOP_SIZE,                  // Starting position in the source array
                        outputbuffer,                               // destination array
                        (int) ((i * SinglePlot.SCOOP_SIZE * staggeramt) + (1 * SinglePlot.SCOOP_SIZE)), // Starting Position in the Destination Array
                        SinglePlot.SCOOP_SIZE);                     // length of bytes to copy
                //java.lang.ArrayIndexOutOfBoundsException: src.length=262144 srcPos=262080 dst.length=262144 dstPos=262144 length=64
            }*/

            try {

                out.write(plot.data);
                out.flush();
            } catch (IOException ioex) {
                Log.e(TAG,"IOException writing to"+mPlotFile);
                return;
            }
            mCallback.notice("PLOTTING", "NONCE", Integer.toString(mWorkingNonce));
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
