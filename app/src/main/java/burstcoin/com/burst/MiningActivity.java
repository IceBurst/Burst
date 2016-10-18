package burstcoin.com.burst;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import burstcoin.com.burst.mining.IntMiningStatus;
import burstcoin.com.burst.mining.MiningPools;
import burstcoin.com.burst.mining.MiningService;
import burstcoin.com.burst.plotting.IntPlotStatus;
import burstcoin.com.burst.plotting.PlotFiles;
import burstcoin.com.burst.plotting.Plotter;
import burstcoin.com.burst.tools.BurstContext;
import burstcoin.com.burst.tools.PowerTool;

public class MiningActivity extends AppCompatActivity implements IntMiningStatus, IntProvider {

    final static String TAG = "MiningActivity";
    final static String sPoolServer = "mobile.burst-team.us:8080";
    final static String sPoolNumericID = "16647933376790760136"; // Mobile:8080

    private MiningService mMiningService;
    private MiningPools mMiningPools;
    private Plotter mPlotter;
    private PlotFiles mPlotFiles;
    private String mNumericID;

    private Button mBtnMiningAction;
    private Button mBtnSetPool;
    private TextView mTxtCurrentBlock;
    private TextView mTxtPoolSvr;
    private TextView mTxtGBPlot;
    private TextView mTxtDL;
    private TextView mTxtAccepted;

    private ImageView mImgMined;
    private MediaPlayer mMediaPlayer;

    private boolean mIsMining;
    private String mBestDeadline;
    private String mPassPhrase = "";
    private String mRewardID = "";
    private boolean mRewardSet = false;
    private long mFinalConfirm;
    private boolean mPlayedSound = false;

