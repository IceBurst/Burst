package burstcoin.com.burst.mining;

import android.annotation.TargetApi;
import android.util.Log;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import burstcoin.com.burst.BurstUtil;
import burstcoin.com.burst.GetAsync;
import burstcoin.com.burst.JSONCaller;
import burstcoin.com.burst.R;
import burstcoin.com.burst.plotting.PlotFile;
import burstcoin.com.burst.plotting.PlotFiles;
import fr.cryptohash.Shabal256;
import nxt.crypto.Crypto;
import nxt.util.Convert;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by IceBurst 7/21/16.
 * Used to Manage Traffic to and From the Pool
 */

public class MiningService {

    // Live BlockData
    public Block mActiveBlock = new Block();
    static String TAG = "MiningService";

    // Only Supporting official on initial release
    public static String POOL_TYPE_URAY="uray";
    public static String POOL_TYPE_OFFICAL="offical";

    public boolean running = false;

    // Used for the GetBlockInfo
    Timer mPoller;
    static int POLL_SECONDS = 3;

    String poolType;

    String poolUrl = "http://mobile.burst-team.us";
    String poolGetBlockInfo = "/burst?requestType=getMiningInfo";

    int minerCpuThreads = 1;
    boolean minerCpuEnabled;
    // This class is commented out
    //ArrayList<PlotFileMiner> minerThreads = new ArrayList<PlotFileMiner>();

    private PlotFiles mPlotFiles;
    private String mNumericID;
    long lastShareSubmitTime;
    BigInteger mBestdeadline = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    Random rand;
    boolean startedCPUMining = false;
    private IntMiningStatus mCallback;


    // Constructor for the Mining Service
    public MiningService (IntMiningStatus cb, PlotFiles pf, String numeric) {
        mCallback = cb;
        mPlotFiles = pf;
        mNumericID = numeric;
        //ToDo: Do some checks, if everything is good start auto-mining
        //start();
    }


    public void start() {
        Log.d(TAG, "Starting");
        running = true;
        mPoller = new Timer();
        mPoller.schedule( new TimerTask() {
            public void run() {
                // This is where we poll the webserver for block data
                //Log.d(TAG, "Trying:"+poolUrl+poolGetBlockInfo);
                String mBlockData = GET(poolUrl+poolGetBlockInfo);
                //Log.d(TAG, mBlockData);
                try {
                    JSONObject mJSONBlockData = new JSONObject(mBlockData);
                    if (mActiveBlock.height != mJSONBlockData.getLong("height")) {
                        mActiveBlock.height = Long.parseLong(mJSONBlockData.getString("height"));
                        mActiveBlock.genSig = mJSONBlockData.getString("generationSignature");
                        mActiveBlock.baseTarget = Long.parseLong(mJSONBlockData.getString("baseTarget"));
                        mActiveBlock.targetDeadline = mJSONBlockData.getLong("targetDeadline");
                        //mActiveBlock.reqProcessingTime = mJSONBlockData.getBoolean("requestProcessingTime"); <-- This was throwing org.json.JSONException: Value 0 at requestProcessingTime of type java.lang.Integer cannot be converted to boolean
                        mActiveBlock.reqProcessingTime = mJSONBlockData.getInt("requestProcessingTime");
                        mCallback.notice("BLOCK","HEIGHT",Long.toString(mActiveBlock.height));
                        // Holy crap we got a new block lets mine this puppy if we can.....
                        stopAndRestartMining();

                    }
                    // Else it's a repeat block dont do anything.
                } catch (org.json.JSONException e) {
                    Log.d(TAG,"JSON Object Exploded on GetBlockData");  // Just try again in another 2 seconds
                    Log.e(TAG, "exception", e);
                }
            }
        }, 0, 1000*POLL_SECONDS);
        mCallback.notice("MINING","STARTED");
    }

    public void stop() {
        mPoller.cancel();
        running = false;
        mCallback.notice("MINING","STOPPED");
    }

    /*
    public synchronized void registerBestShareForChunk(BigInteger lowestInChunk,long nonce,PlotFile plotFile){
           if(lowestInChunk.compareTo(deadline) < 0) {
               deadline = lowestInChunk;
               //shareExecutor.execute(new SubmitShare(nonce,plotFile,deadline));
           }
       }
    */

