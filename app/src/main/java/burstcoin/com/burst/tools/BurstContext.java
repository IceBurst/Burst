package burstcoin.com.burst.tools;

import android.app.Application;
import android.content.Context;

/**
 * Created by IceBurst on 8/20/2016.
 */
public class BurstContext extends Application {
    private static Context mContext;
    private static WalletTool mWallet;

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return  mContext;
    }

    public static void setWallet(WalletTool w) {
        mWallet = w;
    }

    public static WalletTool getWallet() {
        return mWallet;
    }
}
