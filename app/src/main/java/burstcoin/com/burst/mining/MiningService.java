package burstcoin.com.burst.mining;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import burstcoin.com.burst.GetAsync;
import burstcoin.com.burst.JSONCaller;
import fr.cryptohash.Shabal256;
import nxt.crypto.Crypto;
import nxt.util.Convert;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by IceBurst 7/21/16.
 * Used to Manage Traffic to and From the Pool
 */

public class MiningService {

    /*  This is the request to the Server for current block information
    GET /burst?requestType=getMiningInfo HTTP/1.1
    Accept-Encoding: gzip
    User-Agent: Jetty/9.3.8.RC0
    Host: pool.burst-team.us

    // This is the response from the server about the current block, looks like POST back data
    HTTP/1.1 200 OK
    Connection: Keep-Alive
    Content-Length: 181
    Content-Type: text/html; charset=UTF-8
    Date: Thu, 21 Jul 2016 19:48:03 GMT

    {
    "generationSignature":"7dc92a727aaa098113668f023f33fb1f040632884671ea69bc61004707e1f2e1",
       // My notes:        1234567890123456789012345678901234567890123456789012345678901234
                           0        10        20        30        40        50        60
       // GenSig is a 64 byte key
    "baseTarget":"2070337",
    "requestProcessingTime":0,
    "height":"253939",
    "targetDeadline":172800
    }
    */

    // Live BlockData
    Block mActiveBlock = new Block();

    static String TAG = "MiningService";

    // Only Supporting official on initial release
    public static String POOL_TYPE_URAY="uray";
    public static String POOL_TYPE_OFFICAL="offical";

    // Used for the GetBlockInfo
    Timer mPoller;
    static int POLL_SECONDS = 2;

    String poolType;

    String poolUrl = "http://pool.burst-team.us";
    String poolGetBlockInfo = "/burst?requestType=getMiningInfo";

    int minerCpuThreads = 1;
    boolean minerCpuEnabled;
    // This class is commented out
    //ArrayList<PlotFileMiner> minerThreads = new ArrayList<PlotFileMiner>();
    long lastShareSubmitTime;
    BigInteger deadline = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    Random rand;
    boolean startedCPUMining = false;

    // Constructor for the Mining Service
    public MiningService () {
        start();
    }



    public void start() {
        Log.d(TAG, "starting");
        mPoller = new Timer();
        mPoller.schedule( new TimerTask() {
            public void run() {
                // This is where we poll the webserver for block data
                Log.d(TAG, "Trying:"+poolUrl+poolGetBlockInfo);
                String mBlockData = GET(poolUrl+poolGetBlockInfo);
                Log.d(TAG, mBlockData);  // Looks like JSON as we expected
                try {
                    JSONObject mJSONBlockData = new JSONObject(mBlockData);
                    // We are exploding instead off going into our if, I dont understand why
                    if (mActiveBlock.height != mJSONBlockData.getLong("height")) {
                        // We are on a new block, lets work hard!
                        mActiveBlock.height = Long.parseLong(mJSONBlockData.getString("height"));
                        mActiveBlock.genSig = mJSONBlockData.getString("generationSignature");
                        mActiveBlock.baseTarget = Long.parseLong(mJSONBlockData.getString("baseTarget"));
                        mActiveBlock.targetDeadline = mJSONBlockData.getLong("targetDeadline");
                        mActiveBlock.reqProcessingTime = mJSONBlockData.getBoolean("requestProcessingTime");
                        Log.d(TAG,"We just got our next block");
                    }
                    // Else it's a repeat block dont do anything.
                } catch (org.json.JSONException e) {
                    Log.d(TAG,"JSON Object Exploded on GetBlockData");  // Just try again in another 2 seconds
                }
            }
        }, 0, 1000*POLL_SECONDS);
    }