    public void init(){
        rand = new Random();
        //List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        //converters.add(new StringHttpMessageConverter());
        //restTemplate.setMessageConverters(converters);
        //loadPassPhrases();
    }


    public long getLastShareSubmitTime(){
        return lastShareSubmitTime;
    }

    public synchronized long nextNonce(){
        return (long)(rand.nextDouble()*Long.MAX_VALUE);
    }

    /* We Don't need the passphrase for pool mining
    private void loadPassPhrases(){
        try {
            List<String> passphrases = Files.readAllLines(Paths.get("passphrases.txt"), Charset.forName("US-ASCII"));
            for(String ps : passphrases) {
                if(!ps.isEmpty()) {
                    byte[] publicKey = Crypto.getPublicKey(ps);
                    byte[] publicKeyHash = Crypto.sha256().digest(publicKey);
                    Long id = Convert.fullHashToId(publicKeyHash);
                    loadedPassPhrases.put(id, ps);
                    LOGGER.info("Added key: {" + ps + "} -> {" + Convert.toUnsignedLong(id) + "}");
                }
            }
        } catch (IOException e) {
            LOGGER.info("Warning: no passphrases.txt found");

        }
    }
    */

    public void stopAndRestartMining(){
        /* Address this later, worry about breaking cycles
        if(minerCpuEnabled){
            if(!startedCPUMining){
                //LOGGER.info("Starting CPU Mining with {"+minerCpuThreads+"} threads");
                for(int i=0;i<minerCpuThreads;i++){
                    cpuMiningPool.execute(new CPUMiner());
                }
            }
        }
        */
        /* This is to stop existing miners
        for(PlotFileMiner miner : minerThreads){
            miner.stop();
            //miner.plotFile.addIncomplete();
            //LOGGER.info("Stopped mining {"+miner.plotFile.getUUID()+"} due to block change");
        }
        */
        //minerThreads.clear();
        /* What is this non-sense
        this.processing = null;
        while(this.processing==null){
            this.processing = netStateService.getCurrentState();
            if(this.processing==null)try{Thread.sleep(500);}catch(Exception ex){}
        }
        */

        mBestdeadline = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
        // I Think we already have plot files...
        for(PlotFile mPlotFile: mPlotFiles.getPlotFiles()){
            // Pass the mPlotfile to a miner thats Async or something
            PlotFileMiner miner = new PlotFileMiner(mPlotFile);
            Thread mMiner = new Thread(miner);
            mMiner.start();
            //minerThreads.add(miner);
            //executor.execute(miner);
            // This should be AsyncTask Probably
        }
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            URL uri = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) uri.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = convertStreamToString(in);  // This returns the object name @ reference
                // This result is a JSON that we can use our module on
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return result;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    /*
    class CPUMiner implements Runnable{
        long address = 0;

        @Override
        public void run() {
            if(StringUtils.isEmpty(minerCpuAddress)){

            }else{
                address = plotService.getPlots().get(0).getAddress();
            }

            if(address==0){
                //LOGGER.info("No address found for cpu mining.");
                return;
            }

            while(true){
                long nonce = nextNonce();

                MiningPlot plot = new MiningPlot(address,nonce);
                if(poolType.equals(POOL_TYPE_OFFICAL)){
                    checkPlotOffical(plot,nonce);
                }else if(poolType.equals(POOL_TYPE_URAY)){
                    checkPlotOffical(plot,nonce);
                }
            }
        }

        public void checkPlotOffical(MiningPlot plot,long nonce){
            ByteBuffer buf = ByteBuffer.allocate(32 + 8);
            buf.put(processing.getGensig());
            buf.putLong(processing.getHeightL());

            Shabal256 md = new Shabal256();
            md.update(buf.array());
            BigInteger hashnum = new BigInteger(1, md.digest());
            int scoopnum = hashnum.mod(BigInteger.valueOf(MiningPlot.SCOOPS_PER_PLOT)).intValue();
            md.reset();
            md.update(processing.getGensig());
            plot.hashScoop(md,scoopnum);
            byte[] hash = md.digest();
            BigInteger num = new BigInteger(1, new byte[] {hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0]});
            BigInteger deadline = num.divide(BigInteger.valueOf(processing.getBaseTargetL()));
            int compare = deadline.compareTo(BigInteger.valueOf(processing.getTargetDeadlineL()));

            if(compare <= 0) {
                shareExecutor.execute(new SubmitShare(nonce, address ,deadline));
            }

        }

        public void checkPlotUray(MiningPlot plot,long nonce){
        }
    }*/


