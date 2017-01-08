package burstcoin.com.burst.mining;

import android.content.SharedPreferences;
import android.util.Log;
import burstcoin.com.burst.BurstUtil;
import burstcoin.com.burst.PostAsync;
import burstcoin.com.burst.plotting.PlotFile;
import burstcoin.com.burst.plotting.PlotFiles;
import burstcoin.com.burst.tools.BurstContext;
import fr.cryptohash.Shabal256;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IceBurst 7/21/16.
 * Used to Manage Traffic to and From the Pool
 * Find the DeadLines and Submit as required
 */

public class MiningService implements IntMinerDeadLine{

    // Live BlockData
    public Block mActiveBlock = new Block();
    static String TAG = "MiningService";

    // Representative of User Button
    public boolean running = false;

    // Used for the GetBlockInfo from the Pool
    Timer mPoller;
    int POLL_SECONDS;

    // This need to be a static pulled some a central point
    static String poolUrl = "http://m.burst4all.com";
    //static String poolUrl = "http://mobile.burst-team.us:8080";
    static String poolGetBlockInfo = "/burst?requestType=getMiningInfo";

    // This is where we stack up our mining Threads
    private final BlockingQueue<Runnable> minerThreads = new LinkedBlockingQueue<Runnable>();
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor mThreadPool;

    private PlotFiles mPlotFiles;
    private String mNumericID;
    BigInteger mBestdeadline = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private IntMiningStatus mCallback;

    // Constructor for the Mining Service
    public MiningService (IntMiningStatus cb, PlotFiles pf, String numeric) {
        mCallback = cb;
        mPlotFiles = pf;
        mNumericID = numeric;
        mThreadPool = new ThreadPoolExecutor(1, NUMBER_OF_CORES, 1, TimeUnit.SECONDS, minerThreads);

        //ToDo: Do some checks, if everything is good start auto-mining
        //start();
    }

