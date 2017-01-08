package burstcoin.com.burst;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class PostAsync extends AsyncTask<String, String, JSONObject> {
    JSONParser jsonParser = new JSONParser();

    final static String TAG = "PostAsync";
    private String URL = "";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onPreExecute() {

    }

    public
    PostAsync() {
        super();
    }

    public PostAsync(String u) {
        super();
        this.URL = u;
    }

    @Override
    protected JSONObject doInBackground(String... args) {
        try {
            HashMap<String, String> params = new HashMap<>();
            URL = args[0];
            int mParmCt = args.length;
            for (int i = 1; i < mParmCt; i=i+2) {
                params.put(args[i], args[i+1]);
            }

            Log.d(TAG, "POST:" + URL);

            JSONObject json = jsonParser.makeHttpRequest(
                    URL, "POST", params);

            if (json != null) {
                //Log.d("JSON result", json.toString());
                return json;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(JSONObject json) {
        // This should all be overridden
        int success = 0;
        String message = "";

        if (json != null) {
            //Toast.makeText(MainActivity.this, json.toString(),         Toast.LENGTH_LONG).show();

            /*
            try {
                //success = json.getInt(TAG_SUCCESS);
                //message = json.getString(TAG_MESSAGE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            */
        }
        /*
        if (success == 1) {
            //Log.d("Success!", message);
        }else{
            //Log.d("Failure", message);
        }
        */
    }

}