    // Allow to mine in the back ground
    boolean mOnPower;
    boolean mCPULockedOn;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This code is called when we reopen the mining activity!!
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mining);

        mNumericID = getIntent().getStringExtra(MainActivity.NUMERICID);
        mPassPhrase = getIntent().getStringExtra(MainActivity.PASSPHRASE);

        mTxtGBPlot = (TextView) findViewById(R.id.txtGBPlot);
        mTxtCurrentBlock = (TextView) findViewById(R.id.txtCurrentBlock);
        mTxtPoolSvr = (TextView) findViewById(R.id.txtPoolSvr);
        mTxtDL = (TextView) findViewById(R.id.txtCurrentDL);
        mTxtAccepted = (TextView) findViewById(R.id.txtStaticAccepted);
        mTxtAccepted.setVisibility(View.INVISIBLE);

        mImgMined = (ImageView) findViewById(R.id.imgMined);
        mImgMined.setVisibility(View.INVISIBLE);

        mBtnMiningAction = (Button) findViewById(R.id.btnMinerOp);
        mBtnSetPool = (Button) findViewById(R.id.btnSetPool);

        mPlotter = new Plotter(mNumericID);
        mPlotFiles = new PlotFiles(BurstUtil.getPathToSD(), mNumericID);
        mMiningService = new MiningService(this, mPlotFiles, mNumericID);
        mIsMining = false;

        // ToDo: v2.1 allow pool changer
        /*
        mMiningPools = new MiningPools();
        mMiningPools.loadMiningPools();
        */

        int mPlotCt = mPlotter.getPlotSize();
        if (mPlotCt > 0) {
            mTxtGBPlot.setText(Integer.toString(mPlotCt) + " GB");
            mBtnMiningAction.setEnabled(true);
            mBtnMiningAction.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        } else {
            mTxtGBPlot.setText("No Plots Exist");
            mBtnMiningAction.setEnabled(false);
            mBtnMiningAction.setBackgroundColor(Color.DKGRAY);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Start or Stop Mining Button
        mBtnMiningAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMiningService.running)
                    mMiningService.stop();
                    if (PowerTool.isOnPower() == false) {       // after each GB check to see if were plugged in
                        wakeLock.release();
                        mCPULockedOn = false;
                    }

                else
                    mOnPower = PowerTool.isOnPower();
                    mCPULockedOn = false;
                    PowerManager powerManager = (PowerManager) BurstContext.getAppContext().getSystemService(Context.POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyWakelockTag");
                    if (mOnPower) {
                        wakeLock.acquire();
                        mCPULockedOn = true;
                    }
                    mMiningService.start();
            }
        });

        // Set Mining Pool Button
        mBtnSetPool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnSetPool.setText("4 Confirms Remaining");
                mBtnSetPool.setEnabled(false);                      // dont let the user push it again
                mMiningService.start();
                try {
                    Thread.sleep(2000);  // this is very hacky!  But we need to make sure we get the current block
                    mFinalConfirm = mMiningService.mActiveBlock.height + 4;
                } catch (Exception e) {

                }
                // Send the Request
                BurstUtil.setRewardAssignment(sPoolNumericID, mPassPhrase); // <-- Tie our passphrase to the Mining pool payout
            }
        });

        // ToDo: Make this better in the future, this is hacky and only allows a single pool service
        // Mobile Pool: 7Z2V-J9CF-NCW9-HWFRY  - 18401070918313114651
        //  Points to EU.Pool Now

        // https://mobile.burst-team.us:8125/burst?requestType=setRewardRecipient
        // This is normally a post
        // Android Miner BURST-4BS9-D8RC-MTMA-26VUW  --  1039034475383695111

        // Ok, how do we do this, we have to ask the system
        // getRewardRecipient
        // account <-- Our numeric ID
        // {"rewardRecipient":"1039034475383695111","requestProcessingTime":0}
        // rewardRecipient has to be ^^^^^^^^^^^^  Otherwise tell them it's not set and give them a set button.

        getRewardID();

        if (mPlotCt > 0 && mRewardSet) {
            // This never auto starts because getRewardID hasn't had time to happen yet. Need to move to a call back to make it auto
            mMiningService.start();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsMining) {
            mBtnMiningAction.setText("STOP MINING");
            mTxtDL.setText(mBestDeadline);
        } else {
            mBtnMiningAction.setText("START MINING");

        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected  void onRestart() {
        super.onRestart();
        if (mIsMining) {
            mBtnMiningAction.setText("STOP MINING");
            mTxtDL.setText(mBestDeadline);
        } else {
            mBtnMiningAction.setText("START MINING");

        }
    }

    @Override
    public synchronized void notice(String... args) {
        // This is when we get data back from the mining service
        String line = "";
        for (String s : args)
            line += " " + s;
        Log.d(TAG, line);
        switch (args[0]) {
            case "MINING":
                if (args[1].equals("STARTED")) {
                    mIsMining = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBtnMiningAction.setText("STOP MINING");
                        }
                    });
                }
                if (args[1].equals("STOPPED")) {
                    mIsMining = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBtnMiningAction.setText("START MINING");
                            mImgMined.setVisibility(View.INVISIBLE);
                            mTxtAccepted.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                break;
            case "BLOCK":
                if (args[1].equals("HEIGHT")) {
                    final String mCurrentBlock = args[2];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTxtCurrentBlock.setText("Current Block: " + mCurrentBlock);
                            mImgMined.setVisibility(View.INVISIBLE);
                            mTxtAccepted.setVisibility(View.INVISIBLE);
                            mPlayedSound = false;
                            mTxtDL.setText("N/A");

                            if (mFinalConfirm != 0) {
                                Long mToGo = mFinalConfirm - Long.parseLong(mCurrentBlock);
                                mBtnSetPool.setText( Long.toString(mToGo) + " Confirms Remaining");
                                if (mToGo == 0) {
                                    mFinalConfirm = 0;
                                    mRewardID = sPoolNumericID; // More Hackyneess
                                    validateRewardID();
                                }
                            } //mMiningService.mActiveBlock.height + 4;
                        }
                    });
                }
                break;
            case "GOTREWARDID":
                if (args[1].equals("SUCCESS")) {
                    mRewardID = args[2];
                    validateRewardID();
                }
                break;
            case "DEADLINE":
                final String mDL = args[1];
                mBestDeadline = mDL;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTxtDL.setText(mDL);
                    }
                });
                break;
            case "SUBMITNONCE":
                if(args[1].equals("SUCCESS")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImgMined.setVisibility(View.VISIBLE);
                            mTxtAccepted.setVisibility(View.VISIBLE);
                            if(mPlayedSound == false ){
                                mMediaPlayer = MediaPlayer.create(BurstContext.getAppContext(),  R.raw.chaching);
                                mMediaPlayer.start();
                            }
                            mPlayedSound = true;
                        }
                    });
                }
                break;
        }
    }

    private void getRewardID() {
        BurstUtil mBU = new BurstUtil(this);
        mBU.getRewardIDFromNumericID(mNumericID, this);
    }

    private void validateRewardID() {
        if(mRewardID.equals(sPoolNumericID)) {   // <-- MPool
            mRewardSet = true;
            mBtnSetPool.setEnabled(false);
            mBtnSetPool.setVisibility(View.INVISIBLE);
            mTxtPoolSvr.setText(sPoolServer);
        }
        else {
            mRewardSet = false;
            mBtnSetPool.setEnabled(true);
            mBtnSetPool.setVisibility(View.VISIBLE);
            mTxtPoolSvr.setText("Pool Not Set");
        }
    }
}
