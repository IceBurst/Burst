package burstcoin.com.burst;

import org.json.JSONObject;

/**
 * Created by tim on 7/8/2016.
 */
public interface JSONCaller {
    void returnedJSON(JSONObject jsonObject, String function);
}
