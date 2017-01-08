package burstcoin.com.burst;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

    // ToDo: v2.1 read these values from preference
    //final static String sPoolServer = "mobile.burst-team.us:8080";
    //final static String sPoolNumericID = "16647933376790760136";

    // Minor Update to new version
    final static String sPoolServer = "m.burst4all.com";
    final static String sPoolNumericID = "13749927595717144118";

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
    private boolean mCHACHING;
    private MediaPlayer mMediaPlayer;

    private boolean mIsMining;
    private String mBestDeadline;
    private String mPassPhrase = "";
    private String mRewardID = "";
    private boolean mRewardSet = false;
    private long mFinalConfirm;
    private boolean mPlayedSound = false;
    private int mPlotCt;

    // Allow to mine in the back ground
    boolean mOnPower;
    boolean mCPULockedOn;
    PowerManager.WakeLock wakeLock;

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        settings = getSharedPreferences("MINING", 0);
        mCHACHING = settings.getBoolean("CHACHING", false);

        mPlotCt = mPlotter.getPlotSize();
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Start or Stop Mining Button, added power monitor
        mBtnMiningAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMiningService.running) {
                    mMiningService.stop();
                    if (PowerTool.isOnPower() == false) {       // after each GB check to see if were plugged in
                        try {
                            wakeLock.release();
                        }
                        catch (Exception e) {
                            Log.e(TAG, "WakeLock.Release threw an error: " + e.getLocalizedMessage());
                        }
                        mCPULockedOn = false;
                    }
                }
                else {
                    mOnPower = PowerTool.isOnPower();
                    mCPULockedOn = false;
                    PowerManager powerManager = (PowerManager) BurstContext.getAppContext().getSystemService(Context.POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
                    if (mOnPower) {
                        wakeLock.acquire();
                        mCPULockedOn = true;
                    }
                    mMiningService.start();
                }
            }
        });

        // Set Mining Pool Button
        final AlertDialog.Builder mConfirm = new AlertDialog.Builder(this);
        mBtnSetPool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is where we explain whats happening
                mConfirm.setTitle("Are you sure?");
                mConfirm.setMessage("Clicking \"Yes\" will set the pool assignment for this burst account to a new pool.");
                mConfirm.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                mConfirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mBtnSetPool.setText("4 Confirms Remaining");
                        mBtnSetPool.setEnabled(false);
                        mMiningService.start();
                        try {
                            Thread.sleep(5000);                             // this is very hacky!  But we need to make sure we get the current block
                            mFinalConfirm = mMiningService.mActiveBlock.height + 4;
                        } catch (Exception e) {

                        }
                        // Send the Request
                        BurstUtil.setRewardAssignment(sPoolNumericID, mPassPhrase);
                    }
                });
                mConfirm.show();
            }
        });
        getRewardID();
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
    public void notice(String... args) {
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
                            mTxtCurrentBlock.setText("Current Block: Update Coming");
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
                            mTxtDL.setText("");
                            mTxtCurrentBlock.setText("Current Block: Stopped");
                        }
                    });
                    try {
                        mMediaPlayer.release();
                    } catch (Exception e){

                    }
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
                            try {
                                mMediaPlayer.release();
                            } catch (Exception e){
                                // Catch a null reference
                            }
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
                    if (mPlotCt > 0 && mRewardSet) {
                        mMiningService.start();
                    }
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
                    mImgMined.setVisibility(View.VISIBLE);
                    mTxtAccepted.setVisibility(View.VISIBLE);
                    if (mPlayedSound == false && mCHACHING) {
                        synchronized (this) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMediaPlayer = MediaPlayer.create(BurstContext.getAppContext(), R.raw.chaching);
                                    try {
                                        mMediaPlayer.start();
                                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            public void onCompletion(MediaPlayer mMediaPlayer) {
                                                mMediaPlayer.release();
                                            };
                                        });
                                    }
                                    catch (Exception e)
                                    {
                                        // Stupid Media Player no subtitle controller set
                                    }
                                }
                            });
                        }
                        mPlayedSound = true;
                    }
                }
                break;
        }
    }

    private void getRewardID() {
        BurstUtil mBU = new BurstUtil(this);
        mBU.getRewardIDFromNumericID(mNumericID, this);
    }

    private void validateRewardID() {
        if(mRewardID.equals(sPoolNumericID)) {
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
