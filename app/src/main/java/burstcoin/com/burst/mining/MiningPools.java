package burstcoin.com.burst.mining;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import burstcoin.com.burst.BurstUtil;
import burstcoin.com.burst.GetAsync;

/**
 * Created by IceBurst on 8/16/2016.
 * This is a complete Mess and will be addresses in v2.1
 * We are working on a methodology to Link Pool Url to Wallet Payment Address
 * NXT API does not have any mechanism
 *
 * We can scrape the id field from the pool page or ask for a custom tag
 */
public class MiningPools {

    ArrayList<MiningPool> mMiningPools;
    static final String TAG = "MiningPools";

    public MiningPools() {
        mMiningPools = new ArrayList<MiningPool>();
    }

    public void loadMiningPools() {
        mMiningPools = null;
        getPoolList();

        //mMiningPools.put("pool.burst-team.us", "BURST-32TT-TSAC-HTKW-CC26C");
        // Add more pools just like above

        /*
        Pool.Burstcoin.de / pool.Burstcoin.uk
        Recipient: BURST-GHTV-7ZP3-DY4B-FPBFA
        URL: http://pool.burstcoin.de (Port 8080 )
        */
    }

    private void getPoolList() {
        String URL = "http://util.burst-team.us:8888/network/json";
        Log.d(TAG, "Servers on Network:"+URL);

        GetAsync jsonCall = new GetAsync(URL) {
            @Override
            protected void onPostExecute (JSONObject json) {
                // we are getting null back because it is an array of JSON and it can't handle it

                if (this.jsonArray != null) {
                    try {
                        for (int i=0;i< this.jsonArray.length(); i++) {     // 19 entries
                            json = this.jsonArray.getJSONObject(i);
                            if (json.getString("type").equals("Pool")) {
                                String mURL = json.getString("url");
                                String mDeadLine = json.getString("targetDeadline");
                                //MiningPool mp = new MiningPool(mURL, mDeadLine);
                                // Store the Values in a List that we can loop through to collect full data
                                MiningPool MP = new MiningPool(mURL, mDeadLine);
                                //mMiningPools.addPool(mURL, mDeadLine);
                                Log.d(TAG,"Should Call addPool("+mURL + "," + mDeadLine+")");
                                //mMiningPools.add(mp);
                            }
                        }

                    } catch (Exception e) {
                        Log.d(TAG,"getPoolList JSON exploded");
                    }
                }
            }
        };
        jsonCall.execute();
    }
    /* http://util.burst-team.us:8888/network/json
      collect the URL, type.equals("Pool") and targetDeadline

     [{"height":"264536","domain":"pool.burstmining.club","url":"http://pool.burstmining.club","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"1728000","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"178.62.39.204:8121","url":"http://178.62.39.204:8121","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"200000","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"pool.burstcoin.de","url":"http://pool.burstcoin.de","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"1727851","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"burst.ninja","url":"http://burst.ninja","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"86400","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"burstcoinpool.devip.xyz:8080","url":"http://burstcoinpool.devip.xyz:8080","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"2592000","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"wallet.burst-team.us:8127","url":"https://wallet.burst-team.us:8127","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"N/A","type":"Wallet","state":"OK","available":true},
     {"height":"264536","domain":"faucet.burst-team.us","url":"https://faucet.burst-team.us","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"N/A","type":"Faucet","state":"OK","available":true},
     {"height":"264536","domain":"util.burst-team.us:8889","url":"http://util.burst-team.us:8889","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"N/A","type":"Wallet","state":"OK","available":true},
     {"height":"264536","domain":"burstpool.ddns.net:8080","url":"http://burstpool.ddns.net:8080","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"3456000","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"pool.burstcoin.biz","url":"http://pool.burstcoin.biz","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"1814400","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"pool.burstcoin.eu","url":"http://pool.burstcoin.eu","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"6048000","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"wallet.burst-team.us:8126","url":"https://wallet.burst-team.us:8126","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"N/A","type":"Wallet","state":"OK","available":true},
     {"height":"264536","domain":"pool.burst-team.us","url":"http://pool.burst-team.us","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"172800","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"wallet.burst-team.us:8125","url":"https://wallet.burst-team.us:8125","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"N/A","type":"Wallet","state":"OK","available":true},
     {"height":"264536","domain":"mwallet.burst-team.us:8125","url":"https://mwallet.burst-team.us:8125","baseTarget":"2545385","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"N/A","type":"Wallet","state":"OK","available":true},
     {"height":"264536","domain":"pool.burstcoin.it","url":"http://pool.burstcoin.it","baseTarget":"2546562","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"172800","type":"Pool","state":"OK","available":true},
     {"height":"264536","domain":"faucet.burstcoin.pt","url":"http://faucet.burstcoin.pt","baseTarget":"2548486","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"N/A","type":"Faucet","state":"OK","available":true},
     {"height":"264536","domain":"faucet.burstcoin.info","url":"https://faucet.burstcoin.info","baseTarget":"2548486","generationSignature":"eeea602b05c0daf56dfde05c2...","targetDeadline":"N/A","type":"Faucet","state":"OK","available":true},
     {"height":"264506","domain":"wallet.burst-team.us:8128","url":"https://wallet.burst-team.us:8128","baseTarget":"4343122","generationSignature":"ecb663563f9e73ff7e02762f7...","targetDeadline":"N/A","type":"Wallet","state":"OK","available":true}]

    */


