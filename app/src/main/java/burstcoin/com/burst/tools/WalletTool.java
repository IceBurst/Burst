package burstcoin.com.burst.tools;

import android.app.Application;
import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import burstcoin.com.burst.GetAsync;

/**
 * Created by IceBurst on 3/11/2017.
 *
 * The WalletTool is used to find if a given wallet is forked or current
 * The WalletTool is used to get speed responses for wallets to help get the best miner
 *
 */
public class WalletTool {

    // Get Block Height of a given wallet
    // https://wallet1.burstnation.com:8125/burst?requestType=getMiningInfo

    // Get Peers
    // https://wallet1.burstnation.com:8125/burst?requestType=getPeers

    final static String TAG = "WalletTool";

    private String mWalletURL;
    private int mWalletPort;
    public long Height = 0;

    private long mStartTime;
    private long mSpeed = 0;

    public WalletTool(String url) {
        super();
        mWalletURL = url;
        mWalletPort = 80;
    }

    public WalletTool(String url, int port) {
        super();
        mWalletURL = url;
        mWalletPort = port;
    }

    public String getURL() {
        return mWalletURL + ":" + Integer.toString(mWalletPort);
    }

    public long GetSpeed() {
        return mSpeed;
    }

    public long GetHeight() {
        checkHeight();
        return Height;
    }

    // Internal use functions
    private void checkHeight() {
        //https://wallet1.burstnation.com:8125/burst?requestType=getMiningInfo
        //mStartTime = System.nanoTime();
        mStartTime = System.currentTimeMillis();
        String mURL = "https://"+mWalletURL+":"+Integer.toString(mWalletPort)+"/burst?requestType=getMiningInfo";
        Log.d(TAG, "Starting Height Check for: " + mURL);

        GetAsync jsonCall = new GetAsync(mURL) {
            @Override
            protected void onPostExecute(JSONObject json) {
                Log.d(TAG, "PostExecute for checkHeight");
                // This log line is never happening
                //long endTime = System.nanoTime();
                long endTime = System.currentTimeMillis();
                mSpeed = (endTime - mStartTime);
                try {
                    Height = Long.parseLong(json.getString("height"));
                }
                catch (Exception e) {
                    Log.e(TAG,"Failed getting Height");
                }
            }
        };
        try {
            String filler = "aaaa";
            JSONObject results = jsonCall.execute(filler).get();
            Height = Long.parseLong(results.getString("height"));
            long endTime = System.nanoTime();
            mSpeed = (endTime - mStartTime);
        }
        catch (Exception e) {
            Log.e(TAG,"Generic Exception Caught");
        }
    }
}
