package burstcoin.com.burst;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by IceBurst on 7/9/2016.
 */
public class JSInterface {

    private final String TAG = "JSInterface";
    private IntProvider mProvider;

    public JSInterface(IntProvider p) {
        this.mProvider = p;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void processHTML(String html) {
        // Don't use this in production, this is just used to dump code
        Log.d(TAG, html);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void getBurstID(String id) {
        //Log.d(TAG, "burstID:"+id);
        mProvider.notice("GOTBURSTID", "SUCCESS", id);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void getPassPhrase(String id) {
        Log.d(TAG, "PassPhrase:"+id);
        mProvider.notice("PASSPHRASE", id);
    }
}