    /*
      For Each pool [url]/burst?requestType=getMiningInfo
      eg) burstcoinpool.devip.xyz:8080/burst?requestType=getMiningInfo
     */
    /* http://util.burst-team.us:8888/pool/json
       Return Result Set, All Prettied Up, Notice there is no tie to URL, boooo!
    [
    {"accountId":"12468105956737329840","accountRS":"BURST-7CPJ-BW8N-U4XF-CWW3U","name":"BURST.ninja assignment wallet","description":"This is the assignment wallet for BURST.ninja pool.","balance":"474595","assignedMiners":675,"successfulMiners":97,"foundBlocks":1097,"earnedAmount":"3212877"},
    {"accountId":"11894018496043975481","accountRS":"BURST-32TT-TSAC-HTKW-CC26C","name":"Burst-Team Pool","description":null,"balance":"46912","assignedMiners":735,"successfulMiners":124,"foundBlocks":1059,"earnedAmount":"3099611"},
    {"accountId":"Solo-Miners","accountRS":"Solo-Miners","name":"Solo-Miners","description":"","balance":"0","assignedMiners":38,"successfulMiners":38,"foundBlocks":775,"earnedAmount":"2269770"},
    {"accountId":"18401070918313114651","accountRS":"BURST-7Z2V-J9CF-NCW9-HWFRY","name":"Burstcoin.eu pool","description":null,"balance":"4593","assignedMiners":265,"successfulMiners":69,"foundBlocks":173,"earnedAmount":"506304"},
    {"accountId":"21869187791279079","accountRS":"BURST-8NZ9-X6AX-72BK-2KFM2","name":"v2 pool","description":null,"balance":"548859","assignedMiners":582,"successfulMiners":22,"foundBlocks":137,"earnedAmount":"401143"},
    {"accountId":"7551133661433248314","accountRS":"BURST-LGKU-3UUM-M6Q5-86SLK","name":"ITALIAN POOL","description":null,"balance":"15754","assignedMiners":467,"successfulMiners":48,"foundBlocks":121,"earnedAmount":"355140"},
    {"accountId":"3195398293854632251","accountRS":"BURST-JGBV-U7YK-SWHM-4P4QS","name":"tross Burstcoin pool","description":null,"balance":"2168","assignedMiners":31,"successfulMiners":13,"foundBlocks":66,"earnedAmount":"193103"},
    {"accountId":"15674744673246368361","accountRS":"BURST-RNMB-9FJW-3BJW-F3Z3M","name":"Burst Mining Club Pool Wallet","description":null,"balance":"8771","assignedMiners":26,"successfulMiners":13,"foundBlocks":50,"earnedAmount":"146346"},
    {"accountId":"14789046051569562492","accountRS":"BURST-6WVW-2WVD-YXE5-EZBHU","name":"burstcoin.biz Pool","description":null,"balance":"646","assignedMiners":76,"successfulMiners":20,"foundBlocks":46,"earnedAmount":"134640"},
    {"accountId":"15291186589713514299","accountRS":"BURST-GHTV-7ZP3-DY4B-FPBFA","name":"Pool.burstcoin.de","description":null,"balance":"12220","assignedMiners":63,"successfulMiners":16,"foundBlocks":38,"earnedAmount":"111168"},
    {"accountId":"17912950643250639288","accountRS":"BURST-YEFS-QJ32-K9Z5-HPW7K","name":"burstcoinpool.devip.xyz","description":"Wallet for the burstcoinpool.devip.xyz mining pool!","balance":"1382","assignedMiners":8,"successfulMiners":6,"foundBlocks":21,"earnedAmount":"61413"},
    {"accountId":"392861956774712841","accountRS":"BURST-6PJB-VT3P-B6ZQ-2TXWC","name":null,"description":null,"balance":"85","assignedMiners":58,"successfulMiners":4,"foundBlocks":10,"earnedAmount":"29269"},
    {"accountId":"10681581906896806002","accountRS":"BURST-7S5L-6UHX-SVS5-BU6HA","name":"MiningHere BurstPool","description":null,"balance":"77427","assignedMiners":807,"successfulMiners":3,"foundBlocks":7,"earnedAmount":"20493"},
    {"accountId":"4267101048149503620","accountRS":"BURST-DVN6-TGBU-EZQZ-5BYFQ","name":"5BYFQ","description":null,"balance":"2949","assignedMiners":2,"successfulMiners":1,"foundBlocks":1,"earnedAmount":"2949"}
    ]

    // I have to correlate URL to ID otherwise we dont know where to submit our nonces
     */