    public void start() {
        Log.d(TAG, "Starting");
        running = true;
        SharedPreferences settings = BurstContext.getAppContext().getSharedPreferences("MINING", 0);
        int POLL_SECONDS = settings.getInt("POLLTIMER", 3);
        mPoller = new Timer();
        mPoller.schedule( new TimerTask() {
            public void run() {
                Log.v(TAG, "Polling for a new Block");
                String mBlockData = GET(poolUrl+poolGetBlockInfo);
                try {
                    JSONObject mJSONBlockData = new JSONObject(mBlockData);
                    if (mActiveBlock.height != mJSONBlockData.getLong("height")) {
                        mActiveBlock.height = Long.parseLong(mJSONBlockData.getString("height"));
                        mActiveBlock.genSig = mJSONBlockData.getString("generationSignature");
                        mActiveBlock.baseTarget = Long.parseLong(mJSONBlockData.getString("baseTarget"));
                        mActiveBlock.targetDeadline = mJSONBlockData.getLong("targetDeadline");
                        mActiveBlock.reqProcessingTime = mJSONBlockData.getInt("requestProcessingTime");
                        mCallback.notice("BLOCK","HEIGHT",Long.toString(mActiveBlock.height));
                        // We got a new block lets mine this puppy if we can.....
                        stopAndRestartMining();
                    }
                    // Else it's a repeat block don't do anything.
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

    // This is Where Threaded Miners give back their best Deadlines per GB
    @Override
    public synchronized void foundDeadLine(long nonce, BigInteger deadline) {
        String mStringBestDL = BurstUtil.BigIntToHumanReadableDate(deadline);
        Log.d(TAG,"Best Deadline was:" + mStringBestDL);
        if (mBestdeadline.compareTo(deadline) == 1 ) {
            mBestdeadline = deadline;
            // Send the New Best Deadline to the GUI
            mCallback.notice("DEADLINE", mStringBestDL);
            if(mBestdeadline.compareTo(BigInteger.valueOf(mActiveBlock.targetDeadline)) < 0) {
                SubmitShare(nonce); // Pass the deadline up for submission checks
            }
        }
    }

    public void SubmitShare(final Long mNonce) {
        // This string contains miner and size as well
        // bytes = sprintf_s(buffer, buffer_size, "POST /burst?requestType=submitNonce&accountId=%llu&nonce=%llu&deadline=%llu
                                                // HTTP/1.0\r\n
                                                // X-Miner: IBAndroid %s\r\n
                                                // X-Capacity: %llu\r\n
                                                // Connection: close\r\n\r\n", iter->account_id, iter->nonce, iter->best, version, total);

        String URL = poolUrl + "/burst/?requestType=submitNonce&secretPhrase=pool-mining&nonce=" + String.valueOf(mNonce) + "&accountId=" + mNumericID;
        if (mBestdeadline.compareTo(BigInteger.valueOf(mActiveBlock.targetDeadline)) < 0) {
            Log.d(TAG, "Submitting Share:" + mNonce);
            //GetAsync jsonCall = new GetAsync(URL) {
            PostAsync jsonCall = new PostAsync() {
                @Override
                protected void onPostExecute(JSONObject json) {
                    if (json != null) {
                        try {
                            Log.d(TAG, json.toString());
                            String mRespond = json.getString("result");
                            if(mRespond.equals("success")) {
                                mCallback.notice("SUBMITNONCE", "SUCCESS");
                                Log.d(TAG, "Nonce Accepted by Pool:" + mNonce);
                            }
                        } catch (JSONException e) {
                            mCallback.notice("SUBMITNONCE", "INVALID");
                        }
                    } else {
                        mCallback.notice("SUBMITNONCE", "NULL");
                    }
                }
            };
            //jsonCall.execute();
            jsonCall.execute(URL, "X-Miner","IBAndroid");
        }
        else {
            Log.d(TAG, "Did Not Submit: " + URL);
        }
    }

    public void stopAndRestartMining(){

        // Dump all the mining requests in Queue for the last block
        minerThreads.clear();

        mBestdeadline = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
        for(PlotFile mPlotFile: mPlotFiles.getPlotFiles()){
            PlotFileMiner miner = new PlotFileMiner(mPlotFile, this);
            minerThreads.add(miner);
            mThreadPool.execute(miner);
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
            //Log.d(TAG, e.getLocalizedMessage());
            Log.d(TAG,"GET Faulted");
        }
        return result;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    class PlotFileMiner implements Runnable {

        private PlotFile plotFile;
        private int scoopnum;
        private boolean isMining = true;
        private IntMinerDeadLine mSendDeadLine;
        private Thread mThisThread;

        public PlotFileMiner(PlotFile plotFile, IntMinerDeadLine mCB) {
            mSendDeadLine = mCB;
            ByteBuffer buf = ByteBuffer.allocate(32 + 8);

            // d7789cee10c3a0bc455edf8edd0862f6967dad29ea4637e4e5ad4bfc6e21da79  <-- Sig Sample
            //   0         10        20         30       40       50       60
            // Need to Convert the ASCii into a Hex(Byte) equivilant

            buf.put(BurstUtil.stringToBytes(mActiveBlock.genSig));
            buf.putLong(mActiveBlock.height);
            Shabal256 md = new Shabal256();
            md.update(buf.array());
            BigInteger hashnum = new BigInteger(1, md.digest());
            scoopnum = hashnum.mod(BigInteger.valueOf(MiningPlot.SCOOPS_PER_PLOT)).intValue();
            this.plotFile = plotFile;
        }

        public void stop() {
            isMining = false;
            mThisThread.interrupt();
        }

        @Override
        public void run() {
            mThisThread = Thread.currentThread();
            String mPlotFileLocation = BurstUtil.getPathToSD() + '/' + plotFile.getFileName();
            try {
                RandomAccessFile f = new RandomAccessFile(mPlotFileLocation, "r");
                //long chunks = plotFile.getPlots()/ plotFile.getStaggeramt();
                long chunks = 4096;
                BigInteger lowest = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
                BigInteger mDeadLine;
                long mWorkingNonce = 0;
                for (long i = 0; i < chunks; i++) {
                    f.seek((i * plotFile.getStaggeramt() * MiningPlot.PLOT_SIZE) + (scoopnum * plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE));
                    //Log.d(TAG,"Chunk "+i+": Seeked to Position:"+ (i * plotFile.getStaggeramt() * MiningPlot.PLOT_SIZE) + (scoopnum * plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE));
                    byte[] chunk = new byte[(int) (plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE)];
                    //Log.d(TAG,"Read from disk:" + plotFile.getStaggeramt() * MiningPlot.SCOOP_SIZE + " bytes");
                    f.readFully(chunk);
                    mWorkingNonce = plotFile.getStartnonce() + (i * plotFile.getStaggeramt());
                    mDeadLine = getDeadLineForNonce(chunk,mWorkingNonce);

                    if (lowest.compareTo(mDeadLine) == 1) {
                        lowest = mDeadLine;
                        Log.d(TAG, "New Best Deadline is Nonce:"+ mWorkingNonce + " of: "+ BurstUtil.BigIntToHumanReadableDate(mDeadLine));
                    }

                    int compare = mDeadLine.compareTo(BigInteger.valueOf(mActiveBlock.targetDeadline));
                    if(compare <= 0) {                                                                 // This is if it's good enough for the Pool Deadline
                        mSendDeadLine.foundDeadLine(mWorkingNonce,mDeadLine);                          // Send this Deadline back to the Mining Service for a 2nd Evaluation and Submitting
                        //Log.d(TAG, "Found a better submission @ " + mWorkingNonce + " with a DL of:" + deadline);
                    }
                    if(!isMining)return;
                }
                // Only send the best deadline for the GB Plot
                mSendDeadLine.foundDeadLine(mWorkingNonce,lowest);

            } catch (FileNotFoundException e) {
                Log.e(TAG, "Cannot open file: " + mPlotFileLocation);
            } catch (IOException e) {
                Log.e(TAG, "Error reading file: " + mPlotFileLocation);
            }

            Log.d(TAG, "Finished mining a plot file:" + plotFile.getFileName());
            minerThreads.remove(this);
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

    }
}
