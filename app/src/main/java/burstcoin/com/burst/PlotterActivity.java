package burstcoin.com.burst;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.TreeMap;

import burstcoin.com.burst.plotting.IntPlotStatus;
import burstcoin.com.burst.plotting.PlotFile;
import burstcoin.com.burst.plotting.Plotter;
import burstcoin.com.burst.tools.BurstContext;

public class PlotterActivity extends AppCompatActivity implements IntPlotStatus {

    private TreeMap<String, String> mMiningPools = null;
    private TextView mTxtDriveInfo;
    private TextView mTxtTest;
    private TextView mTxtDriveMessage;
    private TextView mTxtDrivePlotSize;
    private TextView mTxtPlotsFound;
    private Button mBtnDone;
    private Button mBtnPlot;
    private Button mBtnDeletePlot;
    private SeekBar mSizeBar;
    public ProgressDialog mProgressDialog;

    private String numericID;
    private double mTotalSpace = 0;
    private double mFreeSpace = 0;
    private final static String TAG = "PlotterActivity";
    private Plotter mPlotter = null;

    final static int PERMISSION_STORAGE = 1;
    final static int PERMISSION_WAKELOCK = 2;
    private int permLocks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotter);
        numericID = getIntent().getStringExtra(MainActivity.NUMERICID);
        mPlotter = new Plotter((IntPlotStatus)this, numericID);

        // Enable the Done button
        mBtnDone = (Button) findViewById(R.id.btnMinerOp);
        mBtnPlot = (Button) findViewById(R.id.btnPlot);
        mBtnDeletePlot = (Button) findViewById(R.id.btnDeletePlot);
        mSizeBar = (SeekBar) findViewById(R.id.setPlotSize);
        mTxtDriveInfo =  (TextView) findViewById(R.id.txtDriveInfo);
        mTxtDriveMessage =  (TextView) findViewById(R.id.txtDriveMessage);
        mTxtDrivePlotSize = (TextView) findViewById(R.id.txtPlotGBSize);
        mTxtPlotsFound = (TextView) findViewById(R.id.txtPlotsFound);
        mTxtTest = (TextView) findViewById(R.id.txtTestHolder);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Generating Plot File..");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMax(PlotFile.NonceToComplete);

        mBtnPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted() && isPowerManagerPermissionGranted()) {
                    // First thing we need is get the value of the slider
                    final int mGBToPlot = mSizeBar.getProgress();
                    new Thread() {
                        @Override
                        public void run() {
                            mPlotter.plotGBs(mGBToPlot);
                        }
                    }.start();
                } else {
                    // No Permissions, we need to ask for permissions
                    requestStoragePermission();
                }
            }
        });

        final AlertDialog.Builder mConfirm = new AlertDialog.Builder(this);
        mConfirm.setTitle("Are you sure?");
        mConfirm.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do Nothing, just close down
            }
        });

        mConfirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPlotter.delete1GB();
                updateDriveInfo();
            }
        });

        mBtnDeletePlot.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  mConfirm.show();
              }
          }
        );
        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Lets update the Screen with current System/Plot Information
        updateDriveInfo();
        updateDriveMessage();

        mSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                progress = seekBar.getProgress();
                if(progress > 0) {
                    mBtnPlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mBtnPlot.setEnabled(true);
                } else {
                    mBtnPlot.setBackgroundColor(Color.DKGRAY);
                    mBtnPlot.setEnabled(false);
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                //Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
                mTxtDrivePlotSize.setText("Plot "+Integer.toString(progress)+"GB");
                if(progress > 0) {
                    mBtnPlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mBtnPlot.setEnabled(true);
                } else {
                    mBtnPlot.setBackgroundColor(Color.DKGRAY);
                    mBtnPlot.setEnabled(false);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTxtDrivePlotSize.setText("Plot "+Integer.toString(progress)+"GB");
                if(progress > 0) {
                    mBtnPlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mBtnPlot.setEnabled(true);
                } else {
                    mBtnPlot.setBackgroundColor(Color.DKGRAY);
                    mBtnPlot.setEnabled(false);
                }
            }
        });


        // below here is all pilot code that is a work in progress

        String mFreeMem = Long.toString(new BurstUtil().getFreeMemoryInMB(this));
        String mTotalMem = Long.toString(new BurstUtil().getTotalMemoryInMB(this));
        mTxtTest.setText("Free Memory "+mFreeMem+"MB of "+mTotalMem+"MB");

       // This is where we need to check for permission on a per activity
        //updateCurrentPlotInfo();

        // This will be added v2.1 under Settings, Select a Pool
        //loadMiningPools();

    }

    // We should pass in the Text box to update
    private void updateDriveInfo() {
        try {
            mTotalSpace = BurstUtil.getTotalSpaceInGB();
            mFreeSpace =  BurstUtil.getFreeSpaceInGB();
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong trying to get Space");
        }
        String mStringTotalSpace = Double.toString(mTotalSpace);
        String mStringFreeSpace = Double.toString(mFreeSpace);
        mTxtDriveInfo.setText(mStringFreeSpace+"GB Free of "+ mStringTotalSpace+"GB Total");
        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.barDriveUseage);
        mProgressBar.setMax((int)(mTotalSpace*100));
        mProgressBar.setProgress((int)((mTotalSpace-mFreeSpace)*100));
        DecimalFormat roundingFormat = new DecimalFormat("#");
        roundingFormat.setRoundingMode(RoundingMode.DOWN);
        // Set the Max Slider to the Max Round Down free GB
        mSizeBar.setMax(Integer.parseInt(roundingFormat.format(mFreeSpace)));

        // Lets tell the user how many plots we have
        int mPlotCt = mPlotter.getPlotSize();
        if (mPlotCt > 0) {
            mTxtPlotsFound.setText(Integer.toString(mPlotCt) + "GB of Plots Exist");
            mBtnDeletePlot.setEnabled(true);
            mBtnDeletePlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        } else {
            mTxtPlotsFound.setText("No Plots Exist");
            mBtnDeletePlot.setEnabled(false);
            mBtnDeletePlot.setBackgroundColor(Color.DKGRAY);
        }
    }
    // Best Memory sizes for plotting are things that break nicely into 1GB, powers of 2.
    // Try to target for 512mb of RAM, less is OK

    private void updateDriveMessage() {
        if (mFreeSpace < 1) {
            mTxtDriveMessage.setText("Not Enough Space to Plot");
            mBtnPlot.setEnabled(false);
            mBtnPlot.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        else {
            mTxtDriveMessage.setText("");
            mBtnPlot.setEnabled(true);
            mBtnPlot.setBackgroundColor(Color.DKGRAY);
        }
    }

    @Override
    public void notice(String... args){
        // This is how we get data back from the Plotter Tool
        String line="";
        for (String s : args)
            line+=" " + s;
        Log.d(TAG, line);
        switch (args[0]) {
            case "PLOTTING":
                if (args[1].equals("NONCE")) {
                    if(args[2].equals(Integer.toString(PlotFile.NonceToComplete))) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                mProgressDialog.setProgress(0);
                                mPlotter.reload();
                                updateDriveInfo();
                            }
                        });
                    } else if (args[2].equals(Integer.toString(0))) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.show();
                            }
                        });

                    } else {
                        mProgressDialog.setProgress(Integer.parseInt(args[2]));
                    }
                }
                break;
            case "TOAST":
                final String sError = args[1];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(BurstContext.getAppContext(), sError, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
        }
    }

    // This is for premissions as defined in Android 6.0
    private  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK}, PERMISSION_STORAGE);
        // do I also need to put WAKE_LOCK in here?
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WAKE_LOCK},PERMISSION_WAKELOCK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.v(TAG, "Permission Denied: STORAGE");
                    mBtnPlot.setEnabled(false);
                    // Lets tell them we can't work without this
                    return;
                }
                mBtnPlot.setEnabled(true);
                break;
            }
            case PERMISSION_WAKELOCK: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Lets tell them we can't work without this
                    Log.v(TAG, "Permission Denied: WAKELOCK");
                    return;
                }
            }
        }
    }

    private boolean isPowerManagerPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WAKE_LOCK)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"WAKE_LOCK Permission is granted");
                return true;
            } else {
                Log.v(TAG,"WAKE_LOCK Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, PERMISSION_WAKELOCK);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

}