    class PlotFileMiner implements Runnable {

        private PlotFile plotFile;
        private int scoopnum;
        private boolean running = true;

        public void stop() {
            running = false;
        }

        public PlotFileMiner(PlotFile plotFile) {

            ByteBuffer buf = ByteBuffer.allocate(32 + 8);

            // d7789cee10c3a0bc455edf8edd0862f6967dad29ea4637e4e5ad4bfc6e21da79  <-- Sig Sample
            //   0         10        20         30       40       50       60
            // Need to Convert the ASCii into a Hex(Byte) equivilant

            buf.put(BurstUtil.stringToBytes(mActiveBlock.genSig));
            buf.putLong(mActiveBlock.height);
            /*
            buf.put(processing.getGensig());
            buf.putLong(processing.getHeightL());
            */
            Shabal256 md = new Shabal256();
            md.update(buf.array());
            BigInteger hashnum = new BigInteger(1, md.digest());
            scoopnum = hashnum.mod(BigInteger.valueOf(MiningPlot.SCOOPS_PER_PLOT)).intValue();
            this.plotFile = plotFile;
        }

        @Override
        public void run() {
            String mPlotFileLocation = BurstUtil.getPathToSD() + '/' + plotFile.getFileName();
            try {

                RandomAccessFile f = new RandomAccessFile(mPlotFileLocation, "r");
                //long chunks = plotFile.getPlots()/ plotFile.getStaggeramt();
                long chunks = 4096;
                BigInteger lowest = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
                BigInteger mDeadLine;
                for (long i = 0; i < chunks; i++) {
                    f.seek((i * plotFile.getStaggeramt() * MiningPlot.PLOT_SIZE) + (scoopnum * plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE));
                    //Log.d(TAG,"Chunk "+i+": Seeked to Position:"+ (i * plotFile.getStaggeramt() * MiningPlot.PLOT_SIZE) + (scoopnum * plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE));
                    byte[] chunk = new byte[(int) (plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE)];
                    //Log.d(TAG,"Read from disk:" + plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE + " bytes");
                    f.readFully(chunk);
                    // Set to only one pool type to start
                    //if(poolType.equals(POOL_TYPE_URAY)) {
                    //checkChunkPoolUray(chunk, plotFile.getStartnonce() + (i * plotFile.getStaggeramt()));
                    //}else if(poolType.equals(POOL_TYPE_OFFICAL)){
                    long mWorkingNonce = plotFile.getStartnonce() + (i * plotFile.getStaggeramt());
                    mDeadLine = getDeadLineForNonce(chunk,mWorkingNonce);
                    //}

                    if (lowest.compareTo(mDeadLine) == 1) {
                        // We always fall in here because checkChuck is fresh everytime!
                        lowest = mDeadLine;
                        Log.d(TAG, "New Best Deadline is Nonce:"+ mWorkingNonce + " of: "+ BurstUtil.BigIntToHumanReadableDate(mDeadLine));
                    }

                    // ToDo: Strengthen this to check Target Deadline && Best Submitted, Maybe we should loop it out to a caller
                    int compare = mDeadLine.compareTo(BigInteger.valueOf(mActiveBlock.targetDeadline));
                    if(compare <= 0) {
                        // Pass it back to see if it's the best to submit
                        //Log.d(TAG, "Found a better submission @ " + mWorkingNonce + " with a DL of:" + deadline);
                        //SubmitShare(chunk_start_nonce+i, mNumericID);
                    }
                    if(!running)return;
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Cannot open file: " + mPlotFileLocation);
                //e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "Error reading file: " + mPlotFileLocation);
            }

            Log.d(TAG, "Finished mining a plot file:" + plotFile.getFileName());
            //plotFile.addChecked();
            //minerThreads.remove(this);
        }

        private BigInteger getDeadLineForNonce(byte[] chunk,long chunk_start_nonce){
            Shabal256 md = new Shabal256();
            BigInteger deadline = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
            for(long i = 0; i < plotFile.getStaggeramt(); i++) {
                md.reset();
                md.update(BurstUtil.stringToBytes(mActiveBlock.genSig));
                md.update(chunk, (int) (i * MiningPlot.SCOOP_SIZE), MiningPlot.SCOOP_SIZE);
                byte[] hash = md.digest();
                BigInteger num = new BigInteger(1, new byte[] {hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0]});
                deadline = num.divide(BigInteger.valueOf(mActiveBlock.baseTarget));
            }
            return deadline;
        }