    // Maybe I should do my own custom class here, allow people to tweak in and out data
    private class MiningPool {

        private String mAcctID;
        private String mAcctRS;
        private String mName;
        private String mDeadline;
        private String mURL;

        public MiningPool(String url, String dl) {
            mURL = url;
            mDeadline = dl;

            //getRewardID(url);
            /*
            GetAsync jsonCall = new GetAsync(mURL + postURL) {
                @Override
                protected void onPostExecute (JSONObject json) {
                    if (json != null) {
                        try {
                            if(json.getString("type").equals("Pool")) {
                                String mURL = json.getString("url");
                                String mDeadLine = json.getString("targetDeadline");
                                // Store the Values in a List that we can loop through to collect full data
                                //mMiningPools.addPool(mURL, mDeadLine);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG,"Exploded in getPoolList reading JSON");
                        }
                    } else {

                    }
                }
            };
            jsonCall.execute();
            */
        }

        private String getRewardID(String url) {

                String result = "";
                try {
                    // create HttpClient
                    URL uri = new URL(url);
                    HttpURLConnection urlConnection = (HttpURLConnection) uri.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        result = BurstUtil.convertStreamToString(in);  // This returns the object name @ reference
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
                Log.d(TAG, result);
                // Need to add parsing in here, right now this is a giant block of HTML
                return result;


                /*<div id="current-block-info">
                <span id="current-block-details">Block: 264590, Scoop: 1783, Diff: 6209</span><br>
                Miners: <span id="num-miners">82</span><br>
                <a href="https://block.burstcoin.info/acc.php?acc=BURST-32TT-TSAC-HTKW-CC26C" target="_blank">BURST-6WVW-2WVD-YXE5-EZBHU</a><br>
                [<a href="http://127.0.0.1:8125/rewardassignment.html" target="_blank">Set reward assignment...</a>]
                </div> */
        }

    }

}