    public void stop() {
        mPoller.cancel();
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

    /*
    public void stopAndRestartMining(){
        if(minerCpuEnabled){
            if(!startedCPUMining){
                //LOGGER.info("Starting CPU Mining with {"+minerCpuThreads+"} threads");
                for(int i=0;i<minerCpuThreads;i++){
                    //cpuMiningPool.execute(new CPUMiner());
                }
            }
        }

        for(PlotFileMiner miner : minerThreads){
            miner.stop();
            //miner.plotFile.addIncomplete();
            //LOGGER.info("Stopped mining {"+miner.plotFile.getUUID()+"} due to block change");
        }
        minerThreads.clear();
        this.processing = null;
        while(this.processing==null){
            this.processing = netStateService.getCurrentState();
            if(this.processing==null)try{Thread.sleep(500);}catch(Exception ex){}
        }

        deadline = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
        for(PlotFile plotFile: plotService.getPlots()){
            PlotFileMiner miner = new PlotFileMiner(plotFile);
            minerThreads.add(miner);
            executor.execute(miner);
        }
    }
*/
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
    }
*/

/*
    class PlotFileMiner implements Runnable{

        private PlotFile plotFile;
        private int scoopnum;
        private boolean running=true;

        public void stop(){
            running=false;
        }

        public PlotFileMiner(PlotFile plotFile){

            ByteBuffer buf = ByteBuffer.allocate(32 + 8);
            buf.put(processing.getGensig());
            buf.putLong(processing.getHeightL());

            Shabal256 md = new Shabal256();
            md.update(buf.array());
            BigInteger hashnum = new BigInteger(1, md.digest());
            scoopnum = hashnum.mod(BigInteger.valueOf(MiningPlot.SCOOPS_PER_PLOT)).intValue();
            this.plotFile = plotFile;
        }

        @Override
        public void run() {

            try(RandomAccessFile f = new RandomAccessFile(plotFile.getPlotFile(), "r")) {
                long chunks = plotFile.getPlots()/ plotFile.getStaggeramt();
                for(long i = 0; i < chunks; i++) {
                    f.seek((i * plotFile.getStaggeramt() * MiningPlot.PLOT_SIZE) + (scoopnum * plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE));
                    byte[] chunk = new byte[(int) (plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE)];
                    f.readFully(chunk);
                    if(poolType.equals(POOL_TYPE_URAY)) {
                        checkChunkPoolUray(chunk,plotFile.getStartnonce() + (i * plotFile.getStaggeramt()));
                    }else if(poolType.equals(POOL_TYPE_OFFICAL)){
                        checkChunkPoolOffical(chunk,plotFile.getStartnonce() + (i * plotFile.getStaggeramt()));
                    }
                    if(!running)return;
                }
            } catch (FileNotFoundException e) {
                LOGGER.info("Cannot open file: " + plotFile.getPlotFile().getName());
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.info("Error reading file: " + plotFile.getPlotFile().getName());
            }

            LOGGER.info("Finished mining {"+plotFile.getUUID()+"}");
            plotFile.addChecked();
            minerThreads.remove(this);
        }

        private void checkChunkPoolUray(byte[] chunk,long chunk_start_nonce){
            Shabal256 md = new Shabal256();
            BigInteger lowest = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
            long lowestscoop = 0;
            for(long i = 0; i < plotFile.getStaggeramt(); i++) {
                md.reset();
                md.update(processing.getGensig());
                md.update(chunk, (int) (i * MiningPlot.SCOOP_SIZE), MiningPlot.SCOOP_SIZE);
                byte[] hash = md.digest();
                BigInteger num = new BigInteger(1, new byte[] {hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0]});
                BigInteger deadline = num.divide(BigInteger.valueOf(processing.getBaseTargetL()));

                int compare = deadline.compareTo(lowest);
                if(compare < 0) {
                    lowest = deadline;
                    lowestscoop = chunk_start_nonce + i;
                }
                if(!running)return;
            }
            registerBestShareForChunk(lowest,lowestscoop,plotFile);
        }

        private void checkChunkPoolOffical(byte[] chunk,long chunk_start_nonce){
            Shabal256 md = new Shabal256();
            for(long i = 0; i < plotFile.getStaggeramt(); i++) {
                md.reset();
                md.update(processing.getGensig());
                md.update(chunk, (int) (i * MiningPlot.SCOOP_SIZE), MiningPlot.SCOOP_SIZE);
                byte[] hash = md.digest();
                BigInteger num = new BigInteger(1, new byte[] {hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0]});
                BigInteger deadline = num.divide(BigInteger.valueOf(processing.getBaseTargetL()));
                int compare = deadline.compareTo(BigInteger.valueOf(processing.getTargetDeadlineL()));

                if(compare <= 0) {
                    shareExecutor.execute(new SubmitShare(chunk_start_nonce+i, plotFile,deadline));
                }
                if(!running)return;
            }

        }

    }
*/

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
            String shareRequest = Convert.toUnsignedLong(plotFile.getAddress()) + ":" + nonce + ":" + processing.getHeight()+" deadline {"+deadline+"}";
            LOGGER.info("Submitting share {"+shareRequest+"}");
            try {
                if(poolType.equals(POOL_TYPE_URAY)) {
                    String request = poolUrl + "/burst?requestType=submitNonce&secretPhrase=pool-mining&nonce=" + Convert.toUnsignedLong(nonce) + "&accountId=" + Convert.toUnsignedLong(address);
                    String response = restTemplate.postForObject(request, shareRequest, String.class);
                    LOGGER.info("Reponse {"+response+"}}");
                    if(plotFile==null){
                        LOGGER.info("Submitted CPU Share.");
                    }else{
                        plotFile.addShare();
                    }
                }else if(poolType.equals(POOL_TYPE_OFFICAL)){
                    shareRequest = Convert.toUnsignedLong(address)+":"+Convert.toUnsignedLong(nonce)+":"+processing.getHeight()+"\n";
                    String response = restTemplate.postForObject(poolUrl + "/pool/submitWork",shareRequest,String.class);
                    LOGGER.info("Reponse {"+response+"}}");
                    if(plotFile==null){
                        LOGGER.info("Submitted CPU Share.");
                    }else{
                        plotFile.addShare();
                    }                }
                lastShareSubmitTime = System.currentTimeMillis();
            }catch(Exception ex){
                Log.d(TAG, "Failed to submitshare{"+shareRequest+"}");
            }
        }
    }
    */
}