    /*
    private class SubmitShare implements  Runnable{

        long nonce;
        PlotFile plotFile;
        BigInteger deadline;
        long address;

        public SubmitShare(long nonce,PlotFile plotFile,BigInteger deadline){
            this.nonce = nonce;
            this.plotFile = plotFile;
            this.deadline = deadline;
            this.address = plotFile.getAddress();
        }

        public SubmitShare(long nonce,long address,BigInteger deadline){
            this.nonce = nonce;
            this.deadline = deadline;
            this.address = address;
        }

        @Override
        public void run() {
            String shareRequest = Convert.toUnsignedLong(plotFile.getAddress()) + ":" + nonce + ":" + mActiveBlock.height + " deadline {"+deadline+"}";
            //LOGGER.info("Submitting share {"+shareRequest+"}");

            // ToDo: This should all be done with our Async Classes
            Log.d(TAG, "Submitting Share");
            try {
                if(poolType.equals(POOL_TYPE_URAY)) {
                    String request = poolUrl + "/burst?requestType=submitNonce&secretPhrase=pool-mining&nonce=" + Convert.toUnsignedLong(nonce) + "&accountId=" + Convert.toUnsignedLong(address);
                    String response = restTemplate.postForObject(request, shareRequest, String.class);
                    LOGGER.info("Reponse {"+response+"}}");
                    if(plotFile==null){
                        Log.d(TAG,"Submitted CPU Share.");
                    }else{
                        //plotFile.addShare();
                    }
                }else if(poolType.equals(POOL_TYPE_OFFICAL)){
                    shareRequest = Convert.toUnsignedLong(address)+":"+Convert.toUnsignedLong(nonce)+":"+processing.getHeight()+"\n";
                    String response = restTemplate.postForObject(poolUrl + "/pool/submitWork",shareRequest,String.class);
                                                                                 ^^-- This is not part of NXT
                    LOGGER.info("Reponse {"+response+"}}");
                    if(plotFile==null){
                        Log.d(TAG,"Submitted CPU Share.");
                    }else{
                        //plotFile.addShare();
                    }                }
                lastShareSubmitTime = System.currentTimeMillis();
            }catch(Exception ex){
                Log.d(TAG, "Failed to submitshare{"+shareRequest+"}");
            }
        }
    }*/

        public void SubmitShare(Long mNonce, String mNumericID) {
            // ToDo: Validate that is lower than a chunk from all other plots before submitting
            // ToDo: Wrap in a check, then figure out the NULL Pointer, less to crash is easier to find in this case

            // Also check the deadline supported by the pool
            String URL = poolUrl + "/burst/?requestType=submitNonce&secretPhrase=pool-mining&nonce=" + String.valueOf(mNonce) + "&accountId=" + mNumericID;
            if (mBestdeadline.compareTo(BigInteger.valueOf(mActiveBlock.targetDeadline)) < 0) {
                GetAsync jsonCall = new GetAsync(URL) {
                    @Override
                    protected void onPostExecute(JSONObject json) {
                        if (json != null) {
                            try {
                                Log.d(TAG, json.toString());
                                String mRespond = json.getString("account");
                                mCallback.notice("SUBMITNONCE", "SUCCESS");
                            } catch (JSONException e) {
                                mCallback.notice("SUBMITNONCE", "INVALID");
                            }
                        } else {
                            mCallback.notice("SUBMITNONCE", "NULL");
                        }
                    }
                };
                jsonCall.execute();
            }
            else {
                Log.d(TAG, "Did Not Submit: URL");
            }

        }
    }
}
