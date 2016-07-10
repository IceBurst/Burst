package burstcoin.com.burst;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by IceBurst on 7/9/2016.
 */
public class JSInterface {

    private final String TAG = "JSInterface";
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void processHTML(String html) {
        Log.d(TAG, html);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void getBurstID(String id) {
        Log.d(TAG, "burstID:"+id);
        //return id;
    }
}
