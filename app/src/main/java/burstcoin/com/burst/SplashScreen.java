/**
 * Created by IceBurst on 3/16/2017.
 */

package burstcoin.com.burst;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import burstcoin.com.burst.tools.BurstContext;
import burstcoin.com.burst.tools.WalletTool;

public class SplashScreen extends Activity{
    final static String TAG = "SplashScreen";
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    private WalletTool mTheBestWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findBestWallet();
        BurstContext.setWallet(mTheBestWallet);
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void findBestWallet() {
        WalletTool mStaticWallets[] = {
                new WalletTool("wallet1.burstnation.com",8125),
                new WalletTool("wallet2.burstnation.com",8125),
                new WalletTool("wallet3.burstnation.com",8125),
                new WalletTool("wallet.burst-team.us")};

        mTheBestWallet = null;
        long h;
        long sp = 999999999;
        h = 0;

        for (WalletTool w : mStaticWallets ) {
            w.GetHeight();
            if (w.Height > h) {
                h = w.Height;
                mTheBestWallet = w;
                Log.d(TAG,"Set New Wallet based on new Height");
            }
            // if (w.Height == h && w.GetSpeed() < sp && w.GetSpeed() != 0) {
            if (w.Height == h && w.GetSpeed() < sp ) {
                sp = w.GetSpeed();
                mTheBestWallet = w;
                Log.d(TAG,"Set New Wallet based on Speed");
            }
            Log.d(TAG, "Checking Wallet " + w.getURL() + " height:" + h + " speed was:" + w.GetSpeed()); // Add URL, add Speed result, we want the lowest speed number
        }
    }
}
