package burstcoin.com.burst.burstcoin.com.burst.plotting;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import burstcoin.com.burst.BurstUtil;

/**
 * Created by tim on 7/19/2016.
 */
public class PlotFile {

    public static int NonceToComplete = 64;  // This will have to be 4096 in the end

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

    public void setNumericID(String numericID) { mNumericID = numericID; }
    public void setStartNonce(long start) {mStart = start;}

    public void plot() {
        // This needs to be Threaded out so we can cancel it....
        Long mNonce = mStart;
        FileOutputStream out;

        mCallback.notice("PLOTTING", "NONCE", "0");
        // Do the plot loop in here
        mFileName = mNumericID + '_' + Long.toString(mStart) + '_' + Long.toString(new Long(NonceToComplete)+mStart) + '_' + Long.toString(mStgr);
        String mPlotFile = BurstUtil.getPathToSD() + '/' + mFileName;
        try {
            // We need the full path here
            Log.d(TAG, "Writing to:" + mPlotFile);
            out = new FileOutputStream(mPlotFile); // mFileName
        } catch (IOException ioex) {
            // We should put an error here if osmething bad happens
            return;
        }
        int staggeramt = 1; // Fix to get us through this with out changing code
        byte[] outputbuffer = new byte[(int) (staggeramt * SinglePlot.PLOT_SIZE)]; // <-- this is 1 nonce
        // java.lang.ArrayIndexOutOfBoundsException: src.length=262144 srcPos=262080 dst.length=262144 dstPos=262144 length=64
        /* this crashes with the following data
07-20 17:54:56.829 2856-2856/burstcoin.com.burst D/PlotFile: 0: This is iteration #:4095
07-20 17:54:56.829 2856-2856/burstcoin.com.burst D/PlotFile: 1: plot.data.length is:262144
07-20 17:54:56.829 2856-2856/burstcoin.com.burst D/PlotFile: 2: starting copy from 262080
07-20 17:54:56.829 2856-2856/burstcoin.com.burst D/PlotFile: 3: outputbuffer has a size of: 262144
07-20 17:54:56.829 2856-2856/burstcoin.com.burst D/PlotFile: 4: Starting to copy at 262144 of the output buffer we are starting at the last element of course this fails
07-20 17:54:56.829 2856-2856/burstcoin.com.burst D/PlotFile: 5: Trying to Copy 64 bytes

         */

        for (int mWorkingNonce = 0;mWorkingNonce < NonceToComplete ;mWorkingNonce++){   // This will need to be 4096
            // I get it they are biuding the buffer up to the stagger size before writing, this is more effecient but we dont care

            SinglePlot plot = new SinglePlot(address, mNonce);
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

            /* complete success
            root@generic_x86_64:/storage/sdcard # ls -l
                    -rwxrwx--- root     sdcard_r 33554432 2016-07-20 18:04 14041956000257417609_0_128_1
            */

            try {
                //out.write(outputbuffer);
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
}